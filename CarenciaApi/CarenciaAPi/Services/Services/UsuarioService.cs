using System.Data;
using CarenciaAPi.Helpers;
using CarenciaAPi.Services.Interfaces;
using Dapper;
using Domain.Dtos;
using Domain.Entities;
using Domain.Helpers;
using Microsoft.AspNetCore.Identity;
using Microsoft.Data.SqlClient;

namespace CarenciaAPi.Services.Services
{
    // Dapper + procedimientos almacenados
    public class UsuarioService : IUsuarioService
    {
        private const int RolAdmin = 1;

        private readonly string _connectionString;
        private readonly IPasswordHasher<Usuario> _passwordHasher;
        private readonly ITokenService _tokenService;

        public UsuarioService(IConfiguration configuration, IPasswordHasher<Usuario> passwordHasher, ITokenService tokenService)
        {
            _connectionString = configuration.GetConnectionString("DefaultConnection")!;
            _passwordHasher = passwordHasher;
            _tokenService = tokenService;
        }

        // POST api/Usuario (sólo Admin): crea otra cuenta de administrador del equipo
        public async Task<Response<Usuario>> CrearAdmin(RegistroDto request)
        {
            using var db = new SqlConnection(_connectionString);
            var usuario = await db.QuerySingleAsync<Usuario>(
                "spUsuarioRegistrar",
                new
                {
                    request.Nombre,
                    request.UserName,
                    Password = _passwordHasher.HashPassword(null!, request.Password),
                    FkRol = RolAdmin
                },
                commandType: CommandType.StoredProcedure);

            return new Response<Usuario>(usuario, "Administrador creado");
        }

        private async Task<Response<LoginResponseDto>> Login(LoginDto request)
        {
            using var db = new SqlConnection(_connectionString);
            var usuario = await db.QueryFirstOrDefaultAsync<Usuario>(
                "spUsuarioLogin", new { request.UserName }, commandType: CommandType.StoredProcedure);

            if (usuario == null ||
                _passwordHasher.VerifyHashedPassword(usuario, usuario.Password, request.Password) == PasswordVerificationResult.Failed)
                throw new UnauthorizedAccessException("Usuario o contraseña incorrectos");

            return new Response<LoginResponseDto>(_tokenService.Crear(usuario), "Login correcto");
        }

        // Login exclusivo del panel de administración: valida credenciales y además el rol
        public async Task<Response<LoginResponseDto>> LoginAdmin(LoginDto request)
        {
            var login = await Login(request);
            if (login.Result!.Usuario.FkRol != RolAdmin)
                throw new ProhibidoException("Esta cuenta no tiene permisos de administrador");

            login.Message = "Bienvenido al panel de administración";
            return login;
        }

        public async Task<Response<List<Usuario>>> Obtener()
        {
            using var db = new SqlConnection(_connectionString);
            var usuarios = await db.QueryAsync<Usuario>("spUsuarioObtener", commandType: CommandType.StoredProcedure);
            return new Response<List<Usuario>>(usuarios.ToList());
        }

        public async Task<Response<Usuario>> ById(int id)
        {
            using var db = new SqlConnection(_connectionString);
            var usuario = await db.QueryFirstOrDefaultAsync<Usuario>(
                "spUsuarioById", new { PkUsuario = id }, commandType: CommandType.StoredProcedure)
                ?? throw new KeyNotFoundException("No se encontró el usuario");
            return new Response<Usuario>(usuario);
        }

        public async Task<Response<Usuario>> Actualizar(int id, UsuarioUpdateDto request)
        {
            using var db = new SqlConnection(_connectionString);
            var usuario = await db.QueryFirstOrDefaultAsync<Usuario>(
                "spUsuarioActualizar",
                new { PkUsuario = id, request.Nombre, request.Estatus, request.FkRol },
                commandType: CommandType.StoredProcedure)
                ?? throw new KeyNotFoundException("No se encontró el usuario");
            return new Response<Usuario>(usuario, "Usuario actualizado");
        }

        public async Task<Response<bool>> Eliminar(int id)
        {
            using var db = new SqlConnection(_connectionString);
            var filas = await db.ExecuteScalarAsync<int>(
                "spUsuarioEliminar", new { PkUsuario = id }, commandType: CommandType.StoredProcedure);
            if (filas == 0) throw new KeyNotFoundException("No se encontró el usuario");
            return new Response<bool>(true, "Usuario eliminado");
        }
    }
}
