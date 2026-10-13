package com.example.mindu.api;

import com.example.mindu.api.cadastro.EmpresaRequest;
import com.example.mindu.api.cadastro.EmpresaResponse;
import com.example.mindu.api.cliente.ClienteRequest;
import com.example.mindu.api.cliente.ClienteResponse;
import com.example.mindu.api.dashboard.DashboardResponse;
import com.example.mindu.api.login.LoginRequest;
import com.example.mindu.api.login.LoginResponse;
import com.example.mindu.api.matriculas.MatriculasResponse;
import com.example.mindu.api.planos.PlanoResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
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

    @GET("v1/empresas/{id}/dashboard")
    Call<List<DashboardResponse>> getDashboard(@Path("id") String empresaId);

    // PlanoResponse deve ter: id, nome, vagasTotais, porteIndicado.
    // Envie o id selecionado como planoId em EmpresaRequest.
    @GET("v1/planos")
    Call<List<PlanoResponse>> listarPlanos();



    @POST("v1/clientes")
    Call<ClienteResponse> cadastrarCliente(@Body ClienteRequest body);

}
