package com.example.mindu.api;

import com.example.mindu.api.cadastro.EmpresaRequest;
import com.example.mindu.api.cadastro.EmpresaResponse;
import com.example.mindu.api.login.LoginRequest;
import com.example.mindu.api.login.LoginResponse;
import com.example.mindu.api.matriculas.MatriculasResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    @POST("v1/auth/login")
    Call<LoginResponse> login(@Body LoginRequest body);

    @POST("v1/empresas")
    Call<EmpresaResponse> cadastrarEmpresa(@Body EmpresaRequest body);

    @POST("v1/empresas/{id}/matriculas")
    Call<List<MatriculasResponse>> gerarMatriculas(
            @Path("id") String empresaId,
            @Query("quantidade") int quantidade
    );

}
