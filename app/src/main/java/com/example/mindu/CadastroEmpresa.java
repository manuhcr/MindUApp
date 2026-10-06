package com.example.mindu;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import eightbitlab.com.blurview.BlurTarget;
import eightbitlab.com.blurview.BlurView;

public class CadastroEmpresa extends AppCompatActivity {


    private final int ROXO = Color.parseColor("#75689A");
    private final int ROXO_CLARO = Color.parseColor("#D9D1E8");
    private LinearLayout layoutEtapa1;
    private LinearLayout layoutEtapa2;
    private TextView textEtapaAtual;
    private View progressoEtapa1;
    private View progressoEtapa2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cadastro_empresa);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        BlurTarget blurTargetCadastro = findViewById(R.id.blurTargetCadastro);
        BlurView blurCadastroEmpresa = findViewById(R.id.blurCadastroEmpresa);

        blurCadastroEmpresa
                .setupWith(blurTargetCadastro)
                .setBlurRadius(20f);

        aplicarEfeitoVidroLiquido(blurCadastroEmpresa);

        // Componentes das duas etapas
        layoutEtapa1 = findViewById(R.id.layoutEtapa1);
        layoutEtapa2 = findViewById(R.id.layoutEtapa2);

        textEtapaAtual = findViewById(R.id.textEtapaAtual);
        progressoEtapa1 = findViewById(R.id.progressoEtapa1);
        progressoEtapa2 = findViewById(R.id.progressoEtapa2);

        Button buttonProximaEtapa =
                findViewById(R.id.buttonProximaEtapa);

        Button buttonVoltarEtapa =
                findViewById(R.id.buttonVoltarEtapa);

        // Avançar para a segunda etapa
        buttonProximaEtapa.setOnClickListener(v -> {
            mostrarEtapa(2);
        });

        // Voltar para a primeira etapa
        buttonVoltarEtapa.setOnClickListener(v -> {
            mostrarEtapa(1);
        });
    }
    private void mostrarEtapa(int etapa) {

        boolean primeiraEtapa = etapa == 1;

        layoutEtapa1.setVisibility(
                primeiraEtapa ? View.VISIBLE : View.GONE);

        layoutEtapa2.setVisibility(
                primeiraEtapa ? View.GONE : View.VISIBLE);

        textEtapaAtual.setText(
                primeiraEtapa ? "ETAPA 1 DE 2" : "ETAPA 2 DE 2");

        // A etapa ativa fica roxa; a outra fica mais clara.
        progressoEtapa1.setBackgroundTintList(
                ColorStateList.valueOf(
                        primeiraEtapa ? ROXO : ROXO_CLARO));

        progressoEtapa2.setBackgroundTintList(
                ColorStateList.valueOf(
                        primeiraEtapa ? ROXO_CLARO : ROXO));

        // Mantém a barra ativa totalmente visível.
        progressoEtapa1.setAlpha(1f);
        progressoEtapa2.setAlpha(1f);

        progressoEtapa1.setAlpha(1f);
        progressoEtapa2.setAlpha(primeiraEtapa ? 0.35f : 1f);

        // Volta o formulário para o início da etapa
        View scroll = findViewById(R.id.scrollCadastroEmpresa);
        scroll.scrollTo(0, 0);
    }
    private void aplicarEfeitoVidroLiquido(View view) {
        if (view == null) return;

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

        view.setElevation(4 * density);
    }
}