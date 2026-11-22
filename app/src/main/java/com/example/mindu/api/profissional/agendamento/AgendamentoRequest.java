package com.example.mindu.api.profissional.agendamento;

public class AgendamentoRequest {

    private String profissionalId;
    private String dataHora;
    private String modalidade;

    public AgendamentoRequest(String profissionalId, String dataHora, String modalidade) {
        this.profissionalId = profissionalId;
        this.dataHora = dataHora;
        this.modalidade = modalidade;
    }

    public String getProfissionalId() {
        return profissionalId;
    }

    public String getDataHora() {
        return dataHora;
    }

    public String getModalidade() {
        return modalidade;
    }
}