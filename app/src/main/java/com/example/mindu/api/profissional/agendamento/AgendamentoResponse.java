package com.example.mindu.api.profissional.agendamento;

public class AgendamentoResponse {

    private String id;
    private String clienteId;
    private String clienteNome;
    private String profissionalId;
    private String profissionalNome;
    private String status;
    private String dataHora;
    private String modalidade;
    private String linkOnline;

    public String getId() {
        return id;
    }

    public String getClienteId() {
        return clienteId;
    }

    public String getClienteNome() {
        return clienteNome;
    }

    public String getProfissionalId() {
        return profissionalId;
    }

    public String getProfissionalNome() {
        return profissionalNome;
    }

    public String getStatus() {
        return status;
    }

    public String getDataHora() {
        return dataHora;
    }

    public String getModalidade() {
        return modalidade;
    }

    public String getLinkOnline() {
        return linkOnline;
    }
}