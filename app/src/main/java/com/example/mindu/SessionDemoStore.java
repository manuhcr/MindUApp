package com.example.mindu;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.mindu.api.profissional.agendamento.AgendamentoResponse;

/** Local-only marker to demo the same appointment on one device. */
public final class SessionDemoStore {
    private SessionDemoStore() { }

    public static void markStarted(Context context, AgendamentoResponse appointment) {
        if (appointment == null || appointment.getId() == null || appointment.getId().trim().isEmpty()) return;
        preferences(context).edit().putString("started_id", appointment.getId()).apply();
    }

    public static boolean isStarted(Context context, String appointmentId) {
        return appointmentId != null && appointmentId.equals(preferences(context).getString("started_id", null));
    }

    public static void clear(Context context, String appointmentId) {
        if (isStarted(context, appointmentId)) preferences(context).edit().remove("started_id").apply();
    }

    private static SharedPreferences preferences(Context context) {
        return context.getApplicationContext().getSharedPreferences("mindu_session_demo", Context.MODE_PRIVATE);
    }
}
