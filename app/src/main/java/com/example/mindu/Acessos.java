package com.example.mindu;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Acessos extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_acessos);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 1. FORMATAÇÃO DO TEXTO EM NEGRITO
        TextView txtPerfil = findViewById(R.id.texto);
        String textoCompleto = "Para começar, precisamos entender qual o seu perfil de acesso.";
        String trechoParaNegrito = "perfil de acesso.";

        android.text.SpannableString spannable = new android.text.SpannableString(textoCompleto);
        int inicio = textoCompleto.indexOf(trechoParaNegrito);
        int fim = inicio + trechoParaNegrito.length();

        if (inicio >= 0) {
            spannable.setSpan(
                    new android.text.style.StyleSpan(android.graphics.Typeface.BOLD),
                    inicio,
                    fim,
                    android.text.SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }
        txtPerfil.setText(spannable);

        // 2. INICIALIZAÇÃO DOS BOTÕES
        Button btnColaborador = findViewById(R.id.btnColaborador);
        Button btnEmpresa = findViewById(R.id.btnEmpresa);
        Button btnPsicologo = findViewById(R.id.btnPsicologo);

        // 3. EVENTOS DE CLIQUE (AÇÕES DE NAVEGAÇÃO)
        btnColaborador.setOnClickListener(v -> {
            Intent rotacionar = new Intent(Acessos.this, TelaInicio.class);
            startActivity(rotacionar);
        });

        btnEmpresa.setOnClickListener(v -> {
            Intent rotacionar = new Intent(Acessos.this, TelaInicio.class);
            startActivity(rotacionar);
        });

        btnPsicologo.setOnClickListener(v -> {
            Intent rotacionar = new Intent(Acessos.this, TelaInicio.class);
            startActivity(rotacionar);
        });

        // 4. O CASAMENTO: Injeta o efeito físico de vidro líquido sobre as tags do XML
        aplicarEfeitoVidroLiquido(btnColaborador);
        aplicarEfeitoVidroLiquido(btnEmpresa);
        aplicarEfeitoVidroLiquido(btnPsicologo);
    }

    private void aplicarEfeitoVidroLiquido(View view) {
        if (view == null) return;

        // Criamos o preenchimento do vidro dinamicamente com as cores exatas da imagem
        GradientDrawable liquidGlass = new GradientDrawable();
        liquidGlass.setShape(GradientDrawable.RECTANGLE);
        liquidGlass.setOrientation(GradientDrawable.Orientation.TL_BR); // Diagonal

        // Mistura de lavanda translúcido com branco fosco para realçar o fundo roxo ondulado
        int[] coresDoVidro = new int[]{
                Color.parseColor("#4DCCDBFF"), // 30% de opacidade no topo esquerdo
                Color.parseColor("#1AFFD6F9")  // 10% de opacidade na base direita
        };
        liquidGlass.setColors(coresDoVidro);

        // Curvatura exata para bater com os cantos do XML
        float radiusPx = 24 * view.getContext().getResources().getDisplayMetrics().density;
        liquidGlass.setCornerRadius(radiusPx);

        // Borda branca fina para dar acabamento de reflexo de corte no vidro
        int strokeWidthPx = (int) (1.2f * view.getContext().getResources().getDisplayMetrics().density);
        liquidGlass.setStroke(strokeWidthPx, Color.parseColor("#80FFFFFF"));

        // Aplica o efeito mesclado diretamente como o fundo real da View
        view.setBackground(liquidGlass);
    }
}
