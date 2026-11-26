package com.example.mindu;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CalendarView;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mindu.adapter.AgendaProfissionalAdapter;

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
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardProfissional extends AppCompatActivity {
    private ProfilePhotoController photos;
    private ApiService api;
    private FrameLayout containerProfissional;
    private LinearLayout content;
    private View navInicio, navAgenda, navDisponibilidade, navPerfil;
    private ProfissionalResponse profile;
    private final List<DisponibilidadeRequest> ranges = new ArrayList<>();
    private TextView rangesText;
    private final List<AgendamentoResponse> agendaCompleta = new ArrayList<>();
    private RecyclerView rvAgenda;
    private TextView agendaEstado;
    private TextView agendaData;
    private AgendaProfissionalAdapter agendaAdapter;
    private String dataAgendaSelecionada;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        if (!SessionManager.guard(this, "PROFISSIONAL")) return;
        photos = new ProfilePhotoController(this, true, state);
        api = RetrofitClient.getApiService(this);
        setContentView(R.layout.activity_dashboard_profissional);
        containerProfissional = findViewById(R.id.containerProfissional);
        navInicio = findViewById(R.id.navInicioProfissional);
        navAgenda = findViewById(R.id.navAgendaProfissional);
        navDisponibilidade = findViewById(R.id.navDisponibilidadeProfissional);
        navPerfil = findViewById(R.id.navPerfilProfissional);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setClipToPadding(false);
        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setClipToPadding(false);
        scroll.addView(content, new ScrollView.LayoutParams(-1, -2));
        containerProfissional.addView(scroll, new FrameLayout.LayoutParams(-1, -1));

        navInicio.setOnClickListener(v -> open("Início"));
        navAgenda.setOnClickListener(v -> open("Minha agenda"));
        navDisponibilidade.setOnClickListener(v -> open("Disponibilidade"));
        navPerfil.setOnClickListener(v -> open("Meu perfil"));
        MilaFab.attach(this);
        open("Início");
    }

    @Override protected void onSaveInstanceState(Bundle out) { if (photos != null) photos.onSaveInstanceState(out); super.onSaveInstanceState(out); }

    private String greeting(String name) {
        return SaudacaoFormatter.format(name);
    }

    private void carregarSaudacaoProfissional(TextView saudacao) {
        api.minhaConta().enqueue(new Callback<com.example.mindu.api.login.MinhaContaResponse>() {
            @Override
            public void onResponse(Call<com.example.mindu.api.login.MinhaContaResponse> call,
                                   Response<com.example.mindu.api.login.MinhaContaResponse> response) {
                if (!response.isSuccessful() || response.body() == null) return;
                String nome = response.body().getNome();
                if (nome == null || nome.trim().isEmpty()) return;
                nome = nome.trim();
                SessionManager.saveName(DashboardProfissional.this, nome);
                saudacao.setText(greeting(nome));
            }

            @Override
            public void onFailure(Call<com.example.mindu.api.login.MinhaContaResponse> call, Throwable t) {
                // Mantém o nome recebido no login e salvo na sessão.
            }
        });
    }

    private void open(String tab) {
        content.removeAllViews();
        content.setPadding(0, 0, 0, dp(20));
        content.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        selecionarNavegacao(tab);
        if ("Início".equals(tab)) loadHome();
        else if ("Minha agenda".equals(tab)) loadAgenda();
        else if ("Meu perfil".equals(tab)) loadProfile();
        else if ("Locais".equals(tab)) localForm();
        else availabilityForm();
        if (!"Início".equals(tab) && !"Minha agenda".equals(tab)) {
            content.setPadding(dp(18), dp(24), dp(18), dp(24));
            content.setBackgroundResource(R.drawable.bg_vidro);
            content.setElevation(dp(4));
            content.setClipToOutline(true);
        }
    }

    private void selecionarNavegacao(String tab) {
        int ativo = android.graphics.Color.WHITE;
        int inativo = android.graphics.Color.rgb(212, 205, 233);
        colorirNav(navInicio, "Início".equals(tab) ? ativo : inativo);
        colorirNav(navAgenda, "Minha agenda".equals(tab) ? ativo : inativo);
        colorirNav(navDisponibilidade, "Disponibilidade".equals(tab) ? ativo : inativo);
        colorirNav(navPerfil, "Meu perfil".equals(tab) || "Locais".equals(tab) ? ativo : inativo);
    }

    private void colorirNav(View view, int color) {
        if (view instanceof ImageView) ((ImageView) view).setColorFilter(color);
        else if (view instanceof android.view.ViewGroup) {
            android.view.ViewGroup group = (android.view.ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) colorirNav(group.getChildAt(i), color);
        }
    }

    private void loadHome() {
        View home = LayoutInflater.from(this).inflate(R.layout.content_inicio_profissional, content, false);
        content.addView(home, new LinearLayout.LayoutParams(-1, -2));
        TextView greeting = home.findViewById(R.id.txtSaudacaoProfissional);
        TextView summary = home.findViewById(R.id.txtResumoDiaProfissional);
        TextView next = home.findViewById(R.id.txtProximoAtendimentoProfissional);
        greeting.setText(greeting(SessionManager.getName(this)));
        carregarSaudacaoProfissional(greeting);
        home.findViewById(R.id.cardAgendaProfissionalHome).setOnClickListener(v -> open("Minha agenda"));
        home.findViewById(R.id.cardDisponibilidadeProfissionalHome).setOnClickListener(v -> open("Disponibilidade"));
        home.findViewById(R.id.cardPerfilProfissionalHome).setOnClickListener(v -> open("Meu perfil"));

        summary.setText("Carregando agenda…");
        next.setText("Buscando próximo atendimento…");
        api.minhaAgenda().enqueue(new Callback<List<AgendamentoResponse>>() {
            @Override public void onResponse(Call<List<AgendamentoResponse>> call, Response<List<AgendamentoResponse>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    summary.setText("Não foi possível carregar os atendimentos.");
                    next.setText("Tente novamente pela agenda.");
                    return;
                }
                String hoje = new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new java.util.Date());
                List<AgendamentoResponse> today = new ArrayList<>();
                for (AgendamentoResponse item : response.body()) {
                    if (item != null && AgendamentoUiRules.belongsToDate(item.getDataHora(), hoje)) today.add(item);
                }
                summary.setText(today.size() + (today.size() == 1 ? " atendimento hoje" : " atendimentos hoje"));
                if (today.isEmpty()) next.setText("Você não tem atendimentos marcados para hoje.");
                else {
                    AgendamentoResponse item = today.get(0);
                    String dateTime = item.getDataHora() == null ? "" : item.getDataHora();
                    String time = dateTime.length() >= 16 ? dateTime.substring(11, 16) : "horário não informado";
                    next.setText("Próximo: " + value(item.getClienteNome(), "Cliente") + " • " + time);
                }
            }
            @Override public void onFailure(Call<List<AgendamentoResponse>> call, Throwable error) {
                summary.setText("Falha ao carregar a agenda.");
                next.setText("Confira sua conexão e tente novamente.");
            }
        });
    }

    private void loadProfile() {
        content.addView(label("Carregando perfil…", 16, false));
        api.meuPerfilProfissional().enqueue(new Callback<ProfissionalResponse>() {
            @Override public void onResponse(Call<ProfissionalResponse> call, Response<ProfissionalResponse> response) {
                content.removeAllViews();
                if (!response.isSuccessful() || response.body() == null) { showError(response.code()); return; }
                profile = response.body();
                content.addView(label("Meu perfil", 24, true));
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
                Button locations = button("Gerenciar locais de atendimento");
                content.addView(locations);
                locations.setOnClickListener(v -> open("Locais"));
                Button logout = button("Sair da conta");
                content.addView(logout);
                logout.setOnClickListener(v -> SessionManager.logout(DashboardProfissional.this));
            }
            @Override public void onFailure(Call<ProfissionalResponse> call, Throwable t) { content.removeAllViews(); content.addView(label("Falha de conexão ao carregar perfil.", 15, false)); }
        });
    }

    private void localForm() {
        content.addView(label("Locais de atendimento", 24, true));
        content.addView(label("Gerencie os endereços e modalidades que aparecem no seu perfil.", 13, false));
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
        content.addView(label("Disponibilidade", 24, true));
        content.addView(label("Defina os horários em que você pode atender.", 13, false));
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
        content.removeAllViews();
        View agendaView = LayoutInflater.from(this).inflate(R.layout.content_agenda_profissional, content, false);
        content.addView(agendaView, new LinearLayout.LayoutParams(-1, -2));

        CalendarView calendar = agendaView.findViewById(R.id.calendarAgendaProfissional);
        rvAgenda = agendaView.findViewById(R.id.rvAgendaProfissional);
        agendaEstado = agendaView.findViewById(R.id.txtAgendaVaziaProfissional);
        agendaData = agendaView.findViewById(R.id.txtDataAgendaProfissional);
        if (dataAgendaSelecionada == null) {
            java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.US);
            dataAgendaSelecionada = dateFormat.format(new java.util.Date());
        }
        rvAgenda.setLayoutManager(new LinearLayoutManager(this));
        agendaAdapter = new AgendaProfissionalAdapter(new AgendaProfissionalAdapter.Listener() {
            @Override public void onLink(AgendamentoResponse item) { askLink(item); }
            @Override public void onStart(AgendamentoResponse item) { abrirSessaoDemo(item); }
            @Override public void onDemo(AgendamentoResponse item) { abrirSessaoDemo(item); }
            @Override public void onRealizada(AgendamentoResponse item) {
                api.marcarRealizado(item.getId()).enqueue(agendaActionCallback("Consulta marcada como realizada.", "Não foi possível atualizar a consulta."));
            }
            @Override public void onCancelar(AgendamentoResponse item) {
                new AlertDialog.Builder(DashboardProfissional.this)
                        .setMessage("Cancelar esta consulta?")
                        .setNegativeButton("Voltar", null)
                        .setPositiveButton("Cancelar", (d, w) -> api.cancelar(item.getId())
                                .enqueue(agendaActionCallback("Consulta cancelada.", "Não foi possível cancelar a consulta.")))
                        .show();
            }
        });
        rvAgenda.setAdapter(agendaAdapter);
        agendaEstado.setText("Carregando agenda…");
        agendaEstado.setVisibility(View.VISIBLE);
        atualizarAgendaDoDia();
        calendar.setOnDateChangeListener((view, year, month, day) -> {
            dataAgendaSelecionada = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, day);
            atualizarAgendaDoDia();
        });

        api.minhaAgenda().enqueue(new Callback<List<AgendamentoResponse>>() {
            @Override public void onResponse(Call<List<AgendamentoResponse>> call, Response<List<AgendamentoResponse>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    agendaCompleta.clear();
                    agendaAdapter.atualizar(null);
                    agendaEstado.setText(response.code() == 403
                            ? "Sua conta ainda não tem permissão para acessar a agenda."
                            : "Não foi possível carregar a agenda.");
                    agendaEstado.setVisibility(View.VISIBLE);
                    rvAgenda.setVisibility(View.GONE);
                    return;
                }
                agendaCompleta.clear();
                for (AgendamentoResponse item : response.body()) if (item != null) agendaCompleta.add(item);
                atualizarAgendaDoDia();
            }
            @Override public void onFailure(Call<List<AgendamentoResponse>> call, Throwable t) {
                agendaCompleta.clear();
                agendaAdapter.atualizar(null);
                agendaEstado.setText("Falha de conexão ao carregar a agenda.");
                agendaEstado.setVisibility(View.VISIBLE);
                rvAgenda.setVisibility(View.GONE);
            }
        });
    }

    private void atualizarAgendaDoDia() {
        if (agendaAdapter == null || agendaEstado == null || rvAgenda == null) return;
        agendaData.setText("Atendimentos em " + dataAgendaSelecionada);
        List<AgendamentoResponse> doDia = new ArrayList<>();
        for (AgendamentoResponse item : agendaCompleta) {
            String dateTime = item == null ? null : item.getDataHora();
            if (AgendamentoUiRules.belongsToDate(dateTime, dataAgendaSelecionada)) doDia.add(item);
        }
        agendaAdapter.atualizar(doDia);
        boolean vazio = doDia.isEmpty();
        agendaEstado.setText(vazio ? "Nenhum atendimento para esta data." : "");
        agendaEstado.setVisibility(vazio ? View.VISIBLE : View.GONE);
        rvAgenda.setVisibility(vazio ? View.GONE : View.VISIBLE);
    }

    private <T> Callback<T> agendaActionCallback(String success, String error) {
        return new Callback<T>() {
            @Override public void onResponse(Call<T> call, Response<T> response) {
                if (response.isSuccessful()) {
                    toast(success);
                    open("Minha agenda");
                } else toast(error);
            }
            @Override public void onFailure(Call<T> call, Throwable t) {
                toast("Falha de conexão. Tente novamente.");
            }
        };
    }

    private void abrirSessaoDemo(AgendamentoResponse item) {
        if (item == null) return;
        SessionDemoStore.markStarted(this, item);
        android.content.Intent intent = new android.content.Intent(this, SalaAtendimentoActivity.class);
        intent.putExtra(SalaAtendimentoActivity.ROLE, "PROFISSIONAL");
        intent.putExtra(SalaAtendimentoActivity.ID, item.getId());
        intent.putExtra(SalaAtendimentoActivity.CLIENT_ID, item.getClienteId());
        intent.putExtra(SalaAtendimentoActivity.CLIENT_NAME, item.getClienteNome());
        intent.putExtra(SalaAtendimentoActivity.PRO_ID, item.getProfissionalId());
        intent.putExtra(SalaAtendimentoActivity.PRO_NAME, item.getProfissionalNome());
        intent.putExtra(SalaAtendimentoActivity.MODALITY, item.getModalidade());
        intent.putExtra(SalaAtendimentoActivity.DATE, item.getDataHora());
        startActivity(intent);
    }

    private void askLink(AgendamentoResponse item) {
        EditText input = field("Link HTTPS", item.getLinkOnline() == null ? "https://" : item.getLinkOnline());
        new AlertDialog.Builder(this).setTitle("Link da consulta online").setView(input).setNegativeButton("Voltar", null)
                .setPositiveButton("Salvar", (d, w) -> api.definirLinkOnline(item.getId(), new LinkOnlineRequest(text(input))).enqueue(agendaActionCallback("Link salvo.", "Informe um link HTTPS válido."))).show();
    }

    private <T> Callback<T> simpleCallback(String ok, String error) {
        return new Callback<T>() {
            @Override public void onResponse(Call<T> c, Response<T> r) { toast(r.isSuccessful() ? ok : error); }
            @Override public void onFailure(Call<T> c, Throwable t) { toast("Falha de conexão. Tente novamente."); }
        };
    }
    private TextView label(String text, int size, boolean bold) { TextView v = new TextView(this); v.setText(text); v.setTextSize(size); v.setPadding(0, dp(8), 0, dp(8)); v.setTextColor(android.graphics.Color.parseColor(bold ? "#4B426D" : "#777184")); v.setTypeface(android.graphics.Typeface.create(bold ? "sans-serif-medium" : "sans-serif", bold ? 1 : 0)); return v; }
    private EditText field(String hint, String value) { EditText v = new EditText(this); v.setSingleLine(true); v.setHint(hint); v.setText(value); v.setBackgroundResource(R.drawable.bg_input_mila); v.setTextColor(android.graphics.Color.parseColor("#4B426D")); v.setHintTextColor(android.graphics.Color.parseColor("#9992A8")); v.setPadding(dp(14), dp(8), dp(14), dp(8)); return v; }
    private Button button(String text) { Button b = new Button(this); b.setText(text); b.setTextSize(13); b.setAllCaps(false); b.setTextColor(android.graphics.Color.WHITE); b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#75689A"))); b.setTypeface(android.graphics.Typeface.create("sans-serif-medium", 0)); return b; }
    private Spinner spinner(String[] items) { Spinner s = new Spinner(this); s.setBackgroundResource(R.drawable.bg_input_mila); s.setPadding(dp(12), 0, dp(12), 0); s.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items)); return s; }
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
    private String value(String value, String fallback) { return value == null || value.trim().isEmpty() ? fallback : value.trim(); }
    private void showError(int code) { content.addView(label(code == 403 ? "Sua conta ainda não tem permissão para acessar esta função." : "Não foi possível carregar os dados agora.", 15, false)); }
    private int dp(int n) { return (int) (n * getResources().getDisplayMetrics().density + .5f); }
}
