
package com.example.mindu;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class DashboardEmpresa extends AppCompatActivity {

    private View containerEmpresa;

    private ImageView navInicioEmpresa;
    private ImageView navColaboradoresEmpresa;
    private ImageView navRelatoriosEmpresa;
    private ImageView navPerfilEmpresa;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard_empresa);

        containerEmpresa = findViewById(R.id.containerEmpresa);

        navInicioEmpresa = findViewById(R.id.navInicioEmpresa);
        navColaboradoresEmpresa = findViewById(R.id.navColaboradoresEmpresa);
        navRelatoriosEmpresa = findViewById(R.id.navRelatoriosEmpresa);
        navPerfilEmpresa = findViewById(R.id.navPerfilEmpresa);

        navInicioEmpresa.setOnClickListener(v -> {
            atualizarNavegacao(navInicioEmpresa);
            mostrarTela(R.layout.content_inicio_empresa);
        });

        /* navColaboradoresEmpresa.setOnClickListener(v -> {
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
        });*/

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
    }
}
