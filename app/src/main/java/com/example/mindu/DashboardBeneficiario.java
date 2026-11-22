
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

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mindu.adapter.ProfissionaisAdapter;
import com.example.mindu.api.RetrofitClient;
import com.example.mindu.api.login.MinhaContaResponse;
import com.example.mindu.api.profissional.ProfissionalResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!SessionManager.guard(this, "CLIENTE")) return;
        setContentView(R.layout.activity_dashboard_beneficiario);
        fabMilaBeneficiario = findViewById(R.id.fabMilaBeneficiario);
        fabMilaBeneficiario.setOnClickListener(v ->
                startActivity(new android.content.Intent(DashboardBeneficiario.this, ChatMilaActivity.class)));

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
        if (fabMilaBeneficiario != null) {
            fabMilaBeneficiario.setVisibility(
                    layout == R.layout.content_inicio_beneficiario ? View.VISIBLE : View.GONE);
        }

        if (layout == R.layout.content_perfil_beneficiario) {
            View sair = tela.findViewById(R.id.cardSairBeneficiario);
            if (sair != null) sair.setOnClickListener(v -> SessionManager.logout(this));
        }

        if (layout == R.layout.content_inicio_beneficiario) {

            configurarBlurViews(tela, blurTargetDashboard);
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

        // Abre a tela de escolha de horário
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
        RadioGroup radioModalidade = tela.findViewById(
                R.id.radioModalidade
        );

        RadioButton radioOnline = tela.findViewById(
                R.id.radioOnline
        );

        RadioButton radioPresencial = tela.findViewById(
                R.id.radioPresencial
        );

        List<String> modalidades =
                profissionalSelecionado.getModalidades();

        boolean ofereceOnline = false;
        boolean oferecePresencial = false;

        if (modalidades != null) {

            for (String modalidade : modalidades) {

                if (modalidade == null) {
                    continue;
                }

                String valor = modalidade.trim().toLowerCase(Locale.ROOT);

                if (valor.contains("online")) {
                    ofereceOnline = true;
                }

                if (valor.contains("presencial")) {
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

            Toast.makeText(
                    this,
                    "Este profissional não possui modalidades informadas.",
                    Toast.LENGTH_LONG
            ).show();
        }
    }
}
