package com.example.mindu;
import android.content.Context;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Shader;
import android.util.AttributeSet;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

public class GradienteTitulo extends AppCompatTextView {

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
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        // O gradiente só é gerado se o tamanho do componente mudar ou for definido
        if (changed) {
            int[] cores = new int[]{
                    Color.parseColor("#683FE6"), // 15%
                    Color.parseColor("#8294EF"), // 57%
                    Color.parseColor("#3D366D")  // 89%
            };

            float[] posicoes = new float[]{0.15f, 0.57f, 0.89f};

            float larguraDoTexto = getPaint().measureText(getText().toString());

            Shader shader = new LinearGradient(
                    0, 0, 0, getHeight(),
                    cores,
                    posicoes,
                    Shader.TileMode.CLAMP
            );


        }
    }
}
