package com.akm.carencia.data

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

// Endpoints de CarenciaAPI (ver Swagger en http://localhost:5101/swagger)
interface ApiService {

    // ----- Usuario -----
    @POST("api/Usuario/login-admin")
    suspend fun loginAdmin(@Body body: LoginRequest): Response<ApiResponse<LoginResponse>>

    @PUT("api/Usuario/contrasena")
    suspend fun cambiarContrasena(@Body body: CambiarContrasenaRequest): Response<ApiResponse<Boolean>>

    @GET("api/Usuario/yo")
    suspend fun yo(): Response<ApiResponse<Usuario>>

    // ----- Niveles (GET) -----
    @GET("api/Nivel")
    suspend fun niveles(): Response<ApiResponse<List<Nivel>>>

    @GET("api/Nivel/{id}")
    suspend fun nivel(@Path("id") id: Int): Response<ApiResponse<Nivel>>

    // ----- Novedades (GET público, POST/PUT/PATCH/DELETE sólo Admin) -----
    @GET("api/Novedad")
    suspend fun novedades(
        @Query("categoria") categoria: String?,
        @Query("buscar") buscar: String?
    ): Response<ApiResponse<List<Novedad>>>

    @GET("api/Novedad/{id}")
    suspend fun novedad(@Path("id") id: Int): Response<ApiResponse<Novedad>>

    @GET("api/Novedad/ultima")
    suspend fun ultimaNovedad(): Response<ApiResponse<Novedad>>

    @Multipart
    @POST("api/Novedad")
    suspend fun crearNovedad(
        @Part("titulo") titulo: RequestBody,
        @Part("resumen") resumen: RequestBody,
        @Part("contenido") contenido: RequestBody,
        @Part("categoria") categoria: RequestBody,
        @Part("destacada") destacada: RequestBody,
        @Part imagen: MultipartBody.Part?
    ): Response<ApiResponse<Novedad>>

    @PUT("api/Novedad/{id}")
    suspend fun actualizarNovedad(
        @Path("id") id: Int,
        @Body body: NovedadUpdateRequest
    ): Response<ApiResponse<Novedad>>

    @PATCH("api/Novedad/{id}/destacar")
    suspend fun destacarNovedad(
        @Path("id") id: Int,
        @Body body: DestacarRequest
    ): Response<ApiResponse<Novedad>>

    @Multipart
    @PATCH("api/Novedad/{id}/imagen")
    suspend fun cambiarImagenNovedad(
        @Path("id") id: Int,
        @Part imagen: MultipartBody.Part
    ): Response<ApiResponse<Novedad>>

    @DELETE("api/Novedad/{id}")
    suspend fun eliminarNovedad(@Path("id") id: Int): Response<ApiResponse<Boolean>>

    // ----- Galería oficial (GET público; POST/PUT/DELETE sólo Admin) -----
    @GET("api/Captura")
    suspend fun capturas(
        @Query("categoria") categoria: String?,
        @Query("buscar") buscar: String?
    ): Response<ApiResponse<List<Captura>>>

    @Multipart
    @POST("api/Captura")
    suspend fun crearCaptura(
        @Part("titulo") titulo: RequestBody,
        @Part("categoria") categoria: RequestBody,
        @Part imagen: MultipartBody.Part
    ): Response<ApiResponse<Captura>>

    @PUT("api/Captura/{id}")
    suspend fun actualizarCaptura(
        @Path("id") id: Int,
        @Body body: CapturaUpdateRequest
    ): Response<ApiResponse<Captura>>

    @DELETE("api/Captura/{id}")
    suspend fun eliminarCaptura(@Path("id") id: Int): Response<ApiResponse<Boolean>>

    // ----- Estadísticas -----
    @GET("api/Estadistica")
    suspend fun estadisticas(): Response<ApiResponse<Estadistica>>
}
