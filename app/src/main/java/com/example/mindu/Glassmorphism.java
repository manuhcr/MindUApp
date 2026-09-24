package com.example.mindu;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.View;

public class Glassmorphism {

    public static void aplicarLiquidGlass(View view) {
        if (view == null) return;

        GradientDrawable glassBackground = new GradientDrawable();
        glassBackground.setShape(GradientDrawable.RECTANGLE);

        // 1. GRADIENTE DE VIDRO (Cores e Opacidade iguaizinhas à imagem)
        // Criamos um gradiente que vai do topo-esquerdo para o canto inferior-direito
        glassBackground.setOrientation(GradientDrawable.Orientation.TL_BR);

        // Cores calculadas em Hexadecimal com Alpha (Opacidade) embutida:
        // #66FFFFFF = Branco com 40% de opacidade (Mais aceso no topo esquerdo)
        // #33FFFFFF = Branco com 20% de opacidade (Mais suave no centro/fim)
        int[] coresDoVidro = new int[] {
                Color.parseColor("#66FFFFFF"),
                Color.parseColor("#33FFFFFF")
        };
        glassBackground.setColors(coresDoVidro);

        // 2. CANTOS ARREDONDADOS (Medida exata da imagem)
        float radiusPx = 24 * view.getContext().getResources().getDisplayMetrics().density;
        glassBackground.setCornerRadius(radiusPx);

        // 3. BORDA BRANCA DE REFLEXO (Fina e bem definida)
        int strokeWidthPx = (int) (1.2f * view.getContext().getResources().getDisplayMetrics().density);
        // #80FFFFFF = Linha branca com 50% de opacidade para dar o contorno do vidro
        int strokeColor = Color.parseColor("#80FFFFFF");
        glassBackground.setStroke(strokeWidthPx, strokeColor);

        // 4. Aplica o fundo final na sua View
        view.setBackground(glassBackground);
    }
}
