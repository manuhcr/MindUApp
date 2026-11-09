package com.example.mindu;
import android.content.Intent;

// Color = permite trabalhar com cores, inclusive cores em hexadecimal
import android.graphics.Color;

// GradientDrawable = permite criar um fundo desenhável
// com cor, borda e cantos arredondados
import android.graphics.drawable.GradientDrawable;

import android.os.Bundle;
import android.view.View;

// ViewOutlineProvider = define o formato usado para recortar a View
// e também para calcular a sombra da elevação
import android.view.ViewOutlineProvider;

import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

// BlurTarget = informa ao BlurView qual conteúdo ele deve observar
// para produzir o desfoque

import eightbitlab.com.blurview.BlurTarget;

// BlurView = componente responsável pelo efeito de desfoque
import eightbitlab.com.blurview.BlurView;

public class Acessos extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // =========================================================
        // EDGE TO EDGE
        // =========================================================

        // Permite que o conteúdo da Activity ocupe toda a tela,
        // inclusive as áreas próximas à barra de status e navegação.
        EdgeToEdge.enable(this);

        // Carrega o arquivo XML dessa tela.
        // Aqui o Android transforma o activity_acessos.xml
        // em Views que podemos manipular pelo Java.
        setContentView(R.layout.activity_acessos);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
            }
        );

        // =========================================================
        // 1. TEXTO
        // =========================================================

        // Procuramos no XML o TextView que possui o ID "texto".
        TextView txtPerfil = findViewById(R.id.texto);


        // Texto completo que será mostrado na tela.
        String textoCompleto = "Para começar, precisamos entender qual o seu perfil de acesso.";


        // Esse é o trecho que queremos deixar em negrito.
        String trechoParaNegrito = "perfil de acesso.";


        // SpannableString permite formatar apenas uma parte
        // de uma String.
        //
        // Se usássemos apenas:
        //
        // txtPerfil.setText(...)
        //
        // todo o texto teria a mesma formatação.

        android.text.SpannableString spannable = new android.text.SpannableString(textoCompleto);

        int inicio = textoCompleto.indexOf(trechoParaNegrito);

        int fim = inicio + trechoParaNegrito.length();


        // Se o trecho realmente foi encontrado...
        if (inicio >= 0) {

            // ...aplicamos negrito somente entre "inicio" e "fim".
            spannable.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD),
                    inicio,
                    fim,
                    android.text.SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }

        txtPerfil.setText(spannable);


        // ==========================================
        // 2. BOTÕES
        // ==========================================

        Button btnColaborador = findViewById(R.id.btnColaborador);
        Button btnEmpresa = findViewById(R.id.btnEmpresa);
        Button btnPsicologo = findViewById(R.id.btnPsicologo);
        Button btnAdmin = findViewById(R.id.btnAdmin);


        // ==========================================
        // 3. BLURVIEWS
        // ==========================================

        // =========================================================
        // 3. BLURTARGET E BLURVIEWS
        // =========================================================

        /*
         * AQUI ESTÁ A PARTE MAIS IMPORTANTE DO EFEITO DE VIDRO.
         *
         * Existem três conceitos diferentes:
         *
         * BlurTarget
         *      ↓
         * conteúdo que pode ser observado pelo BlurView
         *
         * BlurView
         *      ↓
         * superfície de vidro que mostra esse conteúdo desfocado
         *
         * Button
         *      ↓
         * fica DENTRO do BlurView e continua nítido
         */


        // =========================================================
        // BLUR TARGET
        // =========================================================

        // Pegamos o BlurTarget definido no XML.
        BlurTarget blurTarget = findViewById(R.id.blurTarget);


        /*
         * O BlurTarget contém o conteúdo que está atrás do vidro.
         *
         * No nosso XML, ele contém:
         *
         * - a imagem de fundo
         * - o texto
         *
         * Portanto, quando o BlurView estiver sobre essa região,
         * ele consegue capturar esse conteúdo e aplicar o desfoque.
         */


        // =========================================================
        // BLUR VIEWS
        // =========================================================

        // Cada um desses representa a "placa de vidro"
        // de um dos três botões.

        BlurView blurColaborador = findViewById(R.id.blurColaborador);
        BlurView blurEmpresa = findViewById(R.id.blurEmpresa);
        BlurView blurPsicologo = findViewById(R.id.blurPsicologo);
        BlurView blurAdmin = findViewById(R.id.blurAdmin);


        // ==========================================
        // 4. CONFIGURAÇÃO DO BLUR
        // ==========================================

        blurColaborador.setupWith(blurTarget).setBlurRadius(20f);
        blurEmpresa.setupWith(blurTarget).setBlurRadius(20f);
        blurPsicologo.setupWith(blurTarget).setBlurRadius(20f);
        blurAdmin.setupWith(blurTarget).setBlurRadius(20f);



        // =========================================================
        // 5. APLICAÇÃO DO VISUAL DO VIDRO
        // =========================================================

        /*
         * O BlurView sozinho resolve o DESFOQUE.
         *
         * Mas ele não sabe que nosso design possui:
         *
         * - cor #8294EF
         * - 22% de transparência
         * - cantos arredondados
         * - borda
         * - sombra
         *
         * Por isso criamos uma função separada
         * para montar o visual do vidro.
         */

        aplicarEfeitoVidroLiquido(blurColaborador);
        aplicarEfeitoVidroLiquido(blurEmpresa);
        aplicarEfeitoVidroLiquido(blurPsicologo);
        aplicarEfeitoVidroLiquido(blurAdmin);


        // ==========================================
        // 6. EVENTOS DE CLIQUE
        // ==========================================

        btnColaborador.setOnClickListener(v -> {
            Intent rotacionar = new Intent(Acessos.this, LoginBeneficiario.class);
            startActivity(rotacionar);
        });


        btnEmpresa.setOnClickListener(v -> {
            Intent rotacionar = new Intent(Acessos.this, LoginEmpresa.class);
            startActivity(rotacionar);
        });


        btnPsicologo.setOnClickListener(v -> {
            Intent rotacionar = new Intent(Acessos.this, LoginProfissional.class);
            startActivity(rotacionar);
        });
        btnAdmin.setOnClickListener(v -> {
            Intent intent = new Intent(Acessos.this, LoginAdmin.class);
            startActivity(intent);
        });
    }


    // ==========================================
    // EFEITO DE VIDRO
    // ==========================================

    /*
     * Essa função recebe uma View e transforma o fundo dela
     * no nosso estilo de vidro.
     *
     * IMPORTANTE:
     *
     * Ela NÃO cria o blur.
     *
     * O blur é criado aqui:
     *
     *     setupWith(blurTarget)
     *
     * Essa função cria apenas a aparência visual da superfície:
     *
     *     cor + transparência + borda + arredondamento + sombra
     *
     */
    private void aplicarEfeitoVidroLiquido(View view) {

        // Segurança:
        // se a View não existir, não tentamos modificá-la.
        if (view == null) return;

        /*
         * GradientDrawable é um Drawable que podemos configurar
         * programaticamente.
         *
         * Apesar do nome "GradientDrawable", ele também permite
         * simplesmente usar UMA cor.
         */
        GradientDrawable liquidGlass = new GradientDrawable();
        //Define o formato como um retângulo
        liquidGlass.setShape(GradientDrawable.RECTANGLE);
        liquidGlass.setOrientation(GradientDrawable.Orientation.TL_BR);


        // VIDRO DO FIGMA:
        // #8294EF com 22% de transparência
        /*
         * O Android usa a representação:
         *
         *     #AARRGGBB
         *
         * AA = Alpha
         * RR = Red
         * GG = Green
         * BB = Blue
         *
         *
         * 22% de 255 ≈ 56
         *
         * 56 em hexadecimal = 38
         *
         * Portanto:
         *
         * #8294EF
         *
         * vira:
         *
         * #388294EF
         *
         * onde:
         *
         * 38 = 22% de opacidade
         * 82 = vermelho
         * 94 = verde
         * EF = azul
         */

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
        /*
         * Multiplicamos por density porque o método
         * setCornerRadius() trabalha com pixels.
         */
        liquidGlass.setCornerRadius(24 * density);


        /*
         * Criamos uma borda de 1dp.
         */
        int strokeWidthPx = (int) (1 * density);
        //Cor da borda
        liquidGlass.setStroke(strokeWidthPx, Color.parseColor("#99F5F3F7"));
        /*
         * Agora o BlurView passa a ter:
         *
         *     fundo colorido/translúcido
         *
         * enquanto o próprio BlurView continua fazendo:
         *
         *     desfoque do conteúdo atrás dele.
         */
        view.setBackground(liquidGlass);
        /*
         * Dizemos ao Android:
         *
         * "Use o próprio background para definir o formato
         *  da View."
         *
         * Isso é importante porque queremos que o conteúdo
         * respeite os cantos arredondados.
         */
        view.setOutlineProvider(ViewOutlineProvider.BACKGROUND);
        /*
         * Faz o conteúdo da View ser cortado seguindo
         * o formato definido pelo Outline.
         *
         * Resultado:
         *
         * ┌─────────────────────┐
         * │                     │
         * │       VIDRO         │
         * │                     │
         * └─────────────────────┘
         *
         * com os cantos arredondados.
         */

        view.setClipToOutline(true);
        /*
         * Elevação cria uma pequena sensação de profundidade.
         *
         * 4dp = sombra discreta.
         *
         * Isso NÃO é o blur.
         *
         * É apenas a sombra da View.
         */
        view.setElevation(4 * density);
    }
}