package com.example.mindu.api.profissional;

import java.util.List;

public class ProfissionalResponse {

    private String id;
    private String nome;
    private String status;
    private String tipo;
    private List<String> tags;
    private List<String> modalidades;

    public String getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getStatus() {
        return status;
    }

    public String getTipo() {
        return tipo;
    }

    public List<String> getTags() {
        return tags;
    }

    public List<String> getModalidades() {
        return modalidades;
    }
}