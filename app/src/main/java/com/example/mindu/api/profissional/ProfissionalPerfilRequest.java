package com.example.mindu.api.profissional;

import java.util.List;

public class ProfissionalPerfilRequest {
    private String bio;
    private List<String> tags;
    public ProfissionalPerfilRequest(String bio, List<String> tags) { this.bio = bio; this.tags = tags; }
}
