package com.example.mindu;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mindu.adapter.ColaboradoresAdapter;
import com.example.mindu.adapter.MensagemAdapter;
import com.example.mindu.api.ApiService;
import com.example.mindu.api.RetrofitClient;
import com.example.mindu.api.chat.ChatTurn;
import com.example.mindu.api.cliente.ClienteResponse;
import com.example.mindu.api.dashboard.DashboardResponse;
import com.example.mindu.api.login.MinhaContaResponse;
import com.example.mindu.api.matriculas.MatriculaResponse;

import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import eightbitlab.com.blurview.BlurTarget;
import eightbitlab.com.blurview.BlurView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardEmpresa extends AppCompatActivity {

    private static final int REQUEST_SALVAR_PDF = 1001;

    private View containerEmpresa;

    private ImageView navInicioEmpresa;
    private ImageView navColaboradoresEmpresa;
    private ImageView navRelatoriosEmpresa;
    private ImageView navPerfilEmpresa;

    private RecyclerView rvColaboradoresEmpresa;
    private EditText edtBuscarColaboradorEmpresa;
    private Button btnPendentesEmpresa;
    private Button btnAprovadosEmpresa;
    private TextView txtColaboradoresEmpresaVazio;

    private ApiService apiService;
    private ColaboradoresAdapter adapter;

    private final List<ClienteResponse> listaCompleta = new ArrayList<>();
    private final List<ClienteResponse> listaFiltrada = new ArrayList<>();

    private String statusSelecionado = "AGUARDANDO_APROVACAO";
    private String buscaAtual = "";

    private FrameLayout fabMila;

    private RecyclerView rvMensagensMila;
    private EditText edtMensagemMila;
    private ImageView btnEnviarMila;

    private MensagemAdapter mensagemAdapter;
    private final List<ChatTurn> mensagensMila = new ArrayList<>();

    // Dados dos relatórios
    private boolean relatorioCarregado = false;

    private int relTotalMatriculas;
    private int relMatriculasUtilizadas;
    private int relMatriculasDisponiveis;
    private int relPercentualUso;

    private int relTotalColaboradores;
    private int relColaboradoresAprovados;
    private int relColaboradoresAtivos;
    private int relBeneficiarios;
    private int relPercentualAdesao;

    private byte[] pdfPendente;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard_empresa);

        fabMila = findViewById(R.id.fabMila);

        apiService = RetrofitClient.getApiService(this);

        containerEmpresa = findViewById(R.id.containerEmpresa);

        navInicioEmpresa = findViewById(R.id.navInicioEmpresa);
        navColaboradoresEmpresa = findViewById(R.id.navColaboradoresEmpresa);
        navRelatoriosEmpresa = findViewById(R.id.navRelatoriosEmpresa);
        navPerfilEmpresa = findViewById(R.id.navPerfilEmpresa);

        fabMila.setOnClickListener(v -> {
            mostrarTela(R.layout.activity_chat_mila);
        });

        navInicioEmpresa.setOnClickListener(v -> {
            atualizarNavegacao(navInicioEmpresa);
            mostrarTela(R.layout.content_inicio_empresa);
        });

        navColaboradoresEmpresa.setOnClickListener(v -> {
            atualizarNavegacao(navColaboradoresEmpresa);
            mostrarTela(R.layout.content_colaboradores_empresa);
        });

        navRelatoriosEmpresa.setOnClickListener(v -> {
            atualizarNavegacao(navRelatoriosEmpresa);
            mostrarTela(R.layout.content_relatorios_empresa);
        });

        navPerfilEmpresa.setOnClickListener(v -> {
            atualizarNavegacao(navPerfilEmpresa);
            mostrarTela(R.layout.content_perfil_empresa);
        });

        atualizarNavegacao(navInicioEmpresa);
        mostrarTela(R.layout.content_inicio_empresa);
    }

    private void atualizarNavegacao(ImageView selecionado) {

        int corAtiva = Color.parseColor("#FFFFFF");
        int corInativa = Color.parseColor("#D4CDE9");

        navInicioEmpresa.setColorFilter(corInativa);
        navColaboradoresEmpresa.setColorFilter(corInativa);
        navRelatoriosEmpresa.setColorFilter(corInativa);
        navPerfilEmpresa.setColorFilter(corInativa);

        selecionado.setColorFilter(corAtiva);
    }

    private void mostrarTela(int layout) {

        View tela = getLayoutInflater().inflate(
                layout,
                (android.view.ViewGroup) containerEmpresa,
                false
        );

        ((android.view.ViewGroup) containerEmpresa).removeAllViews();
        ((android.view.ViewGroup) containerEmpresa).addView(tela);

        fabMila.setVisibility(
                layout == R.layout.activity_chat_mila
                        ? View.GONE
                        : View.VISIBLE
        );

        if (layout == R.layout.content_colaboradores_empresa) {

            configurarTelaColaboradores(tela);

        } else if (layout == R.layout.content_inicio_empresa) {

            BlurTarget blurTarget = findViewById(
                    R.id.blurTargetDashboardEmpresa
            );

            aplicarEfeitoVidro(
                    tela.findViewById(R.id.blurUsoBeneficioEmpresa),
                    blurTarget
            );

            aplicarEfeitoVidro(
                    tela.findViewById(R.id.blurSessoesEmpresa),
                    blurTarget
            );

            aplicarEfeitoVidro(
                    tela.findViewById(R.id.blurAderenciaEmpresa),
                    blurTarget
            );

            configurarTelaInicioEmpresa(tela);

        } else if (layout == R.layout.content_relatorios_empresa) {

            BlurTarget blurTarget = findViewById(
                    R.id.blurTargetDashboardEmpresa
            );

            aplicarEfeitoVidro(
                    tela.findViewById(R.id.blurRelatorioAderencia),
                    blurTarget
            );

            aplicarEfeitoVidro(
                    tela.findViewById(R.id.blurRelatorioEquipe),
                    blurTarget
            );

            configurarTelaRelatoriosEmpresa(tela);

        } else if (layout == R.layout.content_perfil_empresa) {

            BlurTarget blurTarget = findViewById(
                    R.id.blurTargetDashboardEmpresa
            );

            aplicarEfeitoVidro(
                    tela.findViewById(R.id.cardIdentificacaoEmpresaPerfil),
                    blurTarget
            );

            aplicarEfeitoVidro(
                    tela.findViewById(R.id.cardDadosContaEmpresaPerfil),
                    blurTarget
            );

            aplicarEfeitoVidro(
                    tela.findViewById(R.id.cardSuporteEmpresaPerfil),
                    blurTarget
            );

            configurarTelaPerfilEmpresa(tela);

        } else if (layout == R.layout.activity_chat_mila) {

            configurarChatMila(tela);
        }
    }

    // =========================================================
    // CHAT MILA
    // =========================================================

    private void configurarChatMila(View tela) {

        rvMensagensMila = tela.findViewById(R.id.rvMensagensMila);
        edtMensagemMila = tela.findViewById(R.id.edtMensagemMila);
        btnEnviarMila = tela.findViewById(R.id.btnEnviarMila);

        LinearLayoutManager layoutManager =
                new LinearLayoutManager(this);

        layoutManager.setStackFromEnd(true);

        rvMensagensMila.setLayoutManager(layoutManager);

        mensagemAdapter = new MensagemAdapter(mensagensMila);
        rvMensagensMila.setAdapter(mensagemAdapter);
    }

    // =========================================================
    // DASHBOARD INICIAL
    // =========================================================

    private void configurarTelaInicioEmpresa(View tela) {

        TextView txtUsoBeneficio =
                tela.findViewById(R.id.txtUsoBeneficio);

        TextView txtVariacaoUso =
                tela.findViewById(R.id.txtVariacaoUso);

        TextView txtTotalSessoes =
                tela.findViewById(R.id.txtTotalSessoes);

        TextView txtAderenciaEmpresa =
                tela.findViewById(R.id.txtAderenciaEmpresa);

        TextView txtDescricaoMatriculas =
                tela.findViewById(R.id.txtDescricaoMatriculas);

        ProgressBar progresso =
                tela.findViewById(R.id.progressoAderenciaEmpresa);

        apiService.meuDashboard().enqueue(
                new Callback<DashboardResponse>() {

                    @Override
                    public void onResponse(
                            Call<DashboardResponse> call,
                            Response<DashboardResponse> response) {

                        Log.d("DASHBOARD_MINDU",
                                "Código HTTP: " + response.code());

                        if (response.isSuccessful() && response.body() != null) {

                            DashboardResponse dashboard = response.body();

                            Log.d("DASHBOARD_MINDU",
                                    "Resposta: " + new Gson().toJson(dashboard));

                            int disponiveis =
                                    dashboard.getMatriculasDisponiveis();

                            int usadas =
                                    dashboard.getMatriculasUsadas();

                            int total = disponiveis + usadas;

                            int percentual = total > 0
                                    ? Math.round(usadas * 100f / total)
                                    : 0;

                            txtUsoBeneficio.setText(percentual + "%");

                            txtVariacaoUso.setText(
                                    usadas + " de " + total + " matrículas utilizadas"
                            );

                            txtTotalSessoes.setText(String.valueOf(usadas));

                            txtAderenciaEmpresa.setText(
                                    String.valueOf(disponiveis)
                            );

                            txtDescricaoMatriculas.setText(
                                    percentual + "% das matrículas estão em uso."
                            );

                            progresso.setProgress(percentual);

                        } else {

                            Toast.makeText(
                                    DashboardEmpresa.this,
                                    "Não foi possível carregar os indicadores.",
                                    Toast.LENGTH_SHORT
                            ).show();

                            try {
                                if (response.errorBody() != null) {
                                    Log.e("DASHBOARD_MINDU",
                                            response.errorBody().string());
                                }
                            } catch (IOException e) {
                                Log.e("DASHBOARD_MINDU",
                                        "Erro ao ler resposta", e);
                            }
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<DashboardResponse> call,
                            Throwable t) {

                        Log.e("DASHBOARD_MINDU",
                                "Falha ao carregar dashboard", t);

                        Toast.makeText(
                                DashboardEmpresa.this,
                                "Erro ao carregar o dashboard.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    // =========================================================
    // RELATÓRIOS RH
    // =========================================================

    private void configurarTelaRelatoriosEmpresa(View tela) {

        TextView txtTotal =
                tela.findViewById(R.id.txtRelatorioTotal);

        TextView txtUtilizadas =
                tela.findViewById(R.id.txtRelatorioUtilizadas);

        TextView txtDisponiveis =
                tela.findViewById(R.id.txtRelatorioDisponiveis);

        TextView txtPercentual =
                tela.findViewById(R.id.txtRelatorioPercentual);

        TextView txtDescricao =
                tela.findViewById(R.id.txtRelatorioDescricao);

        TextView txtTotalColaboradores =
                tela.findViewById(R.id.txtRelatorioTotalColaboradores);

        TextView txtAprovados =
                tela.findViewById(R.id.txtRelatorioColaboradores);

        TextView txtAtivos =
                tela.findViewById(R.id.txtRelatorioAtivos);

        TextView txtBeneficiarios =
                tela.findViewById(R.id.txtRelatorioBeneficiarios);

        TextView txtAdesao =
                tela.findViewById(R.id.txtRelatorioAdesao);

        TextView txtDescricaoEquipe =
                tela.findViewById(R.id.txtRelatorioDescricaoEquipe);

        ProgressBar progressoMatriculas =
                tela.findViewById(R.id.progressoRelatorio);

        ProgressBar progressoEquipe =
                tela.findViewById(R.id.progressoAdesaoEquipe);

        Button btnExportar =
                tela.findViewById(R.id.btnExportarRelatorioPDF);

        relatorioCarregado = false;

        if (btnExportar != null) {
            btnExportar.setEnabled(false);
            btnExportar.setAlpha(0.5f);

            btnExportar.setOnClickListener(v -> exportarRelatorioPDF());
        }

        // Consulta os colaboradores da empresa.
        apiService.listarClientes(null).enqueue(
                new Callback<List<ClienteResponse>>() {

                    @Override
                    public void onResponse(
                            Call<List<ClienteResponse>> call,
                            Response<List<ClienteResponse>> response) {

                        if (!response.isSuccessful() || response.body() == null) {

                            Toast.makeText(
                                    DashboardEmpresa.this,
                                    "Não foi possível carregar os colaboradores.",
                                    Toast.LENGTH_SHORT
                            ).show();

                            return;
                        }

                        List<ClienteResponse> clientes = response.body();

                        relTotalColaboradores = clientes.size();
                        relColaboradoresAprovados = 0;
                        relColaboradoresAtivos = 0;

                        Set<String> idsElegiveis = new HashSet<>();

                        for (ClienteResponse cliente : clientes) {

                            boolean aprovado = "APROVADO".equalsIgnoreCase(
                                    cliente.getStatus()
                            );

                            if (aprovado) {
                                relColaboradoresAprovados++;
                            }

                            if (aprovado && cliente.isAtivo()) {

                                relColaboradoresAtivos++;

                                if (cliente.getId() != null) {
                                    idsElegiveis.add(cliente.getId());
                                }
                            }
                        }

                        txtTotalColaboradores.setText(
                                String.valueOf(relTotalColaboradores)
                        );

                        txtAprovados.setText(
                                String.valueOf(relColaboradoresAprovados)
                        );

                        txtAtivos.setText(
                                String.valueOf(relColaboradoresAtivos)
                        );

                        // Consulta as matrículas da empresa.
                        apiService.minhasMatriculas().enqueue(
                                new Callback<List<MatriculaResponse>>() {

                                    @Override
                                    public void onResponse(
                                            Call<List<MatriculaResponse>> call,
                                            Response<List<MatriculaResponse>> response) {

                                        if (!response.isSuccessful()
                                                || response.body() == null) {

                                            Toast.makeText(
                                                    DashboardEmpresa.this,
                                                    "Não foi possível carregar as matrículas.",
                                                    Toast.LENGTH_SHORT
                                            ).show();

                                            return;
                                        }

                                        List<MatriculaResponse> matriculas =
                                                response.body();

                                        relTotalMatriculas = matriculas.size();
                                        relMatriculasUtilizadas = 0;

                                        Set<String> beneficiarios = new HashSet<>();

                                        for (MatriculaResponse matricula : matriculas) {

                                            if (matricula.utilizada) {

                                                relMatriculasUtilizadas++;

                                                String beneficiarioId =
                                                        matricula.beneficiarioId;

                                                if (beneficiarioId != null
                                                        && idsElegiveis.contains(beneficiarioId)) {

                                                    beneficiarios.add(beneficiarioId);
                                                }
                                            }
                                        }

                                        relMatriculasDisponiveis =
                                                relTotalMatriculas - relMatriculasUtilizadas;

                                        relBeneficiarios = beneficiarios.size();

                                        relPercentualUso = relTotalMatriculas > 0
                                                ? Math.round(
                                                relMatriculasUtilizadas * 100f
                                                        / relTotalMatriculas)
                                                : 0;

                                        relPercentualAdesao = relColaboradoresAtivos > 0
                                                ? Math.round(
                                                relBeneficiarios * 100f
                                                        / relColaboradoresAtivos)
                                                : 0;

                                        // CARD DE MATRÍCULAS

                                        txtTotal.setText(
                                                String.valueOf(relTotalMatriculas)
                                        );

                                        txtUtilizadas.setText(
                                                relMatriculasUtilizadas + " utilizadas"
                                        );

                                        txtDisponiveis.setText(
                                                relMatriculasDisponiveis + " disponíveis"
                                        );

                                        txtPercentual.setText(
                                                relPercentualUso + "% de utilização"
                                        );

                                        txtDescricao.setText(
                                                relMatriculasUtilizadas + " de "
                                                        + relTotalMatriculas
                                                        + " matrículas estão em uso."
                                        );

                                        progressoMatriculas.setProgress(
                                                relPercentualUso
                                        );

                                        // CARD DA EQUIPE

                                        txtBeneficiarios.setText(
                                                String.valueOf(relBeneficiarios)
                                        );

                                        txtAdesao.setText(
                                                relPercentualAdesao + "% de participação"
                                        );

                                        if (relColaboradoresAtivos > 0) {

                                            txtDescricaoEquipe.setText(
                                                    relBeneficiarios + " de "
                                                            + relColaboradoresAtivos
                                                            + " colaboradores aprovados e ativos "
                                                            + "utilizaram o benefício."
                                            );

                                        } else {

                                            txtDescricaoEquipe.setText(
                                                    "Não há colaboradores aprovados e ativos "
                                                            + "para calcular a participação."
                                            );
                                        }

                                        progressoEquipe.setProgress(
                                                relPercentualAdesao
                                        );

                                        relatorioCarregado = true;

                                        if (btnExportar != null) {
                                            btnExportar.setEnabled(true);
                                            btnExportar.setAlpha(1f);
                                        }

                                    }

                                    @Override
                                    public void onFailure(
                                            Call<List<MatriculaResponse>> call,
                                            Throwable t) {

                                        Log.e("RELATORIOS_MINDU",
                                                "Erro nas matrículas", t);

                                        Toast.makeText(
                                                DashboardEmpresa.this,
                                                "Erro ao carregar as matrículas.",
                                                Toast.LENGTH_SHORT
                                        ).show();
                                    }
                                });
                    }

                    @Override
                    public void onFailure(
                            Call<List<ClienteResponse>> call,
                            Throwable t) {

                        Log.e("RELATORIOS_MINDU",
                                "Erro nos colaboradores", t);

                        Toast.makeText(
                                DashboardEmpresa.this,
                                "Erro ao carregar os colaboradores.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    // =========================================================
    // EXPORTAR PDF
    // =========================================================

    private void exportarRelatorioPDF() {

        if (!relatorioCarregado) {

            Toast.makeText(
                    this,
                    "Aguarde o carregamento dos indicadores.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        PdfDocument documento = new PdfDocument();

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        PdfDocument.PageInfo pageInfo =
                new PdfDocument.PageInfo.Builder(
                        595, 842, 1
                ).create();

        PdfDocument.Page pagina = documento.startPage(pageInfo);

        Canvas canvas = pagina.getCanvas();

        int roxo = Color.rgb(117, 104, 154);
        int cinza = Color.rgb(100, 96, 112);
        int escuro = Color.rgb(41, 37, 53);

        int margem = 45;
        int y = 65;

        // TÍTULO

        paint.setColor(roxo);
        paint.setTextSize(25);
        paint.setTypeface(Typeface.create(
                "sans-serif", Typeface.BOLD));

        canvas.drawText(
                "MindU",
                margem,
                y,
                paint
        );

        y += 35;

        paint.setColor(escuro);
        paint.setTextSize(19);

        canvas.drawText(
                "Relatório Executivo RH",
                margem,
                y,
                paint
        );

        y += 25;

        paint.setColor(cinza);
        paint.setTextSize(10);
        paint.setTypeface(Typeface.create(
                "sans-serif", Typeface.NORMAL));

        String dataEmissao = new SimpleDateFormat(
                "dd/MM/yyyy 'às' HH:mm",
                Locale.getDefault()
        ).format(new Date());

        canvas.drawText(
                "Emitido em: " + dataEmissao,
                margem,
                y,
                paint
        );

        y += 35;

        // LINHA DIVISÓRIA

        paint.setColor(Color.LTGRAY);
        paint.setStrokeWidth(1);

        canvas.drawLine(
                margem,
                y,
                550,
                y,
                paint
        );

        y += 35;

        // MATRÍCULAS

        paint.setColor(roxo);
        paint.setTextSize(15);
        paint.setTypeface(Typeface.create(
                "sans-serif", Typeface.BOLD));

        canvas.drawText(
                "1. Utilização do benefício",
                margem,
                y,
                paint
        );

        y += 28;

        y = desenharLinhaPDF(
                canvas, paint, "Total de matrículas",
                String.valueOf(relTotalMatriculas), y, margem
        );

        y = desenharLinhaPDF(
                canvas, paint, "Matrículas utilizadas",
                String.valueOf(relMatriculasUtilizadas), y, margem
        );

        y = desenharLinhaPDF(
                canvas, paint, "Matrículas disponíveis",
                String.valueOf(relMatriculasDisponiveis), y, margem
        );

        y = desenharLinhaPDF(
                canvas, paint, "Taxa de utilização",
                relPercentualUso + "%", y, margem
        );

        y += 25;

        // EQUIPE

        paint.setColor(roxo);
        paint.setTextSize(15);
        paint.setTypeface(Typeface.create(
                "sans-serif", Typeface.BOLD));

        canvas.drawText(
                "2. Participação da equipe",
                margem,
                y,
                paint
        );

        y += 28;

        y = desenharLinhaPDF(
                canvas, paint, "Colaboradores cadastrados",
                String.valueOf(relTotalColaboradores), y, margem
        );

        y = desenharLinhaPDF(
                canvas, paint, "Colaboradores aprovados",
                String.valueOf(relColaboradoresAprovados), y, margem
        );

        y = desenharLinhaPDF(
                canvas, paint, "Colaboradores ativos",
                String.valueOf(relColaboradoresAtivos), y, margem
        );

        y = desenharLinhaPDF(
                canvas, paint, "Beneficiários com matrícula utilizada",
                String.valueOf(relBeneficiarios), y, margem
        );

        y = desenharLinhaPDF(
                canvas, paint, "Taxa de participação",
                relPercentualAdesao + "%", y, margem
        );

        y += 35;

        // OBSERVAÇÃO

        paint.setColor(cinza);
        paint.setTextSize(9);
        paint.setTypeface(Typeface.create(
                "sans-serif", Typeface.ITALIC));

        canvas.drawText(
                "Relatório gerado automaticamente pelo aplicativo MindU.",
                margem,
                y,
                paint
        );

        y += 16;

        canvas.drawText(
                "Os indicadores refletem os dados retornados pelo sistema na emissão.",
                margem,
                y,
                paint
        );

        documento.finishPage(pagina);

        ByteArrayOutputStream outputStream =
                new ByteArrayOutputStream();

        try {

            documento.writeTo(outputStream);

            pdfPendente = outputStream.toByteArray();

            documento.close();

            Intent intent = new Intent(
                    Intent.ACTION_CREATE_DOCUMENT
            );

            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("application/pdf");

            intent.putExtra(
                    Intent.EXTRA_TITLE,
                    "Relatorio_Executivo_MindU.pdf"
            );

            startActivityForResult(
                    intent,
                    REQUEST_SALVAR_PDF
            );

        } catch (IOException e) {

            documento.close();

            Log.e("PDF_MINDU",
                    "Erro ao gerar PDF", e);

            Toast.makeText(
                    this,
                    "Não foi possível gerar o PDF.",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private int desenharLinhaPDF(
            Canvas canvas,
            Paint paint,
            String titulo,
            String valor,
            int y,
            int margem) {

        paint.setColor(Color.rgb(90, 86, 103));
        paint.setTextSize(11);
        paint.setTypeface(Typeface.create(
                "sans-serif", Typeface.NORMAL));

        canvas.drawText(
                titulo,
                margem,
                y,
                paint
        );

        paint.setColor(Color.rgb(117, 104, 154));
        paint.setTypeface(Typeface.create(
                "sans-serif", Typeface.BOLD));

        paint.setTextAlign(Paint.Align.RIGHT);

        canvas.drawText(
                valor,
                550,
                y,
                paint
        );

        paint.setTextAlign(Paint.Align.LEFT);

        return y + 25;
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_SALVAR_PDF
                && resultCode == RESULT_OK
                && data != null
                && data.getData() != null) {

            Uri uri = data.getData();

            try (OutputStream outputStream =
                         getContentResolver().openOutputStream(uri)) {

                if (outputStream != null && pdfPendente != null) {

                    outputStream.write(pdfPendente);
                    outputStream.flush();

                    Toast.makeText(
                            this,
                            "Relatório PDF salvo com sucesso!",
                            Toast.LENGTH_LONG
                    ).show();

                    pdfPendente = null;

                } else {

                    Toast.makeText(
                            this,
                            "Não foi possível salvar o arquivo.",
                            Toast.LENGTH_SHORT
                    ).show();
                }

            } catch (IOException e) {

                Log.e("PDF_MINDU",
                        "Erro ao salvar PDF", e);

                Toast.makeText(
                        this,
                        "Erro ao salvar o relatório.",
                        Toast.LENGTH_LONG
                ).show();
            }
        }
    }

    // =========================================================
    // COLABORADORES
    // =========================================================

    private void configurarTelaColaboradores(View tela) {

        rvColaboradoresEmpresa = tela.findViewById(
                R.id.rvColaboradoresEmpresa
        );

        edtBuscarColaboradorEmpresa = tela.findViewById(
                R.id.edtBuscarColaboradorEmpresa
        );

        btnPendentesEmpresa = tela.findViewById(
                R.id.btnPendentesEmpresa
        );

        btnAprovadosEmpresa = tela.findViewById(
                R.id.btnAprovadosEmpresa
        );

        txtColaboradoresEmpresaVazio = tela.findViewById(
                R.id.txtColaboradoresEmpresaVazio
        );

        rvColaboradoresEmpresa.setLayoutManager(
                new LinearLayoutManager(this)
        );

        adapter = new ColaboradoresAdapter(
                listaFiltrada,
                ColaboradoresAdapter.Modo.GERENCIAMENTO,
                colaborador -> abrirFichaColaborador(colaborador),
                (colaborador, aprovado) ->
                        confirmarAcaoColaborador(colaborador, aprovado)
        );

        rvColaboradoresEmpresa.setAdapter(adapter);

        configurarAbas();
        configurarPesquisa();
        carregarColaboradores();
    }

    private void carregarColaboradores() {

        apiService.listarClientes(null).enqueue(
                new Callback<List<ClienteResponse>>() {

                    @Override
                    public void onResponse(
                            Call<List<ClienteResponse>> call,
                            Response<List<ClienteResponse>> response) {

                        if (response.isSuccessful() && response.body() != null) {

                            listaCompleta.clear();
                            listaCompleta.addAll(response.body());

                            filtrarColaboradores();

                        } else {

                            Toast.makeText(
                                    DashboardEmpresa.this,
                                    "Não foi possível carregar os colaboradores.",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<List<ClienteResponse>> call,
                            Throwable t) {

                        Toast.makeText(
                                DashboardEmpresa.this,
                                "Erro de conexão com o servidor.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    private void configurarAbas() {

        btnPendentesEmpresa.setOnClickListener(v -> {

            statusSelecionado = "AGUARDANDO_APROVACAO";

            atualizarEstiloAbas();
            filtrarColaboradores();
        });

        btnAprovadosEmpresa.setOnClickListener(v -> {

            statusSelecionado = "APROVADO";

            atualizarEstiloAbas();
            filtrarColaboradores();
        });

        atualizarEstiloAbas();
    }

    private void atualizarEstiloAbas() {

        boolean pendentes =
                statusSelecionado.equals("AGUARDANDO_APROVACAO");

        btnPendentesEmpresa.setBackgroundTintList(
                ColorStateList.valueOf(
                        Color.parseColor(
                                pendentes ? "#75689A" : "#D4CDE9"
                        )
                )
        );

        btnAprovadosEmpresa.setBackgroundTintList(
                ColorStateList.valueOf(
                        Color.parseColor(
                                pendentes ? "#D4CDE9" : "#75689A"
                        )
                )
        );

        btnPendentesEmpresa.setTextColor(
                Color.parseColor(
                        pendentes ? "#FFFFFF" : "#75689A"
                )
        );

        btnAprovadosEmpresa.setTextColor(
                Color.parseColor(
                        pendentes ? "#75689A" : "#FFFFFF"
                )
        );
    }

    private void configurarPesquisa() {

        edtBuscarColaboradorEmpresa.addTextChangedListener(
                new android.text.TextWatcher() {

                    @Override
                    public void beforeTextChanged(
                            CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(
                            CharSequence s, int start, int before, int count) {

                        buscaAtual = s.toString();
                        filtrarColaboradores();
                    }

                    @Override
                    public void afterTextChanged(android.text.Editable s) {
                    }
                });
    }

    private void filtrarColaboradores() {

        listaFiltrada.clear();

        for (ClienteResponse colaborador : listaCompleta) {

            boolean correspondeStatus =
                    statusSelecionado.equals(colaborador.getStatus());

            boolean correspondeBusca =
                    colaborador.getNome() != null
                            && colaborador.getNome()
                            .toLowerCase(Locale.ROOT)
                            .contains(buscaAtual.toLowerCase(Locale.ROOT));

            if (correspondeStatus && correspondeBusca) {
                listaFiltrada.add(colaborador);
            }
        }

        if (adapter != null) {
            adapter.atualizarLista(listaFiltrada);
        }

        boolean listaVazia = listaFiltrada.isEmpty();

        txtColaboradoresEmpresaVazio.setVisibility(
                listaVazia ? View.VISIBLE : View.GONE
        );

        rvColaboradoresEmpresa.setVisibility(
                listaVazia ? View.GONE : View.VISIBLE
        );
    }

    private void abrirFichaColaborador(ClienteResponse colaborador) {

        View ficha = getLayoutInflater().inflate(
                R.layout.dialog_ficha_colaborador,
                null
        );

        TextView txtFichaNome =
                ficha.findViewById(R.id.txtFichaNome);

        TextView txtFichaStatus =
                ficha.findViewById(R.id.txtFichaStatus);

        TextView txtFichaEmail =
                ficha.findViewById(R.id.txtFichaEmail);

        TextView txtFichaId =
                ficha.findViewById(R.id.txtFichaId);

        Button btnFecharFicha =
                ficha.findViewById(R.id.btnFecharFicha);

        Button btnExcluirColaborador =
                ficha.findViewById(R.id.btnExcluirColaborador);

        txtFichaNome.setText(colaborador.getNome());

        txtFichaStatus.setText(
                colaborador.getStatus() == null
                        ? "Não informado"
                        : colaborador.getStatus()
        );

        txtFichaEmail.setText(
                colaborador.getEmail() == null
                        ? "Não informado"
                        : colaborador.getEmail()
        );

        txtFichaId.setText(
                colaborador.getId() == null
                        ? "Não informado"
                        : colaborador.getId()
        );

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(ficha)
                .create();

        btnFecharFicha.setOnClickListener(v -> dialog.dismiss());

        btnExcluirColaborador.setOnClickListener(v -> {

            new AlertDialog.Builder(this)
                    .setTitle("Excluir colaborador?")
                    .setMessage(
                            "Tem certeza de que deseja excluir "
                                    + colaborador.getNome()
                                    + "? Essa ação não poderá ser desfeita."
                    )
                    .setNegativeButton(
                            "Cancelar",
                            (d, which) -> d.dismiss()
                    )
                    .setPositiveButton(
                            "Excluir",
                            (d, which) -> {
                                dialog.dismiss();
                                excluirColaborador(colaborador);
                            }
                    )
                    .show();
        });

        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(
                    android.R.color.transparent
            );
        }
    }

    private void confirmarAcaoColaborador(
            ClienteResponse colaborador,
            boolean aprovado) {

        String titulo = aprovado
                ? "Aprovar colaborador?"
                : "Reprovar colaborador?";

        String mensagem = aprovado
                ? "Deseja aprovar " + colaborador.getNome() + "?"
                : "Deseja reprovar " + colaborador.getNome() + "?";

        new AlertDialog.Builder(this)
                .setTitle(titulo)
                .setMessage(mensagem)
                .setNegativeButton(
                        "Cancelar",
                        (dialog, which) -> dialog.dismiss()
                )
                .setPositiveButton(
                        "Confirmar",
                        (dialog, which) ->
                                alterarAprovacao(colaborador, aprovado)
                )
                .show();
    }

    private void alterarAprovacao(
            ClienteResponse colaborador,
            boolean aprovado) {

        apiService.aprovarCliente(
                colaborador.getId(),
                aprovado
        ).enqueue(new Callback<ClienteResponse>() {

            @Override
            public void onResponse(
                    Call<ClienteResponse> call,
                    Response<ClienteResponse> response) {

                if (response.isSuccessful()) {

                    Toast.makeText(
                            DashboardEmpresa.this,
                            aprovado
                                    ? "Colaborador aprovado!"
                                    : "Colaborador reprovado!",
                            Toast.LENGTH_SHORT
                    ).show();

                    carregarColaboradores();

                } else {

                    Toast.makeText(
                            DashboardEmpresa.this,
                            "Não foi possível realizar a operação.",
                            Toast.LENGTH_LONG
                    ).show();
                }
            }

            @Override
            public void onFailure(
                    Call<ClienteResponse> call,
                    Throwable t) {

                Toast.makeText(
                        DashboardEmpresa.this,
                        "Erro de conexão com o servidor.",
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private void excluirColaborador(ClienteResponse colaborador) {

        apiService.excluirFuncionario(colaborador.getId())
                .enqueue(new Callback<Void>() {

                    @Override
                    public void onResponse(
                            Call<Void> call,
                            Response<Void> response) {

                        if (response.isSuccessful()) {

                            Toast.makeText(
                                    DashboardEmpresa.this,
                                    "Colaborador excluído com sucesso!",
                                    Toast.LENGTH_SHORT
                            ).show();

                            carregarColaboradores();

                        } else {

                            Toast.makeText(
                                    DashboardEmpresa.this,
                                    "Não foi possível excluir o colaborador.",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<Void> call,
                            Throwable t) {

                        Toast.makeText(
                                DashboardEmpresa.this,
                                "Erro de conexão ao excluir colaborador.",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }

    // =========================================================
    // PERFIL DA EMPRESA
    // =========================================================

    private void configurarTelaPerfilEmpresa(View tela) {

        TextView txtNome =
                tela.findViewById(R.id.txtNomeEmpresaPerfil);

        TextView txtEmail =
                tela.findViewById(R.id.txtRazaoSocialPerfil);

        TextView txtTipo =
                tela.findViewById(R.id.txtPlanoEmpresaPerfil);

        TextView txtStatus =
                tela.findViewById(R.id.txtPlanoDetalhesPerfil);

        apiService.minhaConta().enqueue(
                new Callback<MinhaContaResponse>() {

                    @Override
                    public void onResponse(
                            Call<MinhaContaResponse> call,
                            Response<MinhaContaResponse> response) {

                        if (response.isSuccessful() && response.body() != null) {

                            MinhaContaResponse conta = response.body();

                            txtNome.setText(conta.getNome());
                            txtEmail.setText(conta.getEmail());
                            txtTipo.setText(conta.getTipo());

                            String status = conta.getStatus();

                            txtStatus.setText(
                                    status != null && !status.trim().isEmpty()
                                            ? status
                                            : "Não informado"
                            );

                        } else {

                            Toast.makeText(
                                    DashboardEmpresa.this,
                                    "Não foi possível carregar os dados da conta.",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<MinhaContaResponse> call,
                            Throwable t) {

                        Toast.makeText(
                                DashboardEmpresa.this,
                                "Erro ao carregar o perfil.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    // =========================================================
    // BLURVIEW
    // =========================================================

    private void aplicarEfeitoVidro(
            BlurView blurView,
            BlurTarget blurTarget) {

        if (blurView == null || blurTarget == null) return;

        float density = getResources()
                .getDisplayMetrics().density;

        GradientDrawable borda = new GradientDrawable();

        borda.setShape(GradientDrawable.RECTANGLE);
        borda.setCornerRadius(24 * density);
        borda.setColor(Color.TRANSPARENT);

        borda.setStroke(
                Math.max(1, (int) density),
                Color.parseColor("#99FFFFFF")
        );

        blurView.setBackground(borda);

        blurView.setupWith(blurTarget)
                .setBlurRadius(25f);

        blurView.setBackground(
                getDrawable(R.drawable.bg_vidro)
        );

        blurView.setOutlineProvider(
                ViewOutlineProvider.BACKGROUND
        );

        blurView.setClipToOutline(true);
        blurView.setElevation(4 * density);
    }
}
