package com.example.mindu.api.cliente.dados;

import com.google.gson.annotations.SerializedName;

public class DadosSensiveisResponse {
    @SerializedName("cns") private String cns;
    @SerializedName("dataNascimento") private String dataNascimento;
    @SerializedName("nomeMae") private String nomeMae;
    @SerializedName("genero") private String genero;

    public String getCns() { return cns; }
    public String getDataNascimento() { return dataNascimento; }
    public String getNomeMae() { return nomeMae; }
    public String getGenero() { return genero; }

    public boolean estaCompleto() {
        return cns != null && !cns.trim().isEmpty()
                && dataNascimento != null && !dataNascimento.trim().isEmpty()
                && nomeMae != null && !nomeMae.trim().isEmpty()
                && genero != null && !genero.trim().isEmpty();
    }
}
