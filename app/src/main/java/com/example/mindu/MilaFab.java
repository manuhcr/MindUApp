package com.example.mindu;

import android.app.Activity;
import android.content.Intent;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;

/** Reusable Mila shortcut for authenticated application screens. */
public final class MilaFab {
    private MilaFab() { }

    public static void attach(Activity activity) {
        View existing = activity.findViewById(R.id.fabMila);
        if (existing == null) existing = activity.findViewById(R.id.fabMilaBeneficiario);
        if (existing != null) {
            existing.setOnClickListener(v -> open(activity));
            return;
        }

        FrameLayout root = activity.findViewById(android.R.id.content);
        if (root == null) return;
        ImageButton button = new ImageButton(activity);
        button.setImageResource(R.drawable.mila_light);
        button.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);
        button.setBackgroundResource(R.drawable.bg_fab_mila);
        button.setClipToOutline(true);
        button.setContentDescription("Abrir conversa com a Mila");
        button.setElevation(dp(activity, 8));
        button.setOnClickListener(v -> open(activity));

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(dp(activity, 60), dp(activity, 60),
                Gravity.BOTTOM | Gravity.END);
        params.setMargins(0, 0, dp(activity, 20), dp(activity, 88));
        root.addView(button, params);
    }

    private static void open(Activity activity) {
        activity.startActivity(new Intent(activity, ChatMilaActivity.class));
    }

    private static int dp(Activity activity, int value) {
        return (int) (value * activity.getResources().getDisplayMetrics().density + 0.5f);
    }
}
