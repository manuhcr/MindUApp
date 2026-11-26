
package com.example.mindu;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mindu.adapter.ProfissionaisAdapter;
import com.example.mindu.adapter.MinhasConsultasAdapter;
import com.example.mindu.api.RetrofitClient;
import com.example.mindu.api.login.MinhaContaResponse;
import com.example.mindu.api.profissional.ProfissionalResponse;
import com.example.mindu.api.profissional.agendamento.AgendamentoRequest;
import com.example.mindu.api.profissional.agendamento.AgendamentoResponse;
import com.example.mindu.api.profissional.agendamento.ReagendarRequest;
import com.example.mindu.api.profissional.detalhe.ProfissionalDetalheResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import eightbitlab.com.blurview.BlurTarget;
import eightbitlab.com.blurview.BlurView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardBeneficiario extends AppCompatActivity {

    private FrameLayout containerBeneficiario;
    private FrameLayout fabMilaBeneficiario;

    private ImageView navInicio;
    private ImageView navAgendamentos;
    private ImageView navImediato;
    private ImageView navPerfil;

    private BlurTarget blurTargetDashboard;

    private ProfissionaisAdapter profissionaisAdapter;

    private ProfissionalResponse profissionalSelecionado;

    private String nomeBeneficiario = "";
    private ProfilePhotoController profilePhotoController;
    private String dataHoraSelecionada;
    private String filtroTipo = "";
    private boolean carregandoProfissionais;
    private String erroCarregamentoProfissionais;
    private final List<ProfissionalResponse> profissionaisDisponiveis = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!SessionManager.guard(this, "CLIENTE")) return;
        setContentView(R.layout.activity_dashboard_beneficiario);
        MilaFab.attach(this);
        profilePhotoController = new ProfilePhotoController(this, false, savedInstanceState);
        fabMilaBeneficiario = findViewById(R.id.fabMilaBeneficiario);

        inicializarComponentes();
        configurarNavegacao();

        abrirTela(R.layout.content_inicio_beneficiario);

        carregarNomeBeneficiario();
    }

    private void inicializarComponentes() {

        containerBeneficiario = findViewById(
                R.id.containerBeneficiario
        );

        navInicio = findViewById(
                R.id.navInicioBeneficiario
        );

        navAgendamentos = findViewById(
                R.id.navAgendamentosBeneficiario
        );

        navImediato = findViewById(
                R.id.navImediatoBeneficiario
        );

        navPerfil = findViewById(
                R.id.navPerfilBeneficiario
        );

        blurTargetDashboard = findViewById(
                R.id.blurTargetDashboardBeneficiario
        );
    }

    private void configurarNavegacao() {

        navInicio.setOnClickListener(v ->
                abrirTela(R.layout.content_inicio_beneficiario));

        navAgendamentos.setOnClickListener(v ->
                abrirTela(R.layout.content_agendamento_beneficiario));

        navImediato.setOnClickListener(v -> mostrarMinhasConsultas());

        navPerfil.setOnClickListener(v ->
                abrirTela(R.layout.content_perfil_beneficiario));
    }

    // Abre as telas dentro do container
    private void abrirTela(int layout) {

        View tela = LayoutInflater.from(this)
                .inflate(layout, containerBeneficiario, false);

        containerBeneficiario.removeAllViews();
        containerBeneficiario.addView(tela);
        if (fabMilaBeneficiario != null) fabMilaBeneficiario.setVisibility(
                layout == R.layout.content_agendamento_beneficiario
                        || layout == R.layout.content_sala_online ? View.GONE : View.VISIBLE);
        if (layout == R.layout.content_sala_online) { mostrarMinhasConsultas(); return; }

        if (layout == R.layout.content_perfil_beneficiario) {
            View sair = tela.findViewById(R.id.cardSairBeneficiario);
            if (sair != null) sair.setOnClickListener(v -> SessionManager.logout(this));
            profilePhotoController.bind(tela.findViewById(R.id.areaFotoBeneficiario),
                    tela.findViewById(R.id.imgFotoBeneficiario),
                    tela.findViewById(R.id.txtInicialBeneficiario), nomeBeneficiario);
        }

        if (layout == R.layout.content_inicio_beneficiario) {
            View acessoAgendamento = tela.findViewById(R.id.blurAgendamentoRapido);
            if (acessoAgendamento != null) acessoAgendamento.setOnClickListener(v -> abrirAgendamento());
            View btnAgendarSessao = tela.findViewById(R.id.btnAgendarSessao);
            if (btnAgendarSessao != null) btnAgendarSessao.setOnClickListener(v -> abrirAgendamento());

            configurarBlurViews(tela, blurTargetDashboard);
            configurarConteudosBemEstar(tela);
            atualizarSaudacao();

        } else if (layout == R.layout.content_agendamento_beneficiario) {

            configurarBlurViews(tela, blurTargetDashboard);
            configurarAgendamentos(tela);

        } else if (layout == R.layout.content_perfil_profissional) {

            configurarBlurViews(tela, blurTargetDashboard);
            configurarPerfilProfissional(tela);

        } else if (layout == R.layout.content_escolher_horario) {

            configurarBlurViews(tela, blurTargetDashboard);
            configurarTelaAgendamento(tela);
        }
    }

    private void abrirAgendamento() {
        if (profissionalSelecionado != null) {
            abrirTela(R.layout.content_escolher_horario);
            return;
        }

        View modal = LayoutInflater.from(this).inflate(R.layout.dialog_escolher_profissional, null, false);
        android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(this)
                .setView(modal)
                .create();
        View buscar = modal.findViewById(R.id.btnBuscarProfissionalDialog);
        View fechar = modal.findViewById(R.id.btnFecharProfissionalDialog);
        if (buscar != null) buscar.setOnClickListener(v -> {
            dialog.dismiss();
            abrirTela(R.layout.content_agendamento_beneficiario);
        });
        if (fechar != null) fechar.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }

    private void configurarConteudosBemEstar(View tela) {
        configurarConteudo(tela, R.id.blurConteudoAnsiedade,
                R.layout.dialog_conteudo_ansiedade, R.id.btnFecharConteudoAnsiedade);
        configurarConteudo(tela, R.id.blurConteudoBurnout,
                R.layout.dialog_conteudo_burnout, R.id.btnFecharConteudoBurnout);
        configurarConteudo(tela, R.id.blurConteudoAutocuidado,
                R.layout.dialog_conteudo_autocuidado, R.id.btnFecharConteudoAutocuidado);
        configurarConteudo(tela, R.id.blurConteudoSono,
                R.layout.dialog_conteudo_sono, R.id.btnFecharConteudoSono);
    }

    private void configurarConteudo(View tela, int cardId, int layoutId, int closeId) {
        View card = tela.findViewById(cardId);
        if (card == null) return;
        card.setOnClickListener(v -> {
            View conteudo = LayoutInflater.from(this).inflate(layoutId, null, false);
            android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(this)
                    .setView(conteudo)
                    .create();
            View fechar = conteudo.findViewById(closeId);
            if (fechar != null) fechar.setOnClickListener(button -> dialog.dismiss());
            dialog.show();
        });
    }

    @Override protected void onSaveInstanceState(Bundle outState) {
        if (profilePhotoController != null) profilePhotoController.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    // Configura todos os BlurViews encontrados na tela
    private void configurarBlurViews(View view, BlurTarget target) {

        if (view == null || target == null) {
            return;
        }

        if (view instanceof BlurView) {

            // Preserva o card roxo de bem-estar
            if (view.getId() != R.id.blurBemEstarBeneficiario) {

                BlurView blurView = (BlurView) view;

                blurView.setupWith(target)
                        .setBlurRadius(20f);

                aplicarEfeitoVidroLiquido(blurView);
            }
        }

        if (view instanceof ViewGroup) {

            ViewGroup grupo = (ViewGroup) view;

            for (int i = 0; i < grupo.getChildCount(); i++) {

                configurarBlurViews(
                        grupo.getChildAt(i),
                        target
                );
            }
        }
    }

    // Aplica o visual de vidro líquido
    private void aplicarEfeitoVidroLiquido(View view) {

        if (view == null) {
            return;
        }

        GradientDrawable liquidGlass = new GradientDrawable();

        liquidGlass.setShape(GradientDrawable.RECTANGLE);

        liquidGlass.setOrientation(
                GradientDrawable.Orientation.TL_BR
        );

        liquidGlass.setColor(
                Color.parseColor("#1F8294EF")
        );

        float density = view.getContext()
                .getResources()
                .getDisplayMetrics()
                .density;

        liquidGlass.setCornerRadius(24 * density);

        int strokeWidthPx = (int) (1 * density);

        liquidGlass.setStroke(
                strokeWidthPx,
                Color.parseColor("#99F5F3F7")
        );

        view.setBackground(liquidGlass);

        view.setOutlineProvider(
                ViewOutlineProvider.BACKGROUND
        );

        view.setClipToOutline(true);

        view.setElevation(5 * density);
    }

    // Busca os dados da pessoa autenticada
    private void carregarNomeBeneficiario() {
        String nomeSessao = SessionManager.getName(this);
        if (nomeSessao != null && !nomeSessao.trim().isEmpty()) {
            nomeBeneficiario = nomeSessao.trim();
            TextView initial = findViewById(R.id.txtInicialBeneficiario);
            if (initial != null) initial.setText(nomeBeneficiario.substring(0, 1).toUpperCase());
            atualizarSaudacao();
        }

        RetrofitClient.getApiService(this)
                .minhaConta()
                .enqueue(new Callback<MinhaContaResponse>() {

                    @Override
                    public void onResponse(
                            Call<MinhaContaResponse> call,
                            Response<MinhaContaResponse> response
                    ) {

                        if (response.isSuccessful()
                                && response.body() != null) {

                            String nome = response.body().getNome();

                            if (nome != null && !nome.trim().isEmpty()) {

                                nomeBeneficiario = nome.trim();
                                SessionManager.saveName(DashboardBeneficiario.this, nomeBeneficiario);
                                TextView initial = findViewById(R.id.txtInicialBeneficiario);
                                if (initial != null) initial.setText(nomeBeneficiario.substring(0, 1).toUpperCase());

                                atualizarSaudacao();
                            }
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<MinhaContaResponse> call,
                            Throwable t
                    ) {

                        // Mantém o nome salvo na sessão quando /me falha.
                    }
                });
    }

    // Atualiza a saudação na tela inicial
    private void atualizarSaudacao() {

        if (containerBeneficiario == null) {
            return;
        }

        TextView txtSaudacao = containerBeneficiario.findViewById(
                R.id.txtSaudacaoBeneficiario
        );

        if (txtSaudacao != null) {

            txtSaudacao.setText(SaudacaoFormatter.format(nomeBeneficiario));
        }
    }

    // Configura a lista de profissionais
    private void configurarAgendamentos(View tela) {

        RecyclerView rvProfissionais = tela.findViewById(
                R.id.rvProfissionais
        );

        EditText edtBuscar = tela.findViewById(
                R.id.edtBuscarProfissional
        );

        TextView txtNenhum = tela.findViewById(
                R.id.txtNenhumProfissional
        );

        if (rvProfissionais == null || edtBuscar == null || txtNenhum == null) return;
        rvProfissionais.setLayoutManager(
                new LinearLayoutManager(this)
        );

        rvProfissionais.setHasFixedSize(true);

        profissionaisAdapter = new ProfissionaisAdapter(
                new ArrayList<>(),
                null
        );

        profissionaisAdapter.setModoBeneficiario(true);

        profissionaisAdapter.setOnProfissionalClickListener(profissional -> {

            profissionalSelecionado = profissional;

            abrirTela(R.layout.content_perfil_profissional);

        });

        rvProfissionais.setAdapter(profissionaisAdapter);

        configurarChipsTipo(tela, txtNenhum);
        Button minhasConsultas = new Button(this);
        minhasConsultas.setText("Minhas consultas");
        estilizarBotaoMindU(minhasConsultas);
        minhasConsultas.setOnClickListener(v -> mostrarMinhasConsultas());
        ViewGroup listContainer = (ViewGroup) rvProfissionais.getParent();
        if (listContainer != null) listContainer.addView(minhasConsultas, listContainer.indexOfChild(rvProfissionais));

        txtNenhum.setVisibility(View.GONE);

        // Pesquisa pelo nome
        edtBuscar.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(
                    CharSequence s,
                    int start,
                    int count,
                    int after
            ) {
            }

            @Override
            public void onTextChanged(
                    CharSequence s,
                    int start,
                    int before,
                    int count
            ) {

                profissionaisAdapter.filtrar(s.toString());
                atualizarEstadoProfissionais(txtNenhum);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        carregarProfissionais(txtNenhum);
    }

    // Busca os profissionais disponíveis
    private void carregarProfissionais(TextView txtNenhum) {
        carregandoProfissionais = true;
        erroCarregamentoProfissionais = null;
        atualizarEstadoProfissionais(txtNenhum);
        RetrofitClient.getApiService(this).buscar(null, null, null)
                .enqueue(new Callback<List<ProfissionalResponse>>() {
                    @Override
                    public void onResponse(Call<List<ProfissionalResponse>> call,
                                           Response<List<ProfissionalResponse>> response) {
                        carregandoProfissionais = false;
                        profissionaisDisponiveis.clear();
                        if (!response.isSuccessful() || response.body() == null) {
                            erroCarregamentoProfissionais = "Não foi possível carregar os profissionais. Tente novamente.";
                            if (profissionaisAdapter != null) profissionaisAdapter.atualizarLista(new ArrayList<>());
                            atualizarEstadoProfissionais(txtNenhum);
                            return;
                        }
                        for (ProfissionalResponse item : response.body()) {
                            if (item != null) profissionaisDisponiveis.add(item);
                        }
                        erroCarregamentoProfissionais = null;
                        aplicarFiltroTipo(txtNenhum);
                    }

                    @Override
                    public void onFailure(Call<List<ProfissionalResponse>> call, Throwable t) {
                        carregandoProfissionais = false;
                        erroCarregamentoProfissionais = "Falha de conexão. Tente novamente.";
                        profissionaisDisponiveis.clear();
                        if (profissionaisAdapter != null) profissionaisAdapter.atualizarLista(new ArrayList<>());
                        atualizarEstadoProfissionais(txtNenhum);
                    }
                });
    }

    // Configura o perfil do profissional selecionado
    private void configurarPerfilProfissional(View tela) {

        if (profissionalSelecionado == null) {

            Toast.makeText(
                    this,
                    "Não foi possível identificar o profissional.",
                    Toast.LENGTH_SHORT
            ).show();

            abrirTela(R.layout.content_agendamento_beneficiario);
            return;
        }

        TextView txtNome = tela.findViewById(R.id.txtNomeProfissional);
        TextView txtTipo = tela.findViewById(R.id.txtTipoProfissional);
        TextView txtStatus = tela.findViewById(R.id.txtStatusProfissional);
        TextView txtTags = tela.findViewById(R.id.txtTagsProfissional);
        TextView txtModalidades = tela.findViewById(R.id.txtModalidadesProfissional);

        txtNome.setText(profissionalSelecionado.getNome());

        String tipo = profissionalSelecionado.getTipo();

        txtTipo.setText(
                tipo != null && !tipo.trim().isEmpty()
                        ? tipo
                        : "Profissional"
        );

        String status = profissionalSelecionado.getStatus();

        if (status != null && !status.trim().isEmpty()) {

            txtStatus.setText("Status: " + status);
            txtStatus.setVisibility(View.VISIBLE);

        } else {

            txtStatus.setVisibility(View.GONE);
        }

        List<String> tags = profissionalSelecionado.getTags();

        if (tags != null && !tags.isEmpty()) {

            txtTags.setText(
                    android.text.TextUtils.join(" • ", tags)
            );

        } else {

            txtTags.setText("Nenhuma área de atuação informada.");
        }

        List<String> modalidades = profissionalSelecionado.getModalidades();

        if (modalidades != null && !modalidades.isEmpty()) {

            txtModalidades.setText(
                    android.text.TextUtils.join(" • ", modalidades)
            );

        } else {

            txtModalidades.setText("Modalidades não informadas.");
        }

        // Abre a tela de escolha de horário
        View btnAgendar = tela.findViewById(
                R.id.btnAgendarSessaoProfissional
        );

        btnAgendar.setOnClickListener(v ->
                abrirTela(R.layout.content_escolher_horario)
        );
        String profissionalId = profissionalSelecionado.getId();
        if (profissionalId != null && !profissionalId.trim().isEmpty()) {
            carregarDetalhesProfissional(tela, profissionalId);
        }
    }

    // Configura a tela de escolha de horário
    private void configurarTelaAgendamento(View tela) {

        if (profissionalSelecionado == null) {
            mostrarProfissionalIndisponivel();
            return;
        }

        // Dados do profissional selecionado
        TextView txtNome = tela.findViewById(
                R.id.txtNomeEspecialistaSelecionado
        );

        TextView txtEspecialidade = tela.findViewById(
                R.id.txtEspecialidadeSelecionada
        );

        txtNome.setText(profissionalSelecionado.getNome());

        String tipo = profissionalSelecionado.getTipo();

        txtEspecialidade.setText(
                tipo != null && !tipo.trim().isEmpty()
                        ? tipo
                        : "Profissional"
        );

        // Impede selecionar datas anteriores a hoje
        CalendarView calendario = tela.findViewById(
                R.id.calendarAgendamento
        );

        calendario.setMinDate(System.currentTimeMillis());

        // Verifica as modalidades disponíveis
        RadioGroup radioModalidade = tela.findViewById(R.id.radioModalidade);
        RadioButton radioOnline = tela.findViewById(R.id.radioOnline);
        RadioButton radioPresencial = tela.findViewById(R.id.radioPresencial);
        if (radioModalidade == null || radioOnline == null || radioPresencial == null) {
            mostrarProfissionalIndisponivel();
            return;
        }

        List<String> modalidades = profissionalSelecionado.getModalidades();

        boolean ofereceOnline = false;
        boolean oferecePresencial = false;

        if (modalidades != null) {

            for (String modalidade : modalidades) {

                if (modalidade == null) {
                    continue;
                }

                String valor = modalidade.trim().toLowerCase(Locale.ROOT);

                if (valor.contains("virtual") || valor.contains("online")) {
                    ofereceOnline = true;
                }

                if (valor.contains("presencial")) {
                    oferecePresencial = true;
                }
                if (valor.contains("hibrido")) {
                    ofereceOnline = true;
                    oferecePresencial = true;
                }
            }
        }

        radioOnline.setVisibility(
                ofereceOnline ? View.VISIBLE : View.GONE
        );

        radioPresencial.setVisibility(
                oferecePresencial ? View.VISIBLE : View.GONE
        );

        if (ofereceOnline || oferecePresencial) {

            radioModalidade.setVisibility(View.VISIBLE);

            if (ofereceOnline) {

                radioOnline.setChecked(true);

            } else {

                radioPresencial.setChecked(true);
            }

        } else {
            radioModalidade.setVisibility(View.GONE);
            mostrarProfissionalIndisponivel();
            return;
        }

        GridLayout grid = tela.findViewById(R.id.gridHorarios);
        CalendarView.OnDateChangeListener dateListener = (view, year, month, day) -> {
            Calendar selected = Calendar.getInstance(); selected.set(year, month, day);
            carregarHorarios(tela, selected.getTimeInMillis());
        };
        calendario.setOnDateChangeListener(dateListener);
        carregarHorarios(tela, calendario.getDate());
        Button continuar = tela.findViewById(R.id.btnContinuarAgendamento);
        continuar.setOnClickListener(v -> confirmarAgendamento(tela));
    }

    private void configurarChipsTipo(View tela, TextView estado) {
        TextView todos = tela.findViewById(R.id.chipTipoTodos);
        TextView psicologo = tela.findViewById(R.id.chipTipoPsicologo);
        TextView psiquiatra = tela.findViewById(R.id.chipTipoPsiquiatra);
        TextView terapeuta = tela.findViewById(R.id.chipTipoTerapeuta);
        if (todos == null || psicologo == null || psiquiatra == null || terapeuta == null) return;
        TextView[] chips = {todos, psicologo, psiquiatra, terapeuta};
        String[] tipos = {"", "PSICOLOGO", "PSIQUIATRA", "TERAPEUTA"};
        for (int i = 0; i < chips.length; i++) {
            final int index = i;
            chips[i].setOnClickListener(v -> {
                filtroTipo = tipos[index];
                for (int j = 0; j < chips.length; j++) {
                    boolean selecionado = j == index;
                    chips[j].setBackgroundResource(selecionado
                            ? R.drawable.bg_filtro_selecionado : R.drawable.bg_filtro_normal);
                    chips[j].setTextColor(android.graphics.Color.parseColor(
                            selecionado ? "#FFFFFF" : "#75689A"));
                    chips[j].setTypeface(null, selecionado
                            ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
                }
                aplicarFiltroTipo(estado);
            });
        }
    }

    private void aplicarFiltroTipo(TextView estado) {
        List<ProfissionalResponse> filtrados = new ArrayList<>();
        for (ProfissionalResponse item : profissionaisDisponiveis) {
            if (item == null) continue;
            if (filtroTipo.isEmpty() || filtroTipo.equalsIgnoreCase(item.getTipo())) filtrados.add(item);
        }
        if (profissionaisAdapter != null) profissionaisAdapter.atualizarLista(filtrados);
        atualizarEstadoProfissionais(estado);
    }

    private void atualizarEstadoProfissionais(TextView estado) {
        if (estado == null) return;
        if (carregandoProfissionais) {
            estado.setText("Carregando profissionais…");
            estado.setVisibility(View.VISIBLE);
        } else if (erroCarregamentoProfissionais != null) {
            estado.setText(erroCarregamentoProfissionais);
            estado.setVisibility(View.VISIBLE);
        } else if (profissionaisAdapter == null || profissionaisAdapter.getItemCount() == 0) {
            estado.setText("Nenhum profissional encontrado");
            estado.setVisibility(View.VISIBLE);
        } else {
            estado.setVisibility(View.GONE);
        }
    }

    private void carregarDetalhesProfissional(View tela, String id) {
        RetrofitClient.getApiService(this).detalhes(id).enqueue(new Callback<ProfissionalDetalheResponse>() {
            @Override public void onResponse(Call<ProfissionalDetalheResponse> call, Response<ProfissionalDetalheResponse> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    TextView about = tela.findViewById(R.id.txtSobreProfissional);
                    if (about != null) about.setText("Não foi possível carregar os detalhes agora.");
                    return;
                }
                ProfissionalDetalheResponse detail = response.body();
                TextView about = tela.findViewById(R.id.txtSobreProfissional);
                if (about != null) {
                    StringBuilder description = new StringBuilder(detail.getBio() == null || detail.getBio().trim().isEmpty() ? "Bio não informada." : detail.getBio());
                    if (detail.getLocais() != null && !detail.getLocais().isEmpty()) {
                        description.append("\n\nLocais e modalidades:\n");
                        for (com.example.mindu.api.profissional.local.LocalResponse place : detail.getLocais()) {
                            if (place == null) continue;
                            description.append("• ").append(place.getModalidade() == null ? "Modalidade não informada" : place.getModalidade());
                            if (place.getCidade() != null) {
                                description.append(" — ").append(place.getCidade());
                                if (place.getEstado() != null) description.append("/").append(place.getEstado());
                            }
                            description.append("\n");
                        }
                    }
                    about.setText(description.toString());
                }
                ImageView photo = tela.findViewById(R.id.imgFotoProfissional);
                if (photo == null || id == null || id.trim().isEmpty()) return;
                RetrofitClient.getApiService(DashboardBeneficiario.this).foto(id).enqueue(new Callback<okhttp3.ResponseBody>() {
                    @Override public void onResponse(Call<okhttp3.ResponseBody> c, Response<okhttp3.ResponseBody> r) {
                        if (!r.isSuccessful() || r.body() == null) return;
                        okhttp3.ResponseBody body = r.body();
                        new Thread(() -> {
                            android.graphics.Bitmap bitmap = null;
                            try { bitmap = android.graphics.BitmapFactory.decodeStream(body.byteStream()); } catch (Exception ignored) { }
                            finally { body.close(); }
                            android.graphics.Bitmap finalBitmap = bitmap;
                            if (finalBitmap != null) runOnUiThread(() -> photo.setImageBitmap(finalBitmap));
                        }).start();
                    }
                    @Override public void onFailure(Call<okhttp3.ResponseBody> c, Throwable t) { }
                });
            }
            @Override public void onFailure(Call<ProfissionalDetalheResponse> call, Throwable t) {
                TextView about = tela.findViewById(R.id.txtSobreProfissional);
                if (about != null) about.setText("Falha de conexão ao carregar os detalhes.");
            }
        });
    }

    private void carregarHorarios(View tela, long dateMillis) {
        GridLayout grid = tela == null ? null : tela.findViewById(R.id.gridHorarios);
        if (grid == null) return;
        dataHoraSelecionada = null;
        grid.removeAllViews();
        TextView state = new TextView(this); state.setText("Carregando horários…"); grid.addView(state);
        if (profissionalSelecionado == null || profissionalSelecionado.getId() == null
                || profissionalSelecionado.getId().trim().isEmpty()) {
            mostrarProfissionalIndisponivel();
            return;
        }
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(dateMillis);
        RetrofitClient.getApiService(this).horarios(profissionalSelecionado.getId(), date).enqueue(new Callback<List<String>>() {
            @Override public void onResponse(Call<List<String>> call, Response<List<String>> response) {
                grid.removeAllViews();
                if (!response.isSuccessful()) {
                    addGridMessage(grid, "Não foi possível consultar os horários.");
                    return;
                }
                List<String> available = response.body();
                List<String> validSlots = new ArrayList<>();
                if (available != null) for (String slot : available)
                    if (slot != null && !slot.trim().isEmpty()) validSlots.add(slot);
                if (validSlots.isEmpty()) {
                    mostrarProfissionalIndisponivel();
                    return;
                }
                grid.setColumnCount(3);
                for (int i = 0; i < validSlots.size(); i++) {
                    final String slot = validSlots.get(i);
                    String label = slot.length() >= 16 ? slot.substring(11, 16) : slot;
                    Button button = new Button(DashboardBeneficiario.this); button.setText(label); button.setTextSize(12);
                    GridLayout.LayoutParams params = new GridLayout.LayoutParams(GridLayout.spec(i / 3), GridLayout.spec(i % 3, 1f));
                    params.width = 0; params.height = dp(44); params.setMargins(dp(3), dp(3), dp(3), dp(3)); grid.addView(button, params);
                    button.setOnClickListener(v -> { dataHoraSelecionada = slot; Toast.makeText(DashboardBeneficiario.this, "Horário selecionado: " + label, Toast.LENGTH_SHORT).show(); });
                }
            }
            @Override public void onFailure(Call<List<String>> call, Throwable t) { grid.removeAllViews(); addGridMessage(grid, "Falha de conexão ao consultar os horários."); }
        });
    }

    private void mostrarProfissionalIndisponivel() {
        View modal = LayoutInflater.from(this).inflate(R.layout.dialog_profissional_indisponivel, null, false);
        android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(this)
                .setView(modal)
                .create();
        View voltar = modal.findViewById(R.id.btnVoltarProfissionalIndisponivel);
        if (voltar != null) voltar.setOnClickListener(v -> {
            dialog.dismiss();
            profissionalSelecionado = null;
            dataHoraSelecionada = null;
            abrirTela(R.layout.content_agendamento_beneficiario);
        });
        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }

    private void addGridMessage(GridLayout grid, String message) { TextView text = new TextView(this); text.setText(message); text.setPadding(dp(8), dp(8), dp(8), dp(8)); grid.addView(text); }

    private void confirmarAgendamento(View tela) {
        if (profissionalSelecionado == null || profissionalSelecionado.getId() == null
                || profissionalSelecionado.getId().trim().isEmpty()) {
            mostrarProfissionalIndisponivel();
            return;
        }
        if (dataHoraSelecionada == null || dataHoraSelecionada.trim().isEmpty()) {
            Toast.makeText(this, "Escolha uma data e um horário disponível.", Toast.LENGTH_LONG).show();
            return;
        }
        RadioGroup modalidades = tela.findViewById(R.id.radioModalidade);
        if (modalidades == null || modalidades.getVisibility() != View.VISIBLE) {
            Toast.makeText(this, "Este profissional não possui modalidade de atendimento disponível.", Toast.LENGTH_LONG).show();
            return;
        }
        RadioButton online = tela.findViewById(R.id.radioOnline);
        String modality = online != null && online.getVisibility() == View.VISIBLE && online.isChecked()
                ? "VIRTUAL" : "PRESENCIAL";
        RetrofitClient.getApiService(this).agendar(new AgendamentoRequest(profissionalSelecionado.getId(), dataHoraSelecionada, modality))
                .enqueue(new Callback<AgendamentoResponse>() {
                    @Override public void onResponse(Call<AgendamentoResponse> call, Response<AgendamentoResponse> response) {
                        if (response.isSuccessful()) { Toast.makeText(DashboardBeneficiario.this, "Consulta agendada com sucesso.", Toast.LENGTH_LONG).show(); mostrarMinhasConsultas(); }
                        else if (response.code() == 409) Toast.makeText(DashboardBeneficiario.this, "Esse horário acabou de ser ocupado. Escolha outro.", Toast.LENGTH_LONG).show();
                        else if (response.code() == 403) Toast.makeText(DashboardBeneficiario.this, "Sua conta ainda não está aprovada para agendar.", Toast.LENGTH_LONG).show();
                        else Toast.makeText(DashboardBeneficiario.this, "Não foi possível confirmar. Verifique as regras de horário e modalidade.", Toast.LENGTH_LONG).show();
                    }
                    @Override public void onFailure(Call<AgendamentoResponse> call, Throwable t) { Toast.makeText(DashboardBeneficiario.this, "Falha de conexão ao confirmar o agendamento.", Toast.LENGTH_LONG).show(); }
                });
    }

    private void mostrarMinhasConsultas() {
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(18), dp(14), dp(18), dp(12));
        if (fabMilaBeneficiario != null) fabMilaBeneficiario.setVisibility(View.GONE);
        Button back = new Button(this);
        back.setText("‹ Buscar profissionais");
        estilizarBotaoMindU(back);
        back.setOnClickListener(v -> abrirTela(R.layout.content_agendamento_beneficiario));
        page.addView(back);
        page.addView(textLabel("Minhas consultas", 22, true));
        TextView state = textLabel("Carregando…", 15, false);
        page.addView(state);
        RecyclerView recycler = new RecyclerView(this);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setNestedScrollingEnabled(false);
        page.addView(recycler, new LinearLayout.LayoutParams(-1, 0, 1));
        containerBeneficiario.removeAllViews();
        containerBeneficiario.addView(page);

        RetrofitClient.getApiService(this).meusAgendamentos().enqueue(new Callback<List<AgendamentoResponse>>() {
            @Override public void onResponse(Call<List<AgendamentoResponse>> call, Response<List<AgendamentoResponse>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    recycler.setVisibility(View.GONE);
                    state.setText(response.code() == 403
                            ? "Sua conta ainda não pode consultar agendamentos."
                            : "Não foi possível carregar suas consultas.");
                    return;
                }
                List<AgendamentoResponse> appointments = new ArrayList<>();
                for (AgendamentoResponse item : response.body()) if (item != null) appointments.add(item);
                if (appointments.isEmpty()) {
                    recycler.setVisibility(View.GONE);
                    state.setText("Você ainda não tem consultas.");
                    return;
                }
                MinhasConsultasAdapter adapter = new MinhasConsultasAdapter(appointments, new MinhasConsultasAdapter.Listener() {
                    @Override public void onAbrirSala(AgendamentoResponse item) { abrirSessaoDemo(item, "CLIENTE"); }
                    @Override public void onCancelar(AgendamentoResponse item) {
                        new android.app.AlertDialog.Builder(DashboardBeneficiario.this)
                                .setMessage("Cancelar esta consulta?")
                                .setNegativeButton("Voltar", null)
                                .setPositiveButton("Cancelar", (d, w) -> RetrofitClient.getApiService(DashboardBeneficiario.this)
                                        .cancelar(item.getId()).enqueue(appointmentCallback("Consulta cancelada.")))
                                .show();
                    }
                    @Override public void onRemarcar(AgendamentoResponse item) { escolherDataReagendamento(item); }
                });
                recycler.setAdapter(adapter);
                state.setVisibility(View.GONE);
                recycler.setVisibility(View.VISIBLE);
            }
            @Override public void onFailure(Call<List<AgendamentoResponse>> call, Throwable t) {
                recycler.setVisibility(View.GONE);
                state.setText("Falha de conexão ao carregar suas consultas.");
            }
        });
    }

    private void abrirSessaoDemo(AgendamentoResponse item, String role) {
        if (item == null) return;
        SessionDemoStore.markStarted(this, item);
        Intent intent = new Intent(this, SalaAtendimentoActivity.class);
        intent.putExtra(SalaAtendimentoActivity.ROLE, role);
        intent.putExtra(SalaAtendimentoActivity.ID, item.getId());
        intent.putExtra(SalaAtendimentoActivity.CLIENT_ID, item.getClienteId());
        intent.putExtra(SalaAtendimentoActivity.CLIENT_NAME, item.getClienteNome());
        intent.putExtra(SalaAtendimentoActivity.PRO_ID, item.getProfissionalId());
        intent.putExtra(SalaAtendimentoActivity.PRO_NAME, item.getProfissionalNome());
        intent.putExtra(SalaAtendimentoActivity.MODALITY, item.getModalidade());
        intent.putExtra(SalaAtendimentoActivity.DATE, item.getDataHora());
        startActivity(intent);
    }

    private void estilizarBotaoMindU(Button button) {
        button.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#75689A")));
        button.setTextColor(Color.WHITE);
        button.setAllCaps(false);
        button.setTypeface(android.graphics.Typeface.create("sans-serif-medium", 0));
        button.setPadding(dp(16), dp(10), dp(16), dp(10));
    }

    private void escolherDataReagendamento(AgendamentoResponse item) {
        Calendar now = Calendar.getInstance();
        DatePickerDialog picker = new DatePickerDialog(this, (dialog, year, month, day) -> {
            Calendar date = Calendar.getInstance(); date.set(year, month, day);
            String dateText = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(date.getTime());
            RetrofitClient.getApiService(this).horarios(item.getProfissionalId(), dateText).enqueue(new Callback<List<String>>() {
                @Override public void onResponse(Call<List<String>> call, Response<List<String>> response) {
                    if (!response.isSuccessful() || response.body() == null || response.body().isEmpty()) { Toast.makeText(DashboardBeneficiario.this, "Sem horários disponíveis nesta data.", Toast.LENGTH_LONG).show(); return; }
                    List<String> slots = response.body(); String[] labels = new String[slots.size()];
                    for (int i = 0; i < slots.size(); i++) labels[i] = slots.get(i).length() >= 16 ? slots.get(i).substring(11, 16) : slots.get(i);
                    new android.app.AlertDialog.Builder(DashboardBeneficiario.this).setTitle("Escolha o novo horário").setItems(labels, (d, which) -> RetrofitClient.getApiService(DashboardBeneficiario.this).reagendar(item.getId(), new ReagendarRequest(slots.get(which))).enqueue(appointmentCallback("Consulta remarcada."))).show();
                }
                @Override public void onFailure(Call<List<String>> call, Throwable t) { Toast.makeText(DashboardBeneficiario.this, "Falha ao consultar horários.", Toast.LENGTH_LONG).show(); }
            });
        }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
        picker.getDatePicker().setMinDate(System.currentTimeMillis());
        picker.show();
    }

    private Callback<AgendamentoResponse> appointmentCallback(String success) {
        return new Callback<AgendamentoResponse>() {
            @Override public void onResponse(Call<AgendamentoResponse> call, Response<AgendamentoResponse> response) {
                if (response.isSuccessful()) { Toast.makeText(DashboardBeneficiario.this, success, Toast.LENGTH_LONG).show(); mostrarMinhasConsultas(); }
                else Toast.makeText(DashboardBeneficiario.this, response.code() == 409 ? "Esse horário foi ocupado. Escolha outra data." : "Não foi possível atualizar a consulta.", Toast.LENGTH_LONG).show();
            }
            @Override public void onFailure(Call<AgendamentoResponse> call, Throwable t) { Toast.makeText(DashboardBeneficiario.this, "Falha de conexão. Tente novamente.", Toast.LENGTH_LONG).show(); }
        };
    }

    private void configurarSalaOnline(View tela) {
        TextView status = tela.findViewById(R.id.txtStatusConexao);
        FrameLayout video = tela.findViewById(R.id.containerVideoEspecialista);
        TextView link = new TextView(this); link.setText("Buscando seu link de consulta…"); link.setGravity(android.view.Gravity.CENTER); link.setPadding(dp(16), dp(12), dp(16), dp(12));
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(-1, -2, android.view.Gravity.BOTTOM); video.addView(link, params);
        RetrofitClient.getApiService(this).meusAgendamentos().enqueue(new Callback<List<AgendamentoResponse>>() {
            @Override public void onResponse(Call<List<AgendamentoResponse>> call, Response<List<AgendamentoResponse>> response) {
                if (response.isSuccessful() && response.body() != null) for (AgendamentoResponse item : response.body()) {
                    if ("VIRTUAL".equalsIgnoreCase(item.getModalidade()) && item.getLinkOnline() != null && !item.getLinkOnline().trim().isEmpty()) {
                        status.setText("Link da consulta disponível"); link.setText("Toque para entrar na sala de " + item.getProfissionalNome()); link.setOnClickListener(v -> openSecureLink(item.getLinkOnline())); return;
                    }
                }
                link.setText("O link da consulta aparecerá aqui quando o profissional informar.");
            }
            @Override public void onFailure(Call<List<AgendamentoResponse>> call, Throwable t) { link.setText("Não foi possível consultar os links neste momento."); }
        });
    }

    private void openSecureLink(String value) {
        if (value == null || !value.startsWith("https://")) { Toast.makeText(this, "Link da sala inválido.", Toast.LENGTH_SHORT).show(); return; }
        try { startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(value))); }
        catch (Exception e) { Toast.makeText(this, "Não há aplicativo para abrir o link.", Toast.LENGTH_SHORT).show(); }
    }

    private TextView textLabel(String text, int size, boolean bold) {
        TextView view = new TextView(this); view.setText(text); view.setTextSize(size); view.setPadding(0, dp(8), 0, dp(8));
        if (bold) view.setTypeface(null, android.graphics.Typeface.BOLD); return view;
    }

    private int dp(int value) { return (int) (value * getResources().getDisplayMetrics().density + 0.5f); }
}
