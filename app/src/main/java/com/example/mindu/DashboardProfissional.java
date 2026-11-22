package com.example.mindu;

import android.os.Bundle;
import android.content.Intent;
import android.view.Gravity;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class DashboardProfissional extends AppCompatActivity {
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!SessionManager.guard(this, "PROFISSIONAL")) return;

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setGravity(Gravity.CENTER);
        content.setPadding(32, 32, 32, 32);

        TextView greeting = new TextView(this);
        String name = getIntent().getStringExtra("nome_profissional");
        greeting.setText("Bem-vindo(a), " + (name == null ? "profissional" : name) + "!");
        greeting.setTextSize(22);
        greeting.setGravity(Gravity.CENTER);
        content.addView(greeting, new LinearLayout.LayoutParams(-1, -2));

        ImageButton mila = new ImageButton(this);
        mila.setImageResource(R.drawable.mila_light);
        mila.setBackgroundResource(R.drawable.bg_fab_mila);
        mila.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);
        mila.setClipToOutline(true);
        mila.setContentDescription("Abrir conversa com a Mila");
        mila.setOnClickListener(v ->
                startActivity(new Intent(DashboardProfissional.this, ChatMilaActivity.class)));
        LinearLayout.LayoutParams milaParams = new LinearLayout.LayoutParams(dp(76), dp(76));
        milaParams.topMargin = dp(28);
        milaParams.bottomMargin = dp(8);
        content.addView(mila, milaParams);

        TextView milaLabel = new TextView(this);
        milaLabel.setText("Converse com a Mila");
        milaLabel.setTextSize(16);
        milaLabel.setGravity(Gravity.CENTER);
        content.addView(milaLabel, new LinearLayout.LayoutParams(-1, -2));

        Button logout = new Button(this);
        logout.setText("Sair da conta");
        logout.setOnClickListener(v -> SessionManager.logout(this));
        content.addView(logout);
        setContentView(content);
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
