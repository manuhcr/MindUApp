package com.example.mindu.api.profissional.agendamento;

public class ReagendarRequest {

    private String dataHora;

    public ReagendarRequest(String dataHora) {
        this.dataHora = dataHora;
    }

    public String getDataHora() {
        return dataHora;
    }
}
