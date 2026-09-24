package com.example.mindu;

import android.content.Intent;
import android.os.Bundle;
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

        TextView txtPerfil = findViewById(R.id.texto);

        // Definindo o texto completo exato que aparece na tela
        String textoCompleto = "Para começar, precisamos entender qual o seu perfil de acesso.";
        String trechoParaNegrito = "perfil de acesso."; // O trecho que ficará destacado

        // Criando a estrutura Spannable
        android.text.SpannableString spannable = new android.text.SpannableString(textoCompleto);

        // Encontrando e aplicando o negrito dinamicamente no trecho desejado
        int inicio = textoCompleto.indexOf(trechoParaNegrito);
        int fim = inicio + trechoParaNegrito.length();

        if (inicio >= 0) {
            // Aplica o negrito apenas no intervalo encontrado
            spannable.setSpan(
                    new android.text.style.StyleSpan(android.graphics.Typeface.BOLD),
                    inicio,
                    fim,
                    android.text.SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }

        // Envia o texto formatado de volta para o componente
        txtPerfil.setText(spannable);

        Button btnColaborador = findViewById(R.id.btnColaborador);
        btnColaborador.setOnClickListener(v -> {
            Intent rotacionar = new Intent(Acessos.this, TelaInicio.class);
            startActivity(rotacionar);
        });
        Button btnEmpresa = findViewById(R.id.btnEmpresa);
        btnEmpresa.setOnClickListener(v -> {
            Intent rotacionar = new Intent(Acessos.this, TelaInicio.class);
            startActivity(rotacionar);
        });
        Button btnPsicologo = findViewById(R.id.btnPsicologo);
        btnPsicologo.setOnClickListener(v -> {
            Intent rotacionar = new Intent(Acessos.this, TelaInicio.class);
            startActivity(rotacionar);
        });
        Glassmorphism.aplicarLiquidGlass(btnColaborador);
        Glassmorphism.aplicarLiquidGlass(btnEmpresa);
        Glassmorphism.aplicarLiquidGlass(btnPsicologo);
    }
}