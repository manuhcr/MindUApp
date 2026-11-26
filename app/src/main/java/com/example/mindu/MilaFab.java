package com.example.mindu;

import android.app.Activity;
import android.content.Intent;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import androidx.constraintlayout.widget.ConstraintLayout;

/** Reusable Mila shortcut for authenticated application screens. */
public final class MilaFab {
    private MilaFab() { }

    public static void attach(Activity activity) {
        View existing = activity.findViewById(R.id.fabMila);
        if (existing == null) existing = activity.findViewById(R.id.fabMilaBeneficiario);
        if (existing == null) existing = activity.findViewById(R.id.fabMilaProfissional);
        View bottomNav = findBottomNavigation(activity);

        if (existing != null) {
            existing.setOnClickListener(v -> open(activity));
            reserveListSpace(activity, bottomNav);
            placeExisting(existing, bottomNav, activity);
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
        int bottom = dp(activity, 20);
        if (bottomNav != null) {
            ViewGroupMargin navMargin = navMargin(bottomNav);
            bottom += navMargin.height + navMargin.bottom;
            bottom += dp(activity, 18);
            reserveListSpace(activity, bottomNav);
        }
        params.setMargins(0, 0, dp(activity, 22), bottom);
        root.addView(button, params);
    }

    private static View findBottomNavigation(Activity activity) {
        View nav = activity.findViewById(R.id.navAdmin);
        if (nav == null) nav = activity.findViewById(R.id.navEmpresa);
        if (nav == null) nav = activity.findViewById(R.id.navBeneficiario);
        if (nav == null) nav = activity.findViewById(R.id.navProfissional);
        return nav;
    }

    private static void placeExisting(View button, View nav, Activity activity) {
        if (!(button.getLayoutParams() instanceof ConstraintLayout.LayoutParams)) return;
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) button.getLayoutParams();
        params.width = dp(activity, 60);
        params.height = dp(activity, 60);
        params.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
        params.startToStart = ConstraintLayout.LayoutParams.UNSET;
        params.startToEnd = ConstraintLayout.LayoutParams.UNSET;
        params.endToStart = ConstraintLayout.LayoutParams.UNSET;
        params.topToTop = ConstraintLayout.LayoutParams.UNSET;
        params.topToBottom = ConstraintLayout.LayoutParams.UNSET;
        params.bottomToBottom = nav == null ? ConstraintLayout.LayoutParams.PARENT_ID : ConstraintLayout.LayoutParams.UNSET;
        params.bottomToTop = nav == null ? ConstraintLayout.LayoutParams.UNSET : nav.getId();
        params.setMarginEnd(dp(activity, 22));
        params.bottomMargin = dp(activity, nav == null ? 20 : 18);
        button.setLayoutParams(params);
    }

    private static void reserveListSpace(Activity activity, View nav) {
        if (nav == null) return;
        int containerId = nav.getId() == R.id.navAdmin ? R.id.containerAdmin
                : nav.getId() == R.id.navEmpresa ? R.id.containerEmpresa
                : nav.getId() == R.id.navProfissional ? R.id.containerProfissional
                : R.id.containerBeneficiario;
        View container = activity.findViewById(containerId);
        if (container == null) return;
        int reserve = dp(activity, 78);
        if (container.getPaddingBottom() < reserve) {
            container.setPadding(container.getPaddingLeft(), container.getPaddingTop(),
                    container.getPaddingRight(), reserve);
        }
        if (container instanceof ViewGroup) ((ViewGroup) container).setClipToPadding(false);
    }

    private static ViewGroupMargin navMargin(View nav) {
        android.view.ViewGroup.LayoutParams raw = nav.getLayoutParams();
        int height = nav.getHeight();
        int bottom = 0;
        if (raw instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams margins = (ViewGroup.MarginLayoutParams) raw;
            if (height <= 0 && margins.height > 0) height = margins.height;
            bottom = margins.bottomMargin;
        } else if (height <= 0 && raw != null && raw.height > 0) {
            height = raw.height;
        }
        return new ViewGroupMargin(Math.max(0, height), Math.max(0, bottom));
    }

    private static void open(Activity activity) {
        activity.startActivity(new Intent(activity, ChatMilaActivity.class));
    }

    private static int dp(Activity activity, int value) {
        return (int) (value * activity.getResources().getDisplayMetrics().density + 0.5f);
    }

    private static final class ViewGroupMargin {
        final int height;
        final int bottom;
        ViewGroupMargin(int height, int bottom) { this.height = height; this.bottom = bottom; }
    }
}
