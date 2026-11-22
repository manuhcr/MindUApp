package com.example.mindu.api.profissional.detalhe;

import java.util.List;

public class ProfissionalDetalheResponse {

    private String id;
    private String nome;
    private String tipo;
    private List<String> tags;
    private String fotoUrl;
    private String bannerUrl;

    public String getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getTipo() {
        return tipo;
    }

    public List<String> getTags() {
        return tags;
    }

    public String getFotoUrl() {
        return fotoUrl;
    }

    public String getBannerUrl() {
        return bannerUrl;
    }
}