package com.example.mindu.api.profissional.detalhe;

import java.util.List;
import com.example.mindu.api.profissional.local.LocalResponse;

public class ProfissionalDetalheResponse {

    private String id;
    private String nome;
    private String tipo;
    private String bio;
    private List<String> tags;
    private String fotoUrl;
    private String bannerUrl;
    private List<LocalResponse> locais;
    private List<com.example.mindu.api.profissional.disponibilidade.DisponibilidadeResponse> disponibilidadeSemanal;

    public String getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getTipo() {
        return tipo;
    }

    public String getBio() { return bio; }

    public List<String> getTags() {
        return tags;
    }

    public String getFotoUrl() {
        return fotoUrl;
    }

    public String getBannerUrl() {
        return bannerUrl;
    }

    public List<LocalResponse> getLocais() { return locais; }
    public List<com.example.mindu.api.profissional.disponibilidade.DisponibilidadeResponse> getDisponibilidadeSemanal() { return disponibilidadeSemanal; }
}
