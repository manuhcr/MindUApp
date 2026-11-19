package com.example.mindu.api.cliente;

public class ClienteRequest {

    private String nome;
    private String email;
    private String senha;
    private String empresaId;
    private String matricula;

    public ClienteRequest(String nome, String email, String senha,
                          String empresaId, String matricula) {
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.empresaId = empresaId;
        this.matricula = matricula;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public String getSenha() {
        return senha;
    }

    public String getEmpresaId() {
        return empresaId;
    }

    public String getMatricula() {
        return matricula;
    }
}
