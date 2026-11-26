package com.example.mindu;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mindu.api.RetrofitClient;

import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Sala local para demonstração; não estabelece uma chamada real de áudio/vídeo. */
public final class SalaAtendimentoActivity extends AppCompatActivity {
    private static final ExecutorService IMAGE_EXECUTOR = Executors.newSingleThreadExecutor();
    public static final String ROLE="session_role", ID="session_id", CLIENT_ID="session_client_id",
            CLIENT_NAME="session_client_name", PRO_ID="session_professional_id",
            PRO_NAME="session_professional_name", MODALITY="session_modality", DATE="session_date";
    private final Handler handler=new Handler(Looper.getMainLooper());
    private TextView status, loadingText;
    private View loading;
    private long appointmentTime;
    private final Runnable countdown = new Runnable() {
        @Override public void run() {
            long remaining = appointmentTime - System.currentTimeMillis();
            if (remaining > 0 && remaining <= 300000L) {
                long minutes = Math.max(1, (remaining + 59999L) / 60000L);
                loadingText.setText("Sua consulta marcada começa em " + minutes
                        + (minutes == 1 ? " minuto" : " minutos"));
                loading.setVisibility(View.VISIBLE);
                handler.postDelayed(this, 1000L);
            } else {
                loading.setVisibility(View.GONE);
                if (remaining <= 0 && "CLIENTE".equalsIgnoreCase(getIntent().getStringExtra(ROLE))) {
                    status.setText("●  O host entrou na sala");
                }
            }
        }
    };

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        String role=getIntent().getStringExtra(ROLE);
        if (!"CLIENTE".equalsIgnoreCase(role) && !"PROFISSIONAL".equalsIgnoreCase(role)) { finish(); return; }
        if (!SessionManager.guard(this, role.toUpperCase(Locale.ROOT))) return;
        setContentView(R.layout.content_sala_online);
        boolean pro="PROFISSIONAL".equalsIgnoreCase(role);
        String clientId=getIntent().getStringExtra(CLIENT_ID), client=getIntent().getStringExtra(CLIENT_NAME);
        String proId=getIntent().getStringExtra(PRO_ID), professional=getIntent().getStringExtra(PRO_NAME);
        String otherName=pro?client:professional, otherId=pro?clientId:proId;
        TextView name=findViewById(R.id.txtNomeVideoEspecialista);
        ImageView photo=findViewById(R.id.imgFotoVideoEspecialista);
        status=findViewById(R.id.txtStatusConexao);
        TextView info=findViewById(R.id.txtInfoSessaoSala);
        loading=findViewById(R.id.preCallOverlay);
        loadingText=findViewById(R.id.txtPreCallMessage);
        name.setText(value(otherName,"Participante"));
        info.setText(value(getIntent().getStringExtra(MODALITY),"Modalidade")+"  •  "+value(getIntent().getStringExtra(DATE),"Horário não informado"));
        status.setText(pro?"●  Consulta iniciada (sessão de exemplo)":"●  O host entrou na sala");
        photo.setImageResource(R.drawable.ic_user);
        loadPhoto(photo,otherId,!pro);
        toggle(findViewById(R.id.btnMicrofone),"Microfone");
        toggle(findViewById(R.id.btnCamera),"Câmera");
        View flip=findViewById(R.id.btnAlternarCamera);
        if(flip!=null)flip.setOnClickListener(v->Toast.makeText(this,"Câmera de demonstração alternada.",Toast.LENGTH_SHORT).show());
        View chat=findViewById(R.id.btnChatSessao);
        if(chat!=null)chat.setOnClickListener(v->Toast.makeText(this,"Chat não conectado nesta demonstração.",Toast.LENGTH_LONG).show());
        android.widget.Button end=findViewById(R.id.btnEncerrarSessao);
        end.setText(pro?"Encerrar atendimento":"Sair da consulta");
        end.setOnClickListener(v->{if(pro)SessionDemoStore.clear(this,getIntent().getStringExtra(ID));finish();});
        appointmentTime = until(getIntent().getStringExtra(DATE));
        long remaining = appointmentTime - System.currentTimeMillis();
        if(remaining>300000L) {
            status.setText("●  Aguardando o horário marcado");
            handler.postDelayed(countdown, remaining-300000L);
        } else if(remaining>0) handler.post(countdown);
        else loading.setVisibility(View.GONE);
    }

    private void toggle(ImageButton button,String label) {
        if(button==null)return;
        button.setOnClickListener(v->{boolean on=!Boolean.TRUE.equals(button.getTag());button.setTag(on);button.setAlpha(on?1f:.45f);Toast.makeText(this,label+(on?" ativado.":" desativado.")+" (demonstração)",Toast.LENGTH_SHORT).show();});
    }

    private void loadPhoto(ImageView target,String id,boolean remote) {
        if(id==null||!id.matches("[A-Za-z0-9_-]{1,100}"))return;
        if(!remote) {
            File photo=new File(new File(getFilesDir(),"profile_photos"),"profile_"+id+".jpg");
            if(photo.isFile())IMAGE_EXECUTOR.execute(()->show(target,BitmapFactory.decodeFile(photo.getAbsolutePath())));
            return;
        }
        RetrofitClient.getApiService(this).foto(id).enqueue(new Callback<ResponseBody>() {
            @Override public void onResponse(Call<ResponseBody> call,Response<ResponseBody> response) {
                if(!response.isSuccessful()||response.body()==null)return; ResponseBody body=response.body();
                IMAGE_EXECUTOR.execute(()->{Bitmap bitmap=null;try(InputStream in=body.byteStream()){bitmap=BitmapFactory.decodeStream(in);}catch(Exception ignored){}finally{body.close();}show(target,bitmap);});
            }
            @Override public void onFailure(Call<ResponseBody> call,Throwable error) { }
        });
    }

    private void show(ImageView view,Bitmap bitmap){if(bitmap!=null)runOnUiThread(()->{if(!isFinishing())view.setImageBitmap(bitmap);});}
    private static long until(String value) {
        if(value==null)return -1;
        String[] formats={"yyyy-MM-dd'T'HH:mm:ss.SSSXXX","yyyy-MM-dd'T'HH:mm:ssXXX","yyyy-MM-dd'T'HH:mm:ss","yyyy-MM-dd HH:mm:ss"};
        for(String pattern:formats)try{Date date=new SimpleDateFormat(pattern,Locale.US).parse(value);if(date!=null)return date.getTime();}catch(Exception ignored){}
        return -1;
    }
    private static String value(String text,String fallback){return text==null||text.trim().isEmpty()?fallback:text.trim();}
    @Override protected void onDestroy(){handler.removeCallbacks(countdown);super.onDestroy();}
}
