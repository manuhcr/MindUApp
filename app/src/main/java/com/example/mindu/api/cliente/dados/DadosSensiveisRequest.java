package com.example.mindu.api.cliente.dados;

import com.google.gson.annotations.SerializedName;

public class DadosSensiveisRequest {
    @SerializedName("cns") private String cns;
    @SerializedName("dataNascimento") private String dataNascimento;
    @SerializedName("nomeMae") private String nomeMae;
    @SerializedName("genero") private String genero;

    public DadosSensiveisRequest(String cns, String dataNascimento, String nomeMae, String genero) {
        this.cns = cns;
        this.dataNascimento = dataNascimento;
        this.nomeMae = nomeMae;
        this.genero = genero;
    }
}
