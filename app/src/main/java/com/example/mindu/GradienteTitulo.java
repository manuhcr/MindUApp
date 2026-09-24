package com.example.mindu;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.util.AttributeSet;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

public class GradienteTitulo extends AppCompatTextView {

    private Shader shaderConstruido = null;

    public GradienteTitulo(@NonNull Context context) {
        super(context);
    }

    public GradienteTitulo(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public GradienteTitulo(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // Cria o gradiente sempre que o tamanho mudar
        gerarGradiente(w, h);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        if (shaderConstruido == null && getWidth() > 0) {
            gerarGradiente(getWidth(), getHeight());
        }

        if (shaderConstruido != null) {
            getPaint().setShader(shaderConstruido);
        }

        super.onDraw(canvas);
    }

    private void gerarGradiente(int largura, int altura) {
        if (largura > 0) {
            int[] cores = new int[]{
                    Color.parseColor("#4d366d"),
                    Color.parseColor("#c688dd"),
            };

            float[] posicoes = new float[]{0.15f, 0.57f};

            // Criamos um gradiente horizontal estendido de ponta a ponta
            shaderConstruido = new LinearGradient(
                    0, 0, largura, 0,
                    cores,
                    posicoes,
                    Shader.TileMode.CLAMP
            );
        }
    }
}
