package com.example.mindu;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewOutlineProvider;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import eightbitlab.com.blurview.BlurTarget;
import eightbitlab.com.blurview.BlurView;

import android.net.Uri;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

public class CadastroProfissional extends AppCompatActivity {

    private ActivityResultLauncher<String> selecionarImagemLauncher;

    private Uri fotoCarteirinhaUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cadastro_profissional);
        View main = findViewById(R.id.main);
        if (main != null) {
            ViewCompat.setOnApplyWindowInsetsListener(main, (v, insets) -> {
                Insets systemBars = insets.getInsets(
                        WindowInsetsCompat.Type.systemBars()
                );

                v.setPadding(
                        systemBars.left,
                        systemBars.top,
                        systemBars.right,
                        systemBars.bottom
                );

                return insets;
            });
        }

        BlurTarget blurTargetCadastro = findViewById(
                R.id.blurTargetCadastro
        );

        BlurView blurCadastroProfissional = findViewById(
                R.id.blurCadastroProfissional
        );

        if (blurTargetCadastro != null && blurCadastroProfissional != null) {
            blurCadastroProfissional
                    .setupWith(blurTargetCadastro)
                    .setBlurRadius(20f);

            aplicarEfeitoVidroLiquido(blurCadastroProfissional);
        }

        // Referências das etapas
        View layoutEtapa1 = findViewById(R.id.layoutEtapa1);
        View layoutEtapa2 = findViewById(R.id.layoutEtapa2);

        // Indicadores de progresso
        View progressoEtapa1 = findViewById(R.id.progressoEtapa1);
        View progressoEtapa2 = findViewById(R.id.progressoEtapa2);

        Button buttonProximaEtapa = findViewById(R.id.buttonProximaEtapa);
        Button buttonVoltarEtapa = findViewById(R.id.buttonVoltarEtapa);

        // Avançar para a segunda etapa
        buttonProximaEtapa.setOnClickListener(v -> {
            layoutEtapa1.setVisibility(View.GONE);
            layoutEtapa2.setVisibility(View.VISIBLE);

            progressoEtapa1.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(Color.parseColor("#75689A"))
            );

            progressoEtapa2.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(Color.parseColor("#75689A"))
            );
        });

        // Voltar para a primeira etapa
        buttonVoltarEtapa.setOnClickListener(v -> {
            layoutEtapa2.setVisibility(View.GONE);
            layoutEtapa1.setVisibility(View.VISIBLE);
        });

        selecionarImagemLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        fotoCarteirinhaUri = uri;

                        TextView textCarteirinha = findViewById(
                                R.id.textCarteirinhaSelecionada
                        );

                        textCarteirinha.setText("Carteirinha selecionada!");
                    }
                }
        );

        Button btnSelecionarCarteirinha = findViewById(
                R.id.btnSelecionarCarteirinha
        );

        btnSelecionarCarteirinha.setOnClickListener(v -> {
            selecionarImagemLauncher.launch("image/*");
        });

        Button btnCriarConta = findViewById(R.id.btnCriarConta);
        btnCriarConta.setOnClickListener(v ->  cadastrar());
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

        view.setOutlineProvider(ViewOutlineProvider.BACKGROUND);
        view.setClipToOutline(true);
        view.setElevation(4 * density);
    }

    private void cadastrar(){

    }
}
