
package com.example.mindu;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mindu.api.RetrofitClient;
import com.example.mindu.api.cliente.ClienteResponse;
import com.example.mindu.api.login.MinhaContaResponse;
import com.example.mindu.api.profissional.ProfissionalResponse;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import eightbitlab.com.blurview.BlurTarget;
import eightbitlab.com.blurview.BlurView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.widget.FrameLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mindu.adapter.ProfissionaisAdapter;
import com.example.mindu.api.cadastro.EmpresaResponse;
import com.example.mindu.adapter.EmpresasAdapter;
import com.example.mindu.api.profissional.agendamento.AgendamentoResponse;

public class DashboardAdmin extends AppCompatActivity {

    private ImageView imgFotoAdmin;
    private ImageButton btnCameraAdmin;
    private FrameLayout areaFotoAdmin;

    private ActivityResultLauncher<Void> cameraLauncher;
    private FrameLayout containerAdmin;
    private ImageView navInicio;
    private ImageView navEmpresas;
    private ImageView navProfissionais;
    private ImageView navPerfil;

    private RecyclerView recyclerProfissionais;
    private ProfissionaisAdapter profissionaisAdapter;

    private TextView txtTotalPendentes;
    private TextView txtTotalProfissionais;
    private TextView txtTotalEmpresas;

    private TextView txtListaVazia;

    private RecyclerView recyclerEmpresas;
    private EmpresasAdapter empresasAdapter;
    private TextView txtEmpresasVazio;

    private TextView txtTotalAgendamentos;

    TextView txtNome;
    TextView txtEmail;
    TextView txtInicial;

