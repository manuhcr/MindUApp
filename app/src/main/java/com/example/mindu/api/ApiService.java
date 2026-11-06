package com.example.mindu.api;

import com.example.mindu.api.cadastro.EmpresaRequest;
import com.example.mindu.api.cadastro.EmpresaResponse;
import com.example.mindu.api.cliente.ClienteRequest;
import com.example.mindu.api.cliente.ClienteResponse;
import com.example.mindu.api.cliente.dados.DadosSensiveisRequest;
import com.example.mindu.api.dashboard.DashboardResponse;
import com.example.mindu.api.login.LoginRequest;
import com.example.mindu.api.login.LoginResponse;
import com.example.mindu.api.login.MinhaContaResponse;
import com.example.mindu.api.login.TrocaSenhaRequest;
import com.example.mindu.api.matriculas.MatriculasResponse;
import com.example.mindu.api.planos.PlanoResponse;
import com.example.mindu.api.profissional.ProfissionalRequest;
import com.example.mindu.api.profissional.ProfissionalResponse;
import com.example.mindu.api.profissional.agendamento.AgendamentoRequest;
import com.example.mindu.api.profissional.agendamento.AgendamentoResponse;
import com.example.mindu.api.profissional.agendamento.ReagendarRequest;
import com.example.mindu.api.profissional.detalhe.ProfissionalDetalheResponse;
import com.example.mindu.api.profissional.disponibilidade.ConsultaHorariosRequest;
import com.example.mindu.api.profissional.disponibilidade.DisponibilidadeRequest;
import com.example.mindu.api.profissional.disponibilidade.DisponibilidadeResponse;
import com.example.mindu.api.profissional.local.LocalRequest;
import com.example.mindu.api.profissional.local.LocalResponse;

import java.time.LocalDateTime;
import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    @POST("v1/auth/login")
    Call<LoginResponse> login(@Body LoginRequest body);

    @GET("v1/auth/me")
    Call<MinhaContaResponse> minhaConta();

    @PUT("v1/auth/senha")
    Call<Void> trocarSenha(@Body TrocaSenhaRequest body);

    @POST("v1/empresas")
    Call<EmpresaResponse> cadastrarEmpresa(@Body EmpresaRequest body);

    @POST("v1/empresas/{id}/matriculas")
    Call<List<MatriculasResponse>> gerarMatriculas(
            @Path("id") String empresaId,
            @Query("quantidade") int quantidade
    );

    @POST("v1/empresas/me/matriculas")
    Call<List<MatriculasResponse>> gerarMinhasMatriculas(@Query("quantidade") int quantidade);

    @GET("v1/empresas/me/dashboard")
    Call<DashboardResponse> meuDashboard();

    @GET("v1/empresas/{id}/dashboard")
    Call<List<DashboardResponse>> getDashboard(@Path("id") String empresaId);

    // PlanoResponse deve ter: id, nome, vagasTotais, porteIndicado.
    // Envie o id selecionado como planoId em EmpresaRequest.
    @GET("v1/planos")
    Call<List<PlanoResponse>> listarPlanos();





    @POST("v1/clientes")
    Call<ClienteResponse> cadastrarCliente(@Body ClienteRequest body);

    @GET("v1/clientes/me")
    Call<ClienteResponse> meuPerfil();

    @PATCH("v1/clientes/me/dados-sensiveis")
    Call<ClienteResponse> completarMeuPerfil(@Body DadosSensiveisRequest body);

    @PATCH("v1/clientes/{id}/aprovacao")
    Call<ClienteResponse> aprovarCliente(@Path("id") String id, @Query("aprovado") boolean aprovado);

    @PATCH("v1/clientes/{id}/dados-sensiveis")
    Call<ClienteResponse> enviarDadosSensiveis(@Path("id") String id, @Body DadosSensiveisRequest body);

    @GET("v1/clientes")
    Call<List<ClienteResponse>> listarClientes(@Query("status") String status);

    @DELETE("v1/clientes/{id}")
    Call<Void> excluirFuncionario(@Path("id") String clienteId);



    @Multipart
    @POST("v1/profissionais")
    Call<ProfissionalResponse> cadastrar(
            @Part("nome") RequestBody nome,
            @Part("email") RequestBody email,
            @Part("senha") RequestBody senha,
            @Part("tipo") RequestBody tipo,
            @Part("tags") List<RequestBody> tags,
            @Part MultipartBody.Part fotoCarteirinha
    );

    @PATCH("v1/profissionais/{id}/aprovacao")
    Call<ProfissionalResponse> aprovarProfissional(@Path("id") String id, @Query("aprovado") boolean aprovado);

    @POST("v1/profissionais/{id}/locais")
    Call<LocalResponse> adicionarLocal(@Path("id") String id, @Body LocalRequest body);

    @PUT("v1/profissionais/{id}/disponibilidade")
    Call<List<DisponibilidadeResponse>> definirDisponibilidade(@Path("id") String id, @Body List<DisponibilidadeRequest> body);

    @GET("v1/profissionais")
    Call<List<ProfissionalResponse>> buscar(@Query("modalidade") String modalidade,
                                            @Query("nome") String nome,
                                            @Query("especialidade") String especialidade);

    @GET("v1/profissionais/{id}")
    Call<ProfissionalDetalheResponse> detalhes(@Path("id") String id);

    @POST("v1/profissionais/{id}/horarios-disponiveis")
    Call<List<LocalDateTime>> horarios(@Path("id") String id, @Body ConsultaHorariosRequest body);

    @GET("v1/profissionais/{id}/foto")
    Call<ResponseBody> foto(@Path("id") String id);

    @GET("v1/profissionais/{id}/banner")
    Call<ResponseBody> banner(@Path("id") String id);


    @GET("v1/admin/profissionais")
    Call<List<ProfissionalResponse>> listarProfissionais(@Query("status") String status);

    @GET("v1/admin/profissionais/pendentes")
    Call<List<ProfissionalResponse>> profissionaisPendentes();

    @GET("v1/admin/clientes")
    Call<List<ClienteResponse>> listarClientesAdmin(@Query("status") String status);

    @GET("v1/admin/empresas")
    Call<List<EmpresaResponse>> listarEmpresas();

    @GET("v1/admin/agendamentos")
    Call<List<AgendamentoResponse>> listarAgendamentos();

    @DELETE("v1/admin/empresas/{id}")
    Call<Void> excluirEmpresa(@Path("id") String empresaId);

    @DELETE("v1/admin/profissionais/{id}")
    Call<Void> excluirProfissional(@Path("id") String profissionalId);


    @POST("v1/agendamentos")
    Call<AgendamentoResponse> agendar(@Body AgendamentoRequest body);

    @GET("v1/agendamentos/meus")
    Call<List<AgendamentoResponse>> meusAgendamentos();

    @GET("v1/agendamentos/minha-agenda")
    Call<List<AgendamentoResponse>> minhaAgenda();

    @PATCH("v1/agendamentos/{id}/cancelamento")
    Call<AgendamentoResponse> cancelar(@Path("id") String id);

    @PATCH("v1/agendamentos/{id}/horario")
    Call<AgendamentoResponse> reagendar(@Path("id") String id, @Body ReagendarRequest body);


}