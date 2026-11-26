
package com.example.mindu;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mindu.adapter.ProfissionaisAdapter;
import com.example.mindu.api.RetrofitClient;
import com.example.mindu.api.login.MinhaContaResponse;
import com.example.mindu.api.profissional.ProfissionalResponse;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import eightbitlab.com.blurview.BlurTarget;
import eightbitlab.com.blurview.BlurView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardBeneficiario extends AppCompatActivity {

    private FrameLayout containerBeneficiario;

    private ImageView navInicio;
    private ImageView navAgendamentos;
    private ImageView navImediato;
    private ImageView navPerfil;

    private BlurTarget blurTargetDashboard;

    private ProfissionaisAdapter profissionaisAdapter;

    private ProfissionalResponse profissionalSelecionado;

    private String nomeBeneficiario = "";

    // Dados do agendamento
    private String dataSelecionada;
    private String horarioSelecionado;
    private String modalidadeSelecionada;

    private Button botaoHorarioSelecionado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard_beneficiario);

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

        navImediato.setOnClickListener(v ->
                abrirTela(R.layout.content_sala_online));

        navPerfil.setOnClickListener(v ->
                abrirTela(R.layout.content_perfil_beneficiario));
    }

    // Abre as telas dentro do container
    private void abrirTela(int layout) {

        View tela = LayoutInflater.from(this)
                .inflate(layout, containerBeneficiario, false);

        containerBeneficiario.removeAllViews();
        containerBeneficiario.addView(tela);

        if (layout == R.layout.content_inicio_beneficiario) {

            configurarBlurViews(tela, blurTargetDashboard);
            atualizarSaudacao();
            configurarCardsInicio(tela);

        } else if (layout == R.layout.content_agendamento_beneficiario) {

            configurarBlurViews(tela, blurTargetDashboard);
            configurarAgendamentos(tela);

        } else if (layout == R.layout.content_perfil_beneficiario) {

            configurarBlurViews(tela, blurTargetDashboard);
            configurarPerfilBeneficiario(tela);

        } else if (layout == R.layout.content_perfil_profissional) {

            configurarBlurViews(tela, blurTargetDashboard);
            configurarPerfilProfissional(tela);

        } else if (layout == R.layout.content_escolher_horario) {

            configurarBlurViews(tela, blurTargetDashboard);
            configurarTelaAgendamento(tela);
        }
    }

    // Configura os cards da tela inicial
    private void configurarCardsInicio(View tela) {

        View cardAnsiedade = tela.findViewById(
                R.id.blurConteudoAnsiedade
        );

        if (cardAnsiedade != null) {
            cardAnsiedade.setOnClickListener(v ->
                    abrirDialogConteudo(
                            R.layout.dialog_conteudo_ansiedade,
                            R.id.btnFecharConteudoAnsiedade
                    )
            );
        }

        View cardBurnout = tela.findViewById(
                R.id.blurConteudoBurnout
        );

        if (cardBurnout != null) {
            cardBurnout.setOnClickListener(v ->
                    abrirDialogConteudo(
                            R.layout.dialog_conteudo_burnout,
                            R.id.btnFecharConteudoBurnout
                    )
            );
        }

        View cardAutocuidado = tela.findViewById(
                R.id.blurConteudoAutocuidado
        );

        if (cardAutocuidado != null) {
            cardAutocuidado.setOnClickListener(v ->
                    abrirDialogConteudo(
                            R.layout.dialog_conteudo_autocuidado,
                            R.id.btnFecharConteudoAutocuidado
                    )
            );
        }

        View cardSono = tela.findViewById(
                R.id.blurConteudoSono
        );

        if (cardSono != null) {
            cardSono.setOnClickListener(v ->
                    abrirDialogConteudo(
                            R.layout.dialog_conteudo_sono,
                            R.id.btnFecharConteudoSono
                    )
            );
        }

        // Acesso rápido: agendamento
        View cardAgendamento = tela.findViewById(
                R.id.blurAgendamentoRapido
        );

        View botaoAgendamento = tela.findViewById(
                R.id.btnAgendamentoRapido
        );

        if (cardAgendamento != null) {
            cardAgendamento.setOnClickListener(v ->
                    abrirTela(R.layout.content_agendamento_beneficiario)
            );
        }

        if (botaoAgendamento != null) {
            botaoAgendamento.setOnClickListener(v ->
                    abrirTela(R.layout.content_agendamento_beneficiario)
            );
        }

        // Botão Agendar sessão
        Button btnAgendarSessao = tela.findViewById(
                R.id.btnAgendarSessao
        );

        if (btnAgendarSessao != null) {
            btnAgendarSessao.setOnClickListener(v ->
                    abrirTela(R.layout.content_agendamento_beneficiario)
            );
        }

        // Botão de outros conteúdos
        Button btnOutrosConteudos = tela.findViewById(
                R.id.btnVerOutrosConteudos
        );

        if (btnOutrosConteudos != null) {
            btnOutrosConteudos.setOnClickListener(v ->
                    abrirTelaEmBreve(
                            "Outros conteúdos",
                            "Estamos preparando novos conteúdos sobre saúde mental, bem-estar e autocuidado para você."
                    )
            );
        }
    }

    // Configura os botões do perfil do beneficiário
    private void configurarPerfilBeneficiario(View tela) {

        View cardMinhaConta = tela.findViewById(
                R.id.cardMinhaContaBeneficiario
        );

        View cardPreferencias = tela.findViewById(
                R.id.cardPreferenciasBeneficiario
        );

        View cardPrivacidade = tela.findViewById(
                R.id.cardPrivacidadeBeneficiario
        );

        // Minha conta
        if (cardMinhaConta != null) {
            cardMinhaConta.setOnClickListener(v ->
                    abrirTelaEmBreve(
                            "Minha conta",
                            "Estamos preparando uma área para você consultar e atualizar suas informações pessoais e dados cadastrais."
                    )
            );
        }

        // Preferências
        if (cardPreferencias != null) {
            cardPreferencias.setOnClickListener(v ->
                    abrirTelaEmBreve(
                            "Preferências",
                            "Em breve, você poderá personalizar suas preferências de atendimento e recursos de acessibilidade."
                    )
            );
        }

        // Privacidade e segurança
        if (cardPrivacidade != null) {
            cardPrivacidade.setOnClickListener(v ->
                    abrirTelaEmBreve(
                            "Privacidade e segurança",
                            "Estamos preparando uma área para você gerenciar sua senha, segurança e proteção dos seus dados."
                    )
            );
        }
    }

    // Abre o layout genérico de funcionalidades em desenvolvimento
    private void abrirTelaEmBreve(String titulo, String mensagem) {

        View tela = LayoutInflater.from(this)
                .inflate(
                        R.layout.content_em_breve,
                        containerBeneficiario,
                        false
                );

        TextView txtTitulo = tela.findViewById(
                R.id.txtTituloAvisoHonorarios
        );

        TextView txtMensagem = tela.findViewById(
                R.id.txtMensagemAvisoHonorarios
        );

        if (txtTitulo != null) {
            txtTitulo.setText(titulo);
        }

        if (txtMensagem != null) {
            txtMensagem.setText(mensagem);
        }

        containerBeneficiario.removeAllViews();
        containerBeneficiario.addView(tela);
    }

    // Abre um conteúdo educativo em uma janela de diálogo
    private void abrirDialogConteudo(
            int layout,
            int idBotaoFechar
    ) {

        View conteudo = LayoutInflater.from(this)
                .inflate(layout, null, false);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(conteudo)
                .create();

        View btnFechar = conteudo.findViewById(
                idBotaoFechar
        );

        if (btnFechar != null) {
            btnFechar.setOnClickListener(v -> dialog.dismiss());
        }

        dialog.show();

        Window window = dialog.getWindow();

        if (window != null) {

            window.setBackgroundDrawableResource(
                    android.R.color.transparent
            );

            window.setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT
            );
        }
    }

    // Configura todos os BlurViews encontrados na tela
    private void configurarBlurViews(View view, BlurTarget target) {

        if (view == null || target == null) {
            return;
        }

        if (view instanceof BlurView) {

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

                                atualizarSaudacao();
                            }
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<MinhaContaResponse> call,
                            Throwable t
                    ) {

                        Toast.makeText(
                                DashboardBeneficiario.this,
                                "Não foi possível carregar seu nome.",
                                Toast.LENGTH_SHORT
                        ).show();
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

            if (nomeBeneficiario.isEmpty()) {

                txtSaudacao.setText("Olá!");

            } else {

                txtSaudacao.setText(
                        "Olá, " + nomeBeneficiario + "!"
                );
            }
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

                txtNenhum.setVisibility(
                        profissionaisAdapter.getItemCount() == 0
                                ? View.VISIBLE
                                : View.GONE
                );
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        carregarProfissionais(txtNenhum);
    }

    // Busca os profissionais disponíveis
    private void carregarProfissionais(TextView txtNenhum) {

        RetrofitClient.getApiService(this)
                .buscar(null, null, null)
                .enqueue(new Callback<List<ProfissionalResponse>>() {

                    @Override
                    public void onResponse(
                            Call<List<ProfissionalResponse>> call,
                            Response<List<ProfissionalResponse>> response
                    ) {

                        if (response.isSuccessful()
                                && response.body() != null) {

                            List<ProfissionalResponse> profissionais =
                                    response.body();

                            profissionaisAdapter.atualizarLista(
                                    profissionais
                            );

                            if (profissionais.isEmpty()) {

                                txtNenhum.setText(
                                        "Nenhum profissional disponível no momento."
                                );

                                txtNenhum.setVisibility(View.VISIBLE);

                            } else {

                                txtNenhum.setVisibility(View.GONE);
                            }

                        } else {

                            txtNenhum.setText(
                                    "Não foi possível carregar os profissionais."
                            );

                            txtNenhum.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<List<ProfissionalResponse>> call,
                            Throwable t
                    ) {

                        txtNenhum.setText(
                                "Erro de conexão. Tente novamente."
                        );

                        txtNenhum.setVisibility(View.VISIBLE);

                        Toast.makeText(
                                DashboardBeneficiario.this,
                                "Erro ao carregar profissionais: "
                                        + t.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
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

        View btnAgendar = tela.findViewById(
                R.id.btnAgendarSessaoProfissional
        );

        btnAgendar.setOnClickListener(v ->
                abrirTela(R.layout.content_escolher_horario)
        );
    }

    // Configura a tela de escolha de horário
    private void configurarTelaAgendamento(View tela) {

        if (profissionalSelecionado == null) {

            Toast.makeText(
                    this,
                    "Nenhum profissional selecionado.",
                    Toast.LENGTH_SHORT
            ).show();

            abrirTela(R.layout.content_agendamento_beneficiario);
            return;
        }

        TextView txtNome = tela.findViewById(
                R.id.txtNomeEspecialistaSelecionado
        );

        TextView txtEspecialidade = tela.findViewById(
                R.id.txtEspecialidadeSelecionada
        );

        CalendarView calendario = tela.findViewById(
                R.id.calendarAgendamento
        );

        GridLayout gridHorarios = tela.findViewById(
                R.id.gridHorarios
        );

        RadioGroup radioModalidade = tela.findViewById(
                R.id.radioModalidade
        );

        txtNome.setText(profissionalSelecionado.getNome());

        String tipo = profissionalSelecionado.getTipo();

        txtEspecialidade.setText(
                tipo != null && !tipo.trim().isEmpty()
                        ? tipo
                        : "Profissional"
        );

        calendario.setMinDate(System.currentTimeMillis());

        // Reseta seleções anteriores
        horarioSelecionado = null;
        modalidadeSelecionada = null;
        botaoHorarioSelecionado = null;

        // Define a data inicial
        Calendar dataAtual = Calendar.getInstance();

        dataSelecionada = String.format(
                Locale.ROOT,
                "%04d-%02d-%02d",
                dataAtual.get(Calendar.YEAR),
                dataAtual.get(Calendar.MONTH) + 1,
                dataAtual.get(Calendar.DAY_OF_MONTH)
        );

        carregarHorarios(dataSelecionada, gridHorarios);

        // Atualiza os horários quando a data mudar
        calendario.setOnDateChangeListener(
                (view, year, month, dayOfMonth) -> {

                    dataSelecionada = String.format(
                            Locale.ROOT,
                            "%04d-%02d-%02d",
                            year,
                            month + 1,
                            dayOfMonth
                    );

                    horarioSelecionado = null;
                    botaoHorarioSelecionado = null;

                    carregarHorarios(
                            dataSelecionada,
                            gridHorarios
                    );
                }
        );

        configurarModalidades(radioModalidade);
    }

    // Busca os horários disponíveis no backend
    private void carregarHorarios(
            String data,
            GridLayout gridHorarios
    ) {

        gridHorarios.removeAllViews();

        TextView carregando = new TextView(this);

        carregando.setText("Carregando horários...");
        carregando.setTextColor(Color.parseColor("#75689A"));

        gridHorarios.addView(carregando);

        RetrofitClient.getApiService(this)
                .horarios(
                        profissionalSelecionado.getId(),
                        data
                )
                .enqueue(new Callback<List<String>>() {

                    @Override
                    public void onResponse(
                            Call<List<String>> call,
                            Response<List<String>> response
                    ) {

                        gridHorarios.removeAllViews();

                        if (response.isSuccessful()
                                && response.body() != null) {

                            List<String> horarios = response.body();

                            if (horarios.isEmpty()) {

                                mostrarMensagemHorarios(
                                        gridHorarios,
                                        "Nenhum horário disponível nesta data."
                                );

                                return;
                            }

                            for (String horario : horarios) {

                                if (horario == null
                                        || horario.trim().isEmpty()) {
                                    continue;
                                }

                                Button botao = new Button(
                                        DashboardBeneficiario.this
                                );

                                String horarioExibido =
                                        horario.length() >= 5
                                                ? horario.substring(0, 5)
                                                : horario;

                                botao.setText(horarioExibido);
                                botao.setAllCaps(false);
                                botao.setTextSize(13);

                                botao.setTextColor(
                                        Color.parseColor("#75689A")
                                );

                                botao.setBackgroundTintList(
                                        ColorStateList.valueOf(
                                                Color.parseColor("#F5F3F7")
                                        )
                                );

                                GridLayout.LayoutParams params =
                                        new GridLayout.LayoutParams();

                                params.width = 0;

                                params.height = (int) (
                                        48 * getResources()
                                                .getDisplayMetrics()
                                                .density
                                );

                                params.columnSpec = GridLayout.spec(
                                        GridLayout.UNDEFINED,
                                        1f
                                );

                                int margem = (int) (
                                        4 * getResources()
                                                .getDisplayMetrics()
                                                .density
                                );

                                params.setMargins(
                                        margem,
                                        margem,
                                        margem,
                                        margem
                                );

                                botao.setLayoutParams(params);

                                botao.setOnClickListener(v -> {

                                    if (botaoHorarioSelecionado != null) {

                                        botaoHorarioSelecionado
                                                .setBackgroundTintList(
                                                        ColorStateList.valueOf(
                                                                Color.parseColor("#F5F3F7")
                                                        )
                                                );
                                    }

                                    horarioSelecionado = horario;

                                    botaoHorarioSelecionado = botao;

                                    botao.setBackgroundTintList(
                                            ColorStateList.valueOf(
                                                    Color.parseColor("#DCD3F0")
                                            )
                                    );
                                });

                                gridHorarios.addView(botao);
                            }

                            if (gridHorarios.getChildCount() == 0) {

                                mostrarMensagemHorarios(
                                        gridHorarios,
                                        "Nenhum horário disponível nesta data."
                                );
                            }

                        } else {

                            mostrarMensagemHorarios(
                                    gridHorarios,
                                    "Não foi possível carregar os horários."
                            );
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<List<String>> call,
                            Throwable t
                    ) {

                        gridHorarios.removeAllViews();

                        mostrarMensagemHorarios(
                                gridHorarios,
                                "Erro de conexão ao buscar horários."
                        );

                        Toast.makeText(
                                DashboardBeneficiario.this,
                                "Erro: " + t.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }

    // Exibe mensagens na área de horários
    private void mostrarMensagemHorarios(
            GridLayout gridHorarios,
            String mensagem
    ) {

        TextView texto = new TextView(this);

        texto.setText(mensagem);
        texto.setTextColor(Color.parseColor("#777080"));
        texto.setTextSize(13);
        texto.setPadding(8, 12, 8, 12);

        gridHorarios.addView(texto);
    }

    // Configura as modalidades oferecidas pelo profissional
    private void configurarModalidades(RadioGroup radioModalidade) {

        radioModalidade.removeAllViews();

        List<String> modalidades =
                profissionalSelecionado.getModalidades();

        if (modalidades == null || modalidades.isEmpty()) {

            radioModalidade.setVisibility(View.GONE);

            Toast.makeText(
                    this,
                    "Este profissional não possui modalidades informadas.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        radioModalidade.setVisibility(View.VISIBLE);

        for (String modalidade : modalidades) {

            if (modalidade == null) {
                continue;
            }

            String valor = modalidade.trim().toUpperCase(Locale.ROOT);

            String nomeExibicao;

            switch (valor) {

                case "VIRTUAL":
                    nomeExibicao = "Online";
                    break;

                case "PRESENCIAL":
                    nomeExibicao = "Presencial";
                    break;

                case "HIBRIDO":
                    nomeExibicao = "Híbrido";
                    break;

                default:
                    continue;
            }

            RadioButton radio = new RadioButton(this);

            radio.setId(View.generateViewId());
            radio.setText(nomeExibicao);
            radio.setTextColor(Color.parseColor("#75689A"));
            radio.setTextSize(14);
            radio.setTag(valor);

            radioModalidade.addView(radio);
        }

        if (radioModalidade.getChildCount() == 0) {

            radioModalidade.setVisibility(View.GONE);
            return;
        }

        // Seleciona a primeira modalidade disponível
        radioModalidade.check(
                radioModalidade.getChildAt(0).getId()
        );

        radioModalidade.setOnCheckedChangeListener(
                (group, checkedId) -> {

                    RadioButton selecionado =
                            group.findViewById(checkedId);

                    if (selecionado != null) {

                        modalidadeSelecionada =
                                selecionado.getTag().toString();
                    }
                }
        );

        RadioButton primeiro = radioModalidade.findViewById(
                radioModalidade.getCheckedRadioButtonId()
        );

        if (primeiro != null) {

            modalidadeSelecionada =
                    primeiro.getTag().toString();
        }
    }
}
