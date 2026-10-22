package com.example.mindu.api.profissional;

public class ProfissionalRequest {

        private String nome;
        private String email;
        private String senha;
        private String tipo;
        private String[] tags;

        public ProfissionalRequest(
                String nome,
                String email,
                String senha,
                String tipo,
                String[] tags
        ) {
            this.nome = nome;
            this.email = email;
            this.senha = senha;
            this.tipo = tipo;
            this.tags = tags;
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

        public String getTipo() {
            return tipo;
        }

        public String[] getTags() {
            return tags;
        }
    }
