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

public class LoginEmpresa extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login_empresa);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        BlurTarget blurTargetCadastro = findViewById(R.id.blurTargetCadastro);
        BlurView blurCadastroBeneficiario = findViewById(R.id.blurCadastroBeneficiario);
        blurCadastroBeneficiario.setupWith(blurTargetCadastro).setBlurRadius(20f);

        aplicarEfeitoVidroLiquido(blurCadastroBeneficiario);
    }

    private void aplicarEfeitoVidroLiquido(View view) {
        if (view == null) return;
        GradientDrawable liquidGlass = new GradientDrawable();
        liquidGlass.setShape(GradientDrawable.RECTANGLE);
        liquidGlass.setOrientation(GradientDrawable.Orientation.TL_BR);




        liquidGlass.setColor(
                Color.parseColor("#1F8294EF")
        );
        /*
         * Valores como "24dp" precisam ser convertidos
         * para pixels quando trabalhamos diretamente com Java.
         *
         * density informa a densidade da tela.
         */
        float density = view.getContext()
                .getResources()
                .getDisplayMetrics()
                .density;
        liquidGlass.setCornerRadius(24 * density);
        int strokeWidthPx = (int) (1 * density);
        liquidGlass.setStroke(strokeWidthPx, Color.parseColor("#99F5F3F7"));
        view.setBackground(liquidGlass);
        view.setOutlineProvider(ViewOutlineProvider.BACKGROUND);

        view.setClipToOutline(true);

        view.setElevation(5 * density);

    }
}