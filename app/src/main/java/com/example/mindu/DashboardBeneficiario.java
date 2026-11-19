
package com.example.mindu;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class DashboardBeneficiario extends AppCompatActivity {

    private FrameLayout containerBeneficiario;

    private ImageView navInicio;
    private ImageView navAgendamentos;
    private ImageView navImediato;
    private ImageView navPerfil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard_beneficiario);

        inicializarComponentes();
        configurarNavegacao();

        abrirTela(R.layout.content_inicio_beneficiario);
    }

    private void inicializarComponentes() {

        containerBeneficiario = findViewById(R.id.containerBeneficiario);

        navInicio = findViewById(R.id.navInicioBeneficiario);
        navAgendamentos = findViewById(R.id.navAgendamentosBeneficiario);
        navImediato = findViewById(R.id.navImediatoBeneficiario);
        navPerfil = findViewById(R.id.navPerfilBeneficiario);
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

    private void abrirTela(int layout) {

        View tela = LayoutInflater.from(this)
                .inflate(layout, containerBeneficiario, false);

        containerBeneficiario.removeAllViews();
        containerBeneficiario.addView(tela);
    }
}
