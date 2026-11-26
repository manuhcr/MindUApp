package com.example.mindu;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.inputmethod.EditorInfo;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mindu.adapter.MensagemAdapter;
import com.example.mindu.api.ApiService;
import com.example.mindu.api.RetrofitClient;
import com.example.mindu.api.chat.ChatRequest;
import com.example.mindu.api.chat.ChatResponse;
import com.example.mindu.api.chat.ChatTurn;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.Headers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatMilaActivity extends AppCompatActivity {
    private static final int REQUEST_AUDIO_PERMISSION = 410;
    private static final int MAX_AUDIO_BYTES = 10 * 1024 * 1024;
    private static final int MAX_AUDIO_DURATION_MS = 60_000;
    private static final String TYPING = "Mila digitando...";
    private static final String WELCOME = "Olá! Eu sou a Mila. Como posso ajudar?";

    private final List<ChatTurn> messages = new ArrayList<>();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private RecyclerView recycler;
    private EditText input;
    private ImageButton sendButton;
    private ImageButton microphoneButton;
    private ImageButton cancelAudioButton;
    private TextView status;
    private MensagemAdapter adapter;
    private ApiService api;
    private Call<ChatResponse> pendingCall;
    private MediaRecorder recorder;
    private File audioFile;
    private boolean recording;
    private boolean busy;
    private final Runnable durationLimit = () -> pararGravacao(true);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String type = SessionManager.getType(this);
        if (SessionManager.getToken(this) == null || type == null
                || !("CLIENTE".equalsIgnoreCase(type) || "EMPRESA".equalsIgnoreCase(type)
                || "PROFISSIONAL".equalsIgnoreCase(type) || "ADMIN".equalsIgnoreCase(type))) {
            SessionManager.returnToAccess(this, "Entre novamente para conversar com a Mila.");
            return;
        }
        setContentView(R.layout.activity_chat_mila);
        api = RetrofitClient.getApiService(this);
        recycler = findViewById(R.id.rvMensagensMila);
        input = findViewById(R.id.edtMensagemMila);
        sendButton = findViewById(R.id.btnEnviarMila);
        microphoneButton = findViewById(R.id.btnAudioMila);
        cancelAudioButton = findViewById(R.id.btnCancelarAudioMila);
        status = findViewById(R.id.txtStatusMila);
        findViewById(R.id.btnVoltarChatMila).setOnClickListener(v -> finish());

        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MensagemAdapter(messages);
        recycler.setAdapter(adapter);
        ArrayList<String> restored = savedInstanceState == null ? null
                : savedInstanceState.getStringArrayList("mila_messages");
        if (restored != null && restored.size() >= 2) {
            for (int i = 0; i + 1 < restored.size(); i += 2) {
                messages.add(new ChatTurn(restored.get(i), restored.get(i + 1)));
            }
        } else {
            messages.add(new ChatTurn("assistant", WELCOME));
        }
        atualizarLista();

        sendButton.setOnClickListener(v -> enviarTexto(input.getText().toString()));
        input.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                enviarTexto(input.getText().toString());
                return true;
            }
            return false;
        });
        microphoneButton.setOnClickListener(v -> alternarGravacao());
        cancelAudioButton.setOnClickListener(v -> pararGravacao(false));
        cancelAudioButton.setVisibility(View.GONE);
        ((Button) findViewById(R.id.btnSugestaoBem)).setOnClickListener(v -> enviarTexto("Estou bem 😊"));
        ((Button) findViewById(R.id.btnSugestaoConversar)).setOnClickListener(v -> enviarTexto("Preciso conversar"));
        ((Button) findViewById(R.id.btnSugestaoAnsiosa)).setOnClickListener(v -> enviarTexto("Me sinto ansiosa"));
    }

    private void enviarTexto(String raw) {
        String text = raw == null ? "" : raw.trim();
        if (busy || text.isEmpty()) return;
        if (text.length() > 4000) {
            input.setError("A mensagem pode ter até 4.000 caracteres.");
            return;
        }
        List<ChatTurn> history = historicoAtual();
        messages.add(new ChatTurn("user", text));
        input.setText("");
        int typingPosition = adicionarDigitando();
        setBusy(true, "Enviando mensagem...");
        pendingCall = api.enviarMensagem(new ChatRequest(text, history));
        pendingCall.enqueue(new Callback<ChatResponse>() {
            @Override public void onResponse(Call<ChatResponse> call, Response<ChatResponse> response) {
                if (!estaAtiva()) return;
                if (response.isSuccessful() && response.body() != null
                        && response.body().getResponse() != null
                        && !response.body().getResponse().trim().isEmpty()) {
                    messages.set(typingPosition, new ChatTurn("assistant", response.body().getResponse().trim()));
                    atualizarLista();
                    setBusy(false, "Chat de bem-estar");
                } else {
                    removerDigitando(typingPosition);
                    setBusy(false, "Chat de bem-estar");
                    tratarErro(response.code(), response.headers(), response.errorBody());
                }
            }
            @Override public void onFailure(Call<ChatResponse> call, Throwable error) {
                if (!estaAtiva() || call.isCanceled()) return;
                removerDigitando(typingPosition);
                setBusy(false, "Chat de bem-estar");
                mostrarFalhaRede(error);
            }
        });
    }

    private void alternarGravacao() {
        if (busy) return;
        if (recording) {
            pararGravacao(true);
            return;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},
                    REQUEST_AUDIO_PERMISSION);
            return;
        }
        iniciarGravacao();
    }

    private void iniciarGravacao() {
        try {
            audioFile = File.createTempFile("mila_audio_", ".m4a", getCacheDir());
            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            recorder.setAudioSamplingRate(16000);
            recorder.setAudioEncodingBitRate(64000);
            recorder.setMaxDuration(MAX_AUDIO_DURATION_MS);
            recorder.setMaxFileSize(MAX_AUDIO_BYTES);
            recorder.setOutputFile(audioFile.getAbsolutePath());
            recorder.setOnInfoListener((mediaRecorder, what, extra) -> {
                if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED
                        || what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED) {
                    pararGravacao(true);
                }
            });
            recorder.prepare();
            recorder.start();
            recording = true;
            cancelAudioButton.setVisibility(View.VISIBLE);
            microphoneButton.setContentDescription("Parar e enviar áudio");
            status.setText("Gravando… toque no microfone para enviar ou cancele.");
            handler.postDelayed(durationLimit, MAX_AUDIO_DURATION_MS);
        } catch (Exception error) {
            liberarGravador();
            apagarAudio();
            Toast.makeText(this, "Não foi possível iniciar a gravação. Verifique o microfone do emulador.", Toast.LENGTH_LONG).show();
        }
    }

    private void pararGravacao(boolean enviar) {
        if (!recording) return;
        recording = false;
        handler.removeCallbacks(durationLimit);
        try {
            recorder.stop();
        } catch (RuntimeException ignored) {
            enviar = false;
        } finally {
            liberarGravador();
        }
        cancelAudioButton.setVisibility(View.GONE);
        microphoneButton.setContentDescription("Gravar áudio");
        if (!enviar || audioFile == null || !audioFile.exists() || audioFile.length() == 0) {
            apagarAudio();
            status.setText("Chat de bem-estar");
            if (enviar) Toast.makeText(this, "A gravação ficou vazia. Tente novamente.", Toast.LENGTH_LONG).show();
            return;
        }
        if (audioFile.length() > MAX_AUDIO_BYTES) {
            apagarAudio();
            status.setText("Chat de bem-estar");
            Toast.makeText(this, "O áudio pode ter no máximo 10 MB.", Toast.LENGTH_LONG).show();
            return;
        }
        enviarAudioGravado(audioFile);
    }

    private void enviarAudioGravado(File file) {
        List<ChatTurn> history = historicoAtual();
        int typingPosition = adicionarDigitando();
        setBusy(true, "Enviando áudio para a Mila…");
        RequestBody audioBody = RequestBody.create(MediaType.parse("audio/mp4"), file);
        MultipartBody.Part audioPart = MultipartBody.Part.createFormData("file", file.getName(), audioBody);
        RequestBody historyPart = RequestBody.create(MediaType.parse("text/plain"), new Gson().toJson(history));
        pendingCall = api.enviarAudio(audioPart, null, historyPart);
        pendingCall.enqueue(new Callback<ChatResponse>() {
            @Override public void onResponse(Call<ChatResponse> call, Response<ChatResponse> response) {
                if (!estaAtiva()) { file.delete(); return; }
                file.delete();
                if (response.isSuccessful() && response.body() != null) {
                    ChatResponse result = response.body();
                    removerDigitando(typingPosition);
                    String transcription = result.getTranscription();
                    if (transcription != null && !transcription.trim().isEmpty()) {
                        messages.add(new ChatTurn("user", transcription.trim()));
                    }
                    if (result.getResponse() != null && !result.getResponse().trim().isEmpty()) {
                        messages.add(new ChatTurn("assistant", result.getResponse().trim()));
                    }
                    atualizarLista();
                    setBusy(false, "Chat de bem-estar");
                } else {
                    removerDigitando(typingPosition);
                    setBusy(false, "Chat de bem-estar");
                    tratarErro(response.code(), response.headers(), response.errorBody());
                }
            }
            @Override public void onFailure(Call<ChatResponse> call, Throwable error) {
                file.delete();
                if (!estaAtiva() || call.isCanceled()) return;
                removerDigitando(typingPosition);
                setBusy(false, "Chat de bem-estar");
                mostrarFalhaRede(error);
            }
        });
    }

    private List<ChatTurn> historicoAtual() {
        List<ChatTurn> valid = new ArrayList<>();
        for (ChatTurn turn : messages) {
            if (("user".equals(turn.getRole()) || "assistant".equals(turn.getRole()))
                    && !TYPING.equals(turn.getContent())
                    && !WELCOME.equals(turn.getContent())) valid.add(turn);
        }
        int start = Math.max(0, valid.size() - 20);
        return new ArrayList<>(valid.subList(start, valid.size()));
    }

    private int adicionarDigitando() {
        messages.add(new ChatTurn("assistant", TYPING));
        int position = messages.size() - 1;
        atualizarLista();
        return position;
    }

    private void removerDigitando(int position) {
        if (position >= 0 && position < messages.size() && TYPING.equals(messages.get(position).getContent())) {
            messages.remove(position);
            adapter.notifyItemRemoved(position);
        }
    }

    private void atualizarLista() {
        if (adapter != null) adapter.notifyDataSetChanged();
        if (recycler != null && adapter != null && adapter.getItemCount() > 0) {
            recycler.post(() -> recycler.scrollToPosition(adapter.getItemCount() - 1));
        }
    }

    private void setBusy(boolean value, String message) {
        busy = value;
        sendButton.setEnabled(!value);
        microphoneButton.setEnabled(!value && !recording);
        findViewById(R.id.btnSugestaoBem).setEnabled(!value);
        findViewById(R.id.btnSugestaoConversar).setEnabled(!value);
        findViewById(R.id.btnSugestaoAnsiosa).setEnabled(!value);
        input.setEnabled(!value);
        status.setText(message);
    }

    private void tratarErro(int code, Headers headers, okhttp3.ResponseBody errorBody) {
        if (errorBody != null) errorBody.close();
        if (code == 401) {
            Toast.makeText(this, "Sessão expirada. Entre novamente.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        String message;
        if (code == 429) {
            String retryAfter = headers.get("Retry-After");
            message = retryAfter != null && retryAfter.matches("[0-9]+")
                    ? "Muitas mensagens. Tente novamente em " + retryAfter + " segundos."
                    : "Muitas mensagens. Aguarde um pouco e tente novamente.";
        } else if (code == 502 || code == 503 || code >= 500) {
            message = "A Mila está indisponível no momento. Tente novamente em instantes.";
        } else if (code == 413) {
            message = "O áudio passou do limite de 10 MB.";
        } else if (code == 422 || code == 400) {
            message = "A Mila não conseguiu entender o conteúdo. Tente outra gravação ou mensagem.";
        } else {
            message = "Não foi possível enviar sua mensagem (HTTP " + code + ").";
        }
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void mostrarFalhaRede(Throwable error) {
        String message = error instanceof SocketTimeoutException
                ? "A Mila demorou para responder. Tente novamente."
                : "Não foi possível conectar à API local. Verifique se o Java e a Mila estão ativos.";
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private boolean estaAtiva() {
        return !isFinishing() && !isDestroyed();
    }

    private void liberarGravador() {
        if (recorder != null) {
            try { recorder.reset(); } catch (RuntimeException ignored) { }
            recorder.release();
            recorder = null;
        }
    }

    private void apagarAudio() {
        if (audioFile != null) {
            audioFile.delete();
            audioFile = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_AUDIO_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                iniciarGravacao();
            } else {
                Toast.makeText(this, "Permita o acesso ao microfone para enviar áudio.", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        ArrayList<String> saved = new ArrayList<>();
        for (ChatTurn turn : messages) {
            if (!TYPING.equals(turn.getContent())) {
                saved.add(turn.getRole());
                saved.add(turn.getContent());
            }
        }
        outState.putStringArrayList("mila_messages", saved);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(durationLimit);
        if (pendingCall != null) pendingCall.cancel();
        if (recording) {
            recording = false;
            try { if (recorder != null) recorder.stop(); } catch (RuntimeException ignored) { }
        }
        liberarGravador();
        apagarAudio();
        super.onDestroy();
    }
}
