package com.example.mindu.api.profissional.disponibilidade;

public class DisponibilidadeRequest {
    private String diaSemana;
    private String horaInicio;
    private String horaFim;

    public DisponibilidadeRequest(String diaSemana, String horaInicio, String horaFim) {
        this.diaSemana = diaSemana; this.horaInicio = horaInicio; this.horaFim = horaFim;
    }
}
