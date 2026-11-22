package com.example.mindu;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.view.View;
import android.widget.ImageButton;

import androidx.constraintlayout.widget.ConstraintLayout;

final class LoginBackButton {
    private LoginBackButton() { }

    static void attach(Activity activity) {
        View root = activity.findViewById(R.id.main);
        if (!(root instanceof ConstraintLayout)) return;

        int size = (int) (48 * activity.getResources().getDisplayMetrics().density);
        ImageButton back = new ImageButton(activity);
        back.setImageResource(R.drawable.baseline_arrow_forward_24);
        back.setRotation(180f);
        back.setColorFilter(Color.rgb(117, 104, 154));
        back.setBackgroundColor(Color.TRANSPARENT);
        back.setContentDescription("Voltar à tela anterior");
        back.setScaleType(android.widget.ImageView.ScaleType.CENTER_INSIDE);
        back.setPadding(size / 4, size / 4, size / 4, size / 4);

        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(size, size);
        params.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
        params.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
        int margin = (int) (8 * activity.getResources().getDisplayMetrics().density);
        params.setMargins(margin, margin, 0, 0);
        back.setLayoutParams(params);
        back.setElevation(8 * activity.getResources().getDisplayMetrics().density);
        back.setOnClickListener(v -> {
            if (!activity.isTaskRoot()) {
                activity.finish();
                return;
            }
            Intent intent = new Intent(activity, Acessos.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            activity.startActivity(intent);
            activity.finish();
        });

        ((ConstraintLayout) root).addView(back);
    }
}
