package com.example.mindu;

public final class SaudacaoFormatter {
    private SaudacaoFormatter() { }

    public static String format(String name) {
        if (name == null || name.trim().isEmpty()) return "Olá, seja bem-vindo(a)!";
        return "Olá, " + name.trim() + "!";
    }
}
