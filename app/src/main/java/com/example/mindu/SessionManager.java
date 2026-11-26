package com.example.mindu;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public final class SessionManager {
    private static final String PREFS = "mindu";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_TYPE = "tipoUsuario";
    private static final String KEY_STATUS = "statusUsuario";
    private static final String KEY_USER_ID = "idUsuario";
    private static final String KEY_NAME = "nomeUsuario";
    private static final String KEY_PENDING = "validandoLogin";

    private SessionManager() { }

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static String getToken(Context context) { return prefs(context).getString(KEY_TOKEN, null); }
    public static String getType(Context context) { return prefs(context).getString(KEY_TYPE, null); }
    public static String getStatus(Context context) { return prefs(context).getString(KEY_STATUS, null); }
    public static String getUserId(Context context) { return prefs(context).getString(KEY_USER_ID, null); }
    public static String getName(Context context) { return prefs(context).getString(KEY_NAME, null); }
    public static boolean isValidatingLogin(Context context) { return prefs(context).getBoolean(KEY_PENDING, false); }

    public static void saveName(Context context, String name) {
        if (name == null || name.trim().isEmpty()) return;
        prefs(context).edit().putString(KEY_NAME, name.trim()).apply();
    }

    public static void beginValidation(Context context, String token) {
        prefs(context).edit().putString(KEY_TOKEN, token).putBoolean(KEY_PENDING, true)
                .remove(KEY_TYPE).remove(KEY_STATUS).remove(KEY_NAME).apply();
    }

    public static void save(Context context, String token, String type, String status) {
        save(context, token, type, status, null);
    }

    public static void save(Context context, String token, String type, String status, String userId) {
        save(context, token, type, status, userId, getName(context));
    }

    public static void save(Context context, String token, String type, String status, String userId, String name) {
        SharedPreferences.Editor editor = prefs(context).edit()
                .putString(KEY_TOKEN, token).putString(KEY_TYPE, type)
                .putString(KEY_STATUS, status == null ? "" : status)
                .putString(KEY_USER_ID, userId).putBoolean(KEY_PENDING, false);
        if (name == null || name.trim().isEmpty()) editor.remove(KEY_NAME);
        else editor.putString(KEY_NAME, name.trim());
        editor.apply();
    }

    public static void clear(Context context) {
        prefs(context).edit().remove(KEY_TOKEN).remove(KEY_TYPE).remove(KEY_STATUS)
                .remove(KEY_USER_ID).remove(KEY_NAME).remove(KEY_PENDING).apply();
    }

    public static boolean guard(Activity activity, String expectedType) {
        String token = getToken(activity);
        String type = getType(activity);
        if (token != null && type != null && expectedType.equalsIgnoreCase(type)) {
            String status = getStatus(activity);
            boolean approvalRequired = "CLIENTE".equalsIgnoreCase(expectedType)
                    || "PROFISSIONAL".equalsIgnoreCase(expectedType);
            if (!approvalRequired || "APROVADO".equalsIgnoreCase(status)) return true;
            boolean waiting = ("CLIENTE".equalsIgnoreCase(expectedType)
                    && "AGUARDANDO_APROVACAO".equalsIgnoreCase(status))
                    || ("PROFISSIONAL".equalsIgnoreCase(expectedType)
                    && "PENDENTE".equalsIgnoreCase(status));
            if (waiting) {
                Intent pending = new Intent(activity, ContaPendenteActivity.class);
                pending.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                activity.startActivity(pending);
                activity.finish();
                return false;
            }
        }
        clear(activity);
        returnToAccess(activity, null);
        return false;
    }

    public static void logout(Activity activity) {
        clear(activity);
        returnToAccess(activity, null);
    }

    public static void expireSession(Context context) {
        clear(context);
        Intent intent = new Intent(context, Acessos.class);
        intent.putExtra("session_message", "Sessão expirada. Entre novamente.");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }

    public static void returnToAccess(Activity activity, String message) {
        Intent intent = new Intent(activity, Acessos.class);
        if (message != null) intent.putExtra("session_message", message);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
        activity.finish();
    }
}