    @SuppressLint("ThreadConstraint")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard_admin);

        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicturePreview(),
                bitmap -> {
                    if (bitmap != null) {

                        salvarFotoAdmin(bitmap);

                        imgFotoAdmin.setImageBitmap(bitmap);
                        imgFotoAdmin.setVisibility(View.VISIBLE);
                        txtInicial.setVisibility(View.GONE);



                    }
                }
        );

        containerAdmin = findViewById(R.id.containerAdmin);

        navInicio = findViewById(R.id.navInicio);
        navEmpresas = findViewById(R.id.navEmpresas);
        navProfissionais = findViewById(R.id.navProfissionais);
        navPerfil = findViewById(R.id.navPerfil);

        navInicio.setOnClickListener(v -> {
            atualizarNavegacao(navInicio);
            mostrarTela(R.layout.content_inicio_admin);
        });

        navEmpresas.setOnClickListener(v -> {
            atualizarNavegacao(navEmpresas);
            mostrarEmpresas();
        });

        navProfissionais.setOnClickListener(v -> {
            atualizarNavegacao(navProfissionais);
            mostrarProfissionais();
        });

        navPerfil.setOnClickListener(v -> {
            atualizarNavegacao(navPerfil);
            mostrarTela(R.layout.content_perfil_admin);
        });

        atualizarNavegacao(navInicio);
        mostrarTela(R.layout.content_inicio_admin);
    }

    private void atualizarNavegacao(ImageView selecionado) {

        int corAtiva = Color.parseColor("#FFFFFF");
        int corInativa = Color.parseColor("#D4CDE9");

        navInicio.setColorFilter(corInativa);
        navEmpresas.setColorFilter(corInativa);
        navProfissionais.setColorFilter(corInativa);
        navPerfil.setColorFilter(corInativa);

        selecionado.setColorFilter(corAtiva);
    }



    private void mostrarTela(int layout) {

        View tela = getLayoutInflater().inflate(layout, containerAdmin, false);

        containerAdmin.removeAllViews();
        containerAdmin.addView(tela);



        if (layout == R.layout.content_inicio_admin) {

            txtTotalPendentes = tela.findViewById(R.id.txtTotalPendentes);
            txtTotalProfissionais = tela.findViewById(R.id.txtTotalProfissionais);
            txtTotalEmpresas = tela.findViewById(R.id.txtTotalEmpresas);
            txtTotalAgendamentos = tela.findViewById(R.id.txtTotalAgendamentos);

            BlurTarget blurTarget = findViewById(R.id.blurTargetDashboardAdmin);

            aplicarEfeitoVidro(
                    tela.findViewById(R.id.cardBoasVindasAdmin),
                    blurTarget
            );

            aplicarEfeitoVidro(
                    tela.findViewById(R.id.cardPendentesAdmin),
                    blurTarget
            );

            aplicarEfeitoVidro(
                    tela.findViewById(R.id.cardProfissionaisAdmin),
                    blurTarget
            );

            aplicarEfeitoVidro(
                    tela.findViewById(R.id.cardEmpresasAdmin),
                    blurTarget
            );

            aplicarEfeitoVidro(
                    tela.findViewById(R.id.cardAgendamentosAdmin),
                    blurTarget
            );

            aplicarEfeitoVidro(
                    tela.findViewById(R.id.cardGerenciarEmpresasAdmin),
                    blurTarget
            );

            aplicarEfeitoVidro(
                    tela.findViewById(R.id.cardGerenciarProfissionaisAdmin),
                    blurTarget
            );

            carregarIndicadores();

            TextView txtSaudacao = tela.findViewById(R.id.txtSaudacaoAdmin);

            // Atalhos de acesso
            BlurView cardEmpresas = tela.findViewById(
                    R.id.cardGerenciarEmpresasAdmin
            );

            BlurView cardProfissionais = tela.findViewById(
                    R.id.cardGerenciarProfissionaisAdmin
            );

            cardEmpresas.setOnClickListener(v -> mostrarEmpresas());

            cardProfissionais.setOnClickListener(v -> mostrarProfissionais());


            SharedPreferences prefs = getSharedPreferences("mindu", MODE_PRIVATE);
            String nomeAdmin = prefs.getString("nomeAdmin", "");

            if (nomeAdmin != null && !nomeAdmin.trim().isEmpty()) {
                txtSaudacao.setText("Olá, " + nomeAdmin.trim() + "! 👋");
            } else {
                txtSaudacao.setText("Olá! 👋");
            }
        }

        if (layout == R.layout.content_perfil_admin) {

            BlurView blurCard = tela.findViewById(R.id.blurCardPerfilAdmin);

            BlurTarget blurTarget = findViewById(R.id.blurTargetDashboardAdmin);

            BlurView cardMinhaConta = tela.findViewById(R.id.cardMinhaContaAdmin);

            BlurView cardSair = tela.findViewById(R.id.cardSairAdmin);

            txtNome = tela.findViewById(R.id.txtNomeAdmin);
            txtEmail = tela.findViewById(R.id.txtEmailAdmin);
            txtInicial = tela.findViewById(R.id.txtInicialAdmin);

            imgFotoAdmin = tela.findViewById(R.id.imgFotoAdmin);
            btnCameraAdmin = tela.findViewById(R.id.btnCameraAdmin);
            areaFotoAdmin = tela.findViewById(R.id.areaFotoAdmin);

            carregarFotoAdmin();

            imgFotoAdmin.setOutlineProvider(new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, android.graphics.Outline outline) {
                    outline.setOval(0, 0, view.getWidth(), view.getHeight());
                }
            });

            imgFotoAdmin.setClipToOutline(true);

            btnCameraAdmin.setOnClickListener(v -> cameraLauncher.launch(null));

            areaFotoAdmin.setOnClickListener(v -> cameraLauncher.launch(null));

            aplicarEfeitoVidro(blurCard, blurTarget);

            carregarPerfilAdmin(tela);

            aplicarEfeitoVidro(cardMinhaConta, blurTarget);

            cardMinhaConta.setOnClickListener(v -> mostrarDialogInfoContaAdmin());

            aplicarEfeitoVidro(cardSair, blurTarget);

            cardSair.setOnClickListener(v -> confirmarSaidaAdmin());
        }
    }

    private void mostrarProfissionais(){
        mostrarTela(R.layout.content_profissionais_admin);

        txtListaVazia = containerAdmin.findViewById(R.id.txtListaVazia);

        recyclerProfissionais = containerAdmin.findViewById(R.id.recyclerProfissionais);

        recyclerProfissionais.setLayoutManager(
                new LinearLayoutManager(this));


        carregarProfissionais();

    }

    private void mostrarEmpresas() {

        mostrarTela(R.layout.content_empresas_admin);

        txtEmpresasVazio = containerAdmin.findViewById(R.id.txtEmpresasVazio);

        recyclerEmpresas = containerAdmin.findViewById(R.id.recyclerEmpresas);

        recyclerEmpresas.setLayoutManager(
                new LinearLayoutManager(this));

        carregarEmpresas();
    }
    private void carregarIndicadores() {

        // requisição de clientes pendentes
        RetrofitClient.getApiService(this)
                .listarClientesAdmin("pendente")
                .enqueue(new Callback<>() {

                    @Override
                    public void onResponse(
                            Call<List<ClienteResponse>> call,
                            Response<List<ClienteResponse>> response) {

                        if (response.isSuccessful() && response.body() != null) {

                            List<ClienteResponse> clientes = response.body();

                            int totalClientesPendentes = clientes.size();

                            txtTotalPendentes.setText(
                                    String.valueOf(totalClientesPendentes)
                            );

                        } else {
                            Toast.makeText(
                                    DashboardAdmin.this,
                                    "Não foi possível carregar os indicadores. Código: "
                                            + response.code(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<List<ClienteResponse>> call,
                            Throwable t) {

                        Toast.makeText(
                                DashboardAdmin.this,
                                "Erro de conexão ao carregar os indicadores.",
                                Toast.LENGTH_LONG
                        ).show();

                        Log.e("MINDU_API", "Erro nos indicadores", t);
                    }
                });

        // requisição de profissionais aprovados
        RetrofitClient.getApiService(this)
                .listarProfissionais("aprovado")
                .enqueue(new Callback<>() {
                    @Override
                    public void onResponse(
                            Call<List<ProfissionalResponse>> call,
                            Response<List<ProfissionalResponse>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            txtTotalProfissionais.setText(
                                    String.valueOf(response.body().size())
                            );
                        }
                    }
                    @Override
                    public void onFailure(
                            Call<List<ProfissionalResponse>> call,
                            Throwable t) {

                        Log.e("MINDU_API", "Erro nos indicadores", t);
                        if (t != null) {
                            Log.e("MINDU_API", "Erro nos indicadores", t);
                            Toast.makeText(
                                    DashboardAdmin.this,
                                    "Erro de conexão ao carregar os indicadores.",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }
                }
                );

        // requisição de empresas
        RetrofitClient.getApiService(this)
                .listarEmpresas()
                .enqueue(new Callback<>() {
                    @Override
                    public void onResponse(
                            Call<List<EmpresaResponse>> call,
                            Response<List<EmpresaResponse>> response) {
                        if (response.isSuccessful() && response.body() != null){
                            txtTotalEmpresas.setText(
                                    String.valueOf(response.body().size())
                            );
                        }
                    }
                    @Override
                    public void onFailure(
                            Call<List<EmpresaResponse>> call,
                            Throwable t) {
                        Log.e("MINDU_API", "Erro nos indicadores", t);

                        if (t != null) {
                            Log.e("MINDU_API", "Erro nos indicadores", t);

                            Toast.makeText(
                                    DashboardAdmin.this,
                                    "Erro de conexão ao carregar os indicadores.",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }
                }
                );

        // requisição de total de lista de atendimentos
        RetrofitClient.getApiService(this)
                .listarAgendamentos()
                .enqueue(new Callback<>() {
                    @Override
                    public void onResponse(
                            Call<List<AgendamentoResponse>> call,
                            Response<List<AgendamentoResponse>> response) {

                        if (response.isSuccessful() && response.body() != null) {
                            int total = response.body().size();

                            txtTotalAgendamentos.setText(String.valueOf(total));
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<List<AgendamentoResponse>> call,
                            Throwable t) {
                        Log.e("MINDU_API", "Erro ao carregar agendamentos", t);
                    }
                });

    }
    private void carregarProfissionais() {

        RetrofitClient.getApiService(this)
                .profissionaisPendentes()
                .enqueue(new Callback<>() {
                    @Override
                    public void onResponse(
                            Call<List<ProfissionalResponse>> call,
                            Response<List<ProfissionalResponse>> response) {
                        Log.d("MINDU_API", "Código: " + response.code());

                        Log.d("MINDU_API", "Body: " + response.body());
                        if (!response.isSuccessful()
                                || response.body() == null) {
                            Toast.makeText(
                                    DashboardAdmin.this,
                                    "Não foi possível carregar os profissionais.",
                                    Toast.LENGTH_LONG).show();
                        }else{
                            List<ProfissionalResponse> profissionais =
                                    response.body();
                            if (profissionais.isEmpty()) {
                                txtListaVazia.setVisibility(View.VISIBLE);
                                recyclerProfissionais.setVisibility(View.GONE);

                            } else {
                                txtListaVazia.setVisibility(View.GONE);
                                recyclerProfissionais.setVisibility(View.VISIBLE);
                            }
                            profissionaisAdapter = new ProfissionaisAdapter(

                                    profissionais,
                                    (profissional, aprovado, position) -> {
                                        atualizarAprovacao(profissional, aprovado, position);
                                    }
                            );
                            recyclerProfissionais.setAdapter(profissionaisAdapter);
                            Log.d("MINDU_API", "Total de profissionais: "
                                    + profissionais.size());


                        }
                    }

                    @Override
                    public void onFailure(
                            Call<List<ProfissionalResponse>> call,
                            Throwable t) {
                        Toast.makeText(
                                DashboardAdmin.this,
                                "Erro de conexão ao carregar os profissionais.",
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                });
    }

    private void carregarEmpresas() {

        RetrofitClient.getApiService(this)
                .listarEmpresas()
                .enqueue(new Callback<>() {

                    @Override
                    public void onResponse(
                            Call<List<EmpresaResponse>> call,
                            Response<List<EmpresaResponse>> response) {

                        if (!response.isSuccessful()
                                || response.body() == null) {

                            Toast.makeText(
                                    DashboardAdmin.this,
                                    "Não foi possível carregar as empresas.",
                                    Toast.LENGTH_LONG
                            ).show();

                        } else {

                            List<EmpresaResponse> empresas = response.body();

                            if (empresas.isEmpty()) {

                                txtEmpresasVazio.setVisibility(View.VISIBLE);
                                recyclerEmpresas.setVisibility(View.GONE);

                            } else {

                                txtEmpresasVazio.setVisibility(View.GONE);
                                recyclerEmpresas.setVisibility(View.VISIBLE);
                            }

                            empresasAdapter = new EmpresasAdapter(
                                    empresas,
                                    empresaId -> excluirEmpresa(empresaId)
                            );

                            recyclerEmpresas.setAdapter(empresasAdapter);

                            Log.d("MINDU_API",
                                    "Total de empresas: " + empresas.size());
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<List<EmpresaResponse>> call,
                            Throwable t) {

                        Toast.makeText(
                                DashboardAdmin.this,
                                "Erro de conexão ao carregar empresas.",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });

    }

    private void excluirEmpresa(String empresaId) {

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Excluir empresa")
                .setMessage("Tem certeza de que deseja excluir esta empresa?")
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Excluir", (dialog, which) -> {

                    RetrofitClient.getApiService(this)
                            .excluirEmpresa(empresaId)
                            .enqueue(new Callback<Void>() {

                                @Override
                                public void onResponse(
                                        Call<Void> call,
                                        Response<Void> response) {

                                    if (response.isSuccessful()) {

                                        Toast.makeText(
                                                DashboardAdmin.this,
                                                "Empresa excluída com sucesso!",
                                                Toast.LENGTH_SHORT
                                        ).show();

                                        carregarEmpresas();

                                    } else {

                                        Toast.makeText(
                                                DashboardAdmin.this,
                                                "Não foi possível excluir a empresa. Código: "
                                                        + response.code(),
                                                Toast.LENGTH_LONG
                                        ).show();
                                    }
                                }

                                @Override
                                public void onFailure(
                                        Call<Void> call,
                                        Throwable t) {

                                    Log.e("MINDU_API",
                                            "Erro ao excluir empresa", t);

                                    Toast.makeText(
                                            DashboardAdmin.this,
                                            "Erro de conexão ao excluir empresa.",
                                            Toast.LENGTH_LONG
                                    ).show();
                                }
                            });
                })
                .show();
    }

    private void atualizarAprovacao(
            ProfissionalResponse profissional,
            boolean aprovado, int position) {

        RetrofitClient.getApiService(this)
                .aprovarProfissional(
                        profissional.getId(), aprovado)
                .enqueue(new Callback<>() {

                    @Override
                    public void onResponse(
                            Call<ProfissionalResponse> call,
                            Response<ProfissionalResponse> response) {

                        if (response.isSuccessful()) {
                            profissionaisAdapter.removerProfissional(position);

                            if (profissionaisAdapter.getItemCount() == 0){
                                txtListaVazia.setVisibility(View.VISIBLE);
                                recyclerProfissionais.setVisibility(View.GONE);

                            }
                        } else {
                            Toast.makeText(
                                    DashboardAdmin.this,
                                    "Não foi possível concluir a ação. "
                                            + "Código: " + response.code(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<ProfissionalResponse> call,
                            Throwable t) {

                        Toast.makeText(
                                DashboardAdmin.this,
                                "Erro de conexão ao atualizar cadastro.",
                                Toast.LENGTH_LONG).show();
                    }
                });
    }



    private void aplicarEfeitoVidro(
            BlurView blurView,
            BlurTarget blurTarget) {

        if (blurView == null || blurTarget == null) return;

        float density = getResources().getDisplayMetrics().density;

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

    private void carregarPerfilAdmin(View tela) {

        TextView txtNome = tela.findViewById(R.id.txtNomeAdmin);
        TextView txtEmail = tela.findViewById(R.id.txtEmailAdmin);
        TextView txtInicial = tela.findViewById(R.id.txtInicialAdmin);



        RetrofitClient.getApiService(this)
                .minhaConta()
                .enqueue(new Callback<MinhaContaResponse>() {

                    @Override
                    public void onResponse(
                            Call<MinhaContaResponse> call,
                            Response<MinhaContaResponse> response) {

                        if (response.isSuccessful() && response.body() != null) {

                            MinhaContaResponse conta = response.body();

                            String nome = conta.getNome();
                            String email = conta.getEmail();

                            txtNome.setText(nome);
                            txtEmail.setText(email);

                            if (nome != null && !nome.trim().isEmpty()) {
                                txtInicial.setText(
                                        nome.trim().substring(0, 1).toUpperCase()
                                );
                            }

                            // Salva os dados recebidos para uso local
                            SharedPreferences prefs =
                                    getSharedPreferences("mindu", MODE_PRIVATE);

                            prefs.edit()
                                    .putString("nomeAdmin", nome)
                                    .putString("emailAdmin", email)
                                    .apply();

                        } else {
                            Log.e("MINDU_API",
                                    "Erro ao carregar perfil. Código: " + response.code());

                            Toast.makeText(
                                    DashboardAdmin.this,
                                    "Não foi possível carregar os dados da conta.",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<MinhaContaResponse> call,
                            Throwable t) {

                        Log.e("MINDU_API", "Erro ao carregar perfil", t);

                        Toast.makeText(
                                DashboardAdmin.this,
                                "Erro de conexão ao carregar o perfil.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }


    private void carregarFotoAdmin() {
        File arquivo = new File(getFilesDir(), "foto_admin.png");

        if (arquivo.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(arquivo.getAbsolutePath());

            if (bitmap != null) {
                imgFotoAdmin.setImageBitmap(bitmap);
                imgFotoAdmin.setVisibility(View.VISIBLE);
                txtInicial.setVisibility(View.GONE);
            }
        }
    }
    private void salvarFotoAdmin(Bitmap bitmap) {
        try {
            File arquivo = new File(getFilesDir(), "foto_admin.png");

            FileOutputStream outputStream = new FileOutputStream(arquivo);

            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);

            outputStream.close();

        } catch (IOException e) {
            Log.e("MINDU_API", "Erro ao salvar foto do administrador", e);
            Toast.makeText(this, "Erro ao salvar a foto.", Toast.LENGTH_SHORT).show();
        }
    }

    private void mostrarDialogInfoContaAdmin() {

        View dialogView = getLayoutInflater()
                .inflate(R.layout.dialog_info_conta_admin, null);

        TextView nome = dialogView.findViewById(R.id.dialogNomeAdmin);
        TextView email = dialogView.findViewById(R.id.dialogEmailAdmin);

        Button btnEditar = dialogView.findViewById(R.id.btnEditarContaAdmin);

        nome.setText(txtNome.getText().toString());
        email.setText(txtEmail.getText().toString());

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        dialog.getWindow();

        btnEditar.setOnClickListener(v -> {
            dialog.dismiss();
            mostrarDialogEditarContaAdmin();
        });

        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(
                    new android.graphics.drawable.ColorDrawable(
                            android.graphics.Color.TRANSPARENT
                    )
            );

            dialog.getWindow().setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.88),
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }

    private void mostrarDialogEditarContaAdmin() {

        View dialogView = getLayoutInflater()
                .inflate(R.layout.dialog_editar_conta_admin, null);

        EditText edtNome = dialogView.findViewById(R.id.edtNomeAdmin);
        EditText edtEmail = dialogView.findViewById(R.id.edtEmailAdmin);

        Button btnSalvar = dialogView.findViewById(R.id.btnSalvarEdicaoAdmin);

        edtNome.setText(txtNome.getText().toString());
        edtEmail.setText(txtEmail.getText().toString());

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(
                    android.R.color.transparent
            );
        }
    }

    private void confirmarSaidaAdmin() {

        new AlertDialog.Builder(this)
                .setTitle("Sair da conta")
                .setMessage("Tem certeza de que deseja sair da sua conta?")
                .setNegativeButton("Cancelar", (dialog, which) -> {
                    dialog.dismiss();
                })
                .setPositiveButton("Sair", (dialog, which) -> {

                    SharedPreferences prefs =
                            getSharedPreferences("mindu", MODE_PRIVATE);

                    prefs.edit()
                            .remove("token")
                            .remove("nomeAdmin")
                            .remove("emailAdmin")
                            .apply();

                    Intent intent = new Intent(
                            DashboardAdmin.this,
                            LoginAdmin.class
                    );

                    intent.setFlags(
                            Intent.FLAG_ACTIVITY_NEW_TASK |
                                    Intent.FLAG_ACTIVITY_CLEAR_TASK
                    );

                    startActivity(intent);
                    finish();
                })
                .show();
    }

}