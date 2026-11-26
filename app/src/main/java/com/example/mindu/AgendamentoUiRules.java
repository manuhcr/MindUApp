package com.example.mindu;

public final class AgendamentoUiRules {
    private AgendamentoUiRules() { }

    public static boolean belongsToDate(String dateTime, String date) {
        return dateTime != null && date != null && !date.trim().isEmpty()
                && dateTime.startsWith(date.trim());
    }

    public static boolean isActive(String status) {
        return "AGENDADO".equalsIgnoreCase(status == null ? "" : status.trim());
    }

    public static boolean supportsOnline(String modality) {
        if (modality == null) return false;
        String value = modality.trim();
        return "VIRTUAL".equalsIgnoreCase(value)
                || "HIBRIDO".equalsIgnoreCase(value)
                || "ONLINE".equalsIgnoreCase(value);
    }
}
