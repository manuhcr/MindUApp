package com.example.mindu;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mindu.api.ApiService;
import com.example.mindu.api.RetrofitClient;
import com.example.mindu.api.profissional.ProfissionalPerfilRequest;
import com.example.mindu.api.profissional.ProfissionalResponse;
import com.example.mindu.api.profissional.agendamento.AgendamentoResponse;
import com.example.mindu.api.profissional.agendamento.LinkOnlineRequest;
import com.example.mindu.api.profissional.disponibilidade.DisponibilidadeRequest;
import com.example.mindu.api.profissional.local.LocalRequest;
import com.example.mindu.api.profissional.detalhe.ProfissionalDetalheResponse;
import com.example.mindu.api.profissional.disponibilidade.DisponibilidadeResponse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardProfissional extends AppCompatActivity {
    private ProfilePhotoController photos;
    private ApiService api;
    private LinearLayout content;
    private ProfissionalResponse profile;
    private final List<DisponibilidadeRequest> ranges = new ArrayList<>();
    private TextView rangesText;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        if (!SessionManager.guard(this, "PROFISSIONAL")) return;
        photos = new ProfilePhotoController(this, true, state);
        api = RetrofitClient.getApiService(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(18), dp(16), dp(18), dp(88));
        TextView title = label("Painel do Profissional", 23, true);
        root.addView(title);
        LinearLayout nav = new LinearLayout(this);
        nav.setOrientation(LinearLayout.HORIZONTAL);
        String[] tabs = {"Minha agenda", "Meu perfil", "Locais", "Disponibilidade"};
        for (String tab : tabs) {
            Button button = new Button(this); button.setText(tab); button.setTextSize(11);
            nav.addView(button, new LinearLayout.LayoutParams(0, dp(52), 1));
            button.setOnClickListener(v -> open(tab));
        }
        root.addView(nav);
        ScrollView scroll = new ScrollView(this);
        content = new LinearLayout(this); content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(0, dp(12), 0, dp(20)); scroll.addView(content);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        Button logout = new Button(this); logout.setText("Sair da conta"); logout.setOnClickListener(v -> SessionManager.logout(this));
        root.addView(logout);
        setContentView(root);
        MilaFab.attach(this);
        open("Minha agenda");
    }

    @Override protected void onSaveInstanceState(Bundle out) { if (photos != null) photos.onSaveInstanceState(out); super.onSaveInstanceState(out); }

    private void open(String tab) {
        content.removeAllViews();
        if ("Minha agenda".equals(tab)) loadAgenda();
        else if ("Meu perfil".equals(tab)) loadProfile();
        else if ("Locais".equals(tab)) localForm();
        else availabilityForm();
    }

    private void loadProfile() {
        content.addView(label("Carregando perfil…", 16, false));
        api.meuPerfilProfissional().enqueue(new Callback<ProfissionalResponse>() {
            @Override public void onResponse(Call<ProfissionalResponse> call, Response<ProfissionalResponse> response) {
                content.removeAllViews();
                if (!response.isSuccessful() || response.body() == null) { showError(response.code()); return; }
                profile = response.body();
                FrameLayout avatar = new FrameLayout(DashboardProfissional.this);
                TextView initial = label(first(profile.getNome()), 30, true); initial.setGravity(Gravity.CENTER);
                ImageView image = new ImageView(DashboardProfissional.this); image.setScaleType(ImageView.ScaleType.CENTER_CROP);
                avatar.addView(image, new FrameLayout.LayoutParams(dp(96), dp(96)));
                avatar.addView(initial, new FrameLayout.LayoutParams(dp(96), dp(96)));
                content.addView(avatar, new LinearLayout.LayoutParams(dp(96), dp(96)));
                photos.bind(avatar, image, initial, profile.getNome());
                content.addView(label(profile.getNome(), 20, true));
                EditText bio = field("Bio", profile.getBio()); content.addView(bio);
                EditText tags = field("Áreas (separe por vírgula)", profile.getTags() == null ? "" : android.text.TextUtils.join(", ", profile.getTags())); content.addView(tags);
                Button save = button("Salvar bio e áreas"); content.addView(save);
                save.setOnClickListener(v -> {
                    List<String> items = new ArrayList<>();
                    for (String item : tags.getText().toString().split(",")) if (!item.trim().isEmpty()) items.add(item.trim());
                    api.atualizarPerfilProfissional(profile.getId(), new ProfissionalPerfilRequest(bio.getText().toString().trim(), items))
                            .enqueue(simpleCallback("Perfil atualizado.", "Não foi possível atualizar o perfil."));
                });
                content.addView(label("Tipo: " + profile.getTipo() + "   •   Status: " + profile.getStatus(), 14, false));
            }
            @Override public void onFailure(Call<ProfissionalResponse> call, Throwable t) { content.removeAllViews(); content.addView(label("Falha de conexão ao carregar perfil.", 15, false)); }
        });
    }

    private void localForm() {
        content.addView(label("Adicionar local de atendimento", 19, true));
        TextView existingPlaces = label("Consultando locais cadastrados…", 14, false); content.addView(existingPlaces);
        String professionalId = SessionManager.getUserId(this);
        if (professionalId != null) api.detalhes(professionalId).enqueue(new Callback<ProfissionalDetalheResponse>() {
            @Override public void onResponse(Call<ProfissionalDetalheResponse> c, Response<ProfissionalDetalheResponse> r) {
                if (!r.isSuccessful() || r.body() == null || r.body().getLocais() == null || r.body().getLocais().isEmpty()) { existingPlaces.setText("Nenhum local cadastrado."); return; }
                StringBuilder value = new StringBuilder("Locais cadastrados:\n");
                for (com.example.mindu.api.profissional.local.LocalResponse place : r.body().getLocais()) {
                    value.append("• ").append(place.getModalidade());
                    if (place.getCidade() != null) value.append(" — ").append(place.getCidade()).append("/").append(place.getEstado());
                    value.append("\n");
                }
                existingPlaces.setText(value.toString());
            }
            @Override public void onFailure(Call<ProfissionalDetalheResponse> c, Throwable t) { existingPlaces.setText("Não foi possível listar os locais agora."); }
        });
        Spinner modality = spinner(new String[]{"PRESENCIAL", "VIRTUAL", "HIBRIDO"}); content.addView(modality);
        EditText cep = field("CEP", ""); EditText street = field("Logradouro", ""); EditText number = field("Número", "");
        EditText complement = field("Complemento", ""); EditText district = field("Bairro", ""); EditText city = field("Cidade", ""); EditText state = field("UF", "");
        List<EditText> address = Arrays.asList(cep, street, number, complement, district, city, state);
        for (EditText edit : address) content.addView(edit);
        modality.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> p, View v, int position, long id) {
                boolean virtual = "VIRTUAL".equals(modality.getSelectedItem().toString());
                for (EditText edit : address) edit.setVisibility(virtual ? View.GONE : View.VISIBLE);
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> p) { }
        });
        Button add = button("Adicionar local"); content.addView(add);
        add.setOnClickListener(v -> {
            String mode = modality.getSelectedItem().toString();
            if (!"VIRTUAL".equals(mode) && (blank(cep) || blank(street) || blank(city))) { toast("Informe CEP, logradouro e cidade para atendimento presencial."); return; }
            String id = SessionManager.getUserId(this);
            if (id == null) { toast("Não foi possível identificar o perfil."); return; }
            api.adicionarLocal(id, new LocalRequest(mode, text(cep), text(street), text(number), text(complement), text(district), text(city), text(state)))
                    .enqueue(simpleCallback("Local adicionado.", "Não foi possível adicionar o local."));
        });
    }

    private void availabilityForm() {
        content.addView(label("Disponibilidade semanal", 19, true));
        ranges.clear();
        Spinner day = spinner(new String[]{"SEGUNDA", "TERCA", "QUARTA", "QUINTA", "SEXTA", "SABADO", "DOMINGO"}); content.addView(day);
        EditText start = field("Início (HH:mm)", "09:00"); EditText end = field("Fim (HH:mm)", "17:00");
        content.addView(start); content.addView(end);
        rangesText = label("Nenhum horário adicionado", 14, false); content.addView(rangesText);
        Button save = button("Salvar disponibilidade"); save.setEnabled(false); content.addView(save);
        String professionalId = SessionManager.getUserId(this);
        if (professionalId != null) api.detalhes(professionalId).enqueue(new Callback<ProfissionalDetalheResponse>() {
            @Override public void onResponse(Call<ProfissionalDetalheResponse> c, Response<ProfissionalDetalheResponse> r) {
                save.setEnabled(true);
                if (!r.isSuccessful() || r.body() == null || r.body().getDisponibilidadeSemanal() == null) return;
                List<DisponibilidadeResponse> current = r.body().getDisponibilidadeSemanal();
                for (DisponibilidadeResponse item : current) ranges.add(new DisponibilidadeRequest(item.getDiaSemana(), item.getHoraInicio(), item.getHoraFim()));
                rangesText.setText(current.isEmpty() ? "Nenhuma faixa cadastrada." : "Faixas atuais carregadas: " + current.size() + ". Adicione outras antes de salvar.");
            }
            @Override public void onFailure(Call<ProfissionalDetalheResponse> c, Throwable t) { save.setEnabled(true); rangesText.setText("Não foi possível carregar a disponibilidade atual."); }
        });
        else save.setEnabled(true);
        Button add = button("Adicionar faixa"); content.addView(add);
        add.setOnClickListener(v -> {
            String from = text(start), to = text(end);
            try {
                if (toMinutes(from) >= toMinutes(to)) { toast("O fim precisa ser depois do início."); return; }
                ranges.add(new DisponibilidadeRequest(day.getSelectedItem().toString(), from + (from.length() == 5 ? ":00" : ""), to + (to.length() == 5 ? ":00" : "")));
                rangesText.setText(ranges.size() + " faixa(s) configurada(s) nesta edição.");
            } catch (Exception e) { toast("Informe horários válidos no formato HH:mm."); }
        });
        save.setOnClickListener(v -> {
            String id = SessionManager.getUserId(this); if (id == null) { toast("Não foi possível identificar o perfil."); return; }
            api.definirDisponibilidade(id, new ArrayList<>(ranges)).enqueue(new Callback<List<com.example.mindu.api.profissional.disponibilidade.DisponibilidadeResponse>>() {
                @Override public void onResponse(Call<List<com.example.mindu.api.profissional.disponibilidade.DisponibilidadeResponse>> c, Response<List<com.example.mindu.api.profissional.disponibilidade.DisponibilidadeResponse>> r) {
                    toast(r.isSuccessful() ? "Disponibilidade salva." : "A API recusou a disponibilidade. Revise os horários e tente novamente.");
                }
                @Override public void onFailure(Call<List<com.example.mindu.api.profissional.disponibilidade.DisponibilidadeResponse>> c, Throwable t) { toast("Falha de conexão ao salvar disponibilidade."); }
            });
        });
    }

    private void loadAgenda() {
        content.addView(label("Carregando agenda…", 16, false));
        api.minhaAgenda().enqueue(new Callback<List<AgendamentoResponse>>() {
            @Override public void onResponse(Call<List<AgendamentoResponse>> call, Response<List<AgendamentoResponse>> response) {
                content.removeAllViews();
                if (!response.isSuccessful() || response.body() == null) { showError(response.code()); return; }
                if (response.body().isEmpty()) { content.addView(label("Você ainda não tem consultas agendadas.", 15, false)); return; }
                for (AgendamentoResponse item : response.body()) {
                    content.addView(label(item.getDataHora() + "\n" + item.getClienteNome() + " • " + item.getModalidade() + " • " + item.getStatus(), 15, true));
                    LinearLayout actions = new LinearLayout(DashboardProfissional.this);
                    Button link = button("Link online"); Button done = button("Realizada"); Button cancel = button("Cancelar");
                    actions.addView(link, new LinearLayout.LayoutParams(0, -2, 1)); actions.addView(done, new LinearLayout.LayoutParams(0, -2, 1)); actions.addView(cancel, new LinearLayout.LayoutParams(0, -2, 1)); content.addView(actions);
                    link.setOnClickListener(v -> askLink(item));
                    done.setOnClickListener(v -> api.marcarRealizado(item.getId()).enqueue(simpleCallback("Consulta marcada como realizada.", "Não foi possível atualizar a consulta.")));
                    cancel.setOnClickListener(v -> new AlertDialog.Builder(DashboardProfissional.this).setMessage("Cancelar esta consulta?").setNegativeButton("Voltar", null).setPositiveButton("Cancelar", (d, w) -> api.cancelar(item.getId()).enqueue(simpleCallback("Consulta cancelada.", "Não foi possível cancelar a consulta."))).show());
                }
            }
            @Override public void onFailure(Call<List<AgendamentoResponse>> call, Throwable t) { content.removeAllViews(); content.addView(label("Falha de conexão ao carregar a agenda.", 15, false)); }
        });
    }

    private void askLink(AgendamentoResponse item) {
        EditText input = field("Link HTTPS", item.getLinkOnline() == null ? "https://" : item.getLinkOnline());
        new AlertDialog.Builder(this).setTitle("Link da consulta online").setView(input).setNegativeButton("Voltar", null)
                .setPositiveButton("Salvar", (d, w) -> api.definirLinkOnline(item.getId(), new LinkOnlineRequest(text(input))).enqueue(simpleCallback("Link salvo.", "Informe um link HTTPS válido."))).show();
    }

    private <T> Callback<T> simpleCallback(String ok, String error) {
        return new Callback<T>() {
            @Override public void onResponse(Call<T> c, Response<T> r) { toast(r.isSuccessful() ? ok : error); }
            @Override public void onFailure(Call<T> c, Throwable t) { toast("Falha de conexão. Tente novamente."); }
        };
    }
    private TextView label(String text, int size, boolean bold) { TextView v = new TextView(this); v.setText(text); v.setTextSize(size); v.setPadding(0, dp(8), 0, dp(8)); if (bold) v.setTypeface(null, android.graphics.Typeface.BOLD); return v; }
    private EditText field(String hint, String value) { EditText v = new EditText(this); v.setSingleLine(true); v.setHint(hint); v.setText(value); return v; }
    private Button button(String text) { Button b = new Button(this); b.setText(text); b.setTextSize(11); return b; }
    private Spinner spinner(String[] items) { Spinner s = new Spinner(this); s.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items)); return s; }
    private String first(String name) { return name == null || name.trim().isEmpty() ? "P" : name.trim().substring(0, 1).toUpperCase(); }
    private String text(EditText v) { return v.getText().toString().trim(); }
    private boolean blank(EditText v) { return text(v).isEmpty(); }
    private int toMinutes(String value) {
        String[] parts = value.split(":");
        if (parts.length != 2) throw new IllegalArgumentException();
        int hours = Integer.parseInt(parts[0]); int minutes = Integer.parseInt(parts[1]);
        if (hours < 0 || hours > 23 || minutes < 0 || minutes > 59) throw new IllegalArgumentException();
        return hours * 60 + minutes;
    }
    private void toast(String value) { Toast.makeText(this, value, Toast.LENGTH_LONG).show(); }
    private void showError(int code) { content.addView(label(code == 403 ? "Sua conta ainda não tem permissão para acessar esta função." : "Não foi possível carregar os dados agora.", 15, false)); }
    private int dp(int n) { return (int) (n * getResources().getDisplayMetrics().density + .5f); }
}
