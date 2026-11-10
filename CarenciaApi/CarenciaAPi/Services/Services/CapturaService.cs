using System.Data;
using CarenciaAPi.Helpers;
using CarenciaAPi.Services.Interfaces;
using Dapper;
using Domain.Dtos;
using Domain.Entities;
using Domain.Helpers;
using Microsoft.Data.SqlClient;

namespace CarenciaAPi.Services.Services
{
    public class CapturaService : ICapturaService
    {
        private readonly string _connectionString;
        private readonly IArchivoService _archivos;

        public CapturaService(IConfiguration configuration, IArchivoService archivos)
        {
            _connectionString = configuration.GetConnectionString("DefaultConnection")!;
            _archivos = archivos;
        }

        public async Task<Response<List<Captura>>> Obtener(string? categoria, string? buscar, int? usuarioId)
        {
            using var db = new SqlConnection(_connectionString);
            var lista = await db.QueryAsync<Captura>(
                "spCapturaObtener",
                new
                {
                    Categoria = string.IsNullOrWhiteSpace(categoria) ? null : categoria.Trim(),
                    Buscar = string.IsNullOrWhiteSpace(buscar) ? null : buscar.Trim(),
                    FkUsuario = usuarioId
                },
                commandType: CommandType.StoredProcedure);
            return new Response<List<Captura>>(lista.ToList());
        }

        public async Task<Response<Captura>> ById(int id)
        {
            using var db = new SqlConnection(_connectionString);
            var captura = await db.QueryFirstOrDefaultAsync<Captura>(
                "spCapturaById", new { PkCaptura = id }, commandType: CommandType.StoredProcedure)
                ?? throw new KeyNotFoundException("No se encontró la captura");
            return new Response<Captura>(captura);
        }

        public async Task<Response<Captura>> Crear(string titulo, string categoria, IFormFile imagen, int usuarioId)
        {
            ValidarCategoria(categoria);
            var url = await _archivos.Guardar(imagen, "capturas");

            using var db = new SqlConnection(_connectionString);
            var captura = await db.QuerySingleAsync<Captura>(
                "spCapturaCrear",
                new { Titulo = titulo, Categoria = categoria, ImagenUrl = url, FkUsuario = usuarioId },
                commandType: CommandType.StoredProcedure);
            return new Response<Captura>(captura, "Foto agregada a la galería");
        }

        // El permiso (sólo Admin) lo valida el controlador con [Authorize(Roles = "Admin")]
        public async Task<Response<Captura>> Actualizar(int id, CapturaUpdateDto request)
        {
            ValidarCategoria(request.Categoria);
            await ById(id); // 404 si no existe

            using var db = new SqlConnection(_connectionString);
            var captura = await db.QuerySingleAsync<Captura>(
                "spCapturaActualizar",
                new { PkCaptura = id, request.Titulo, request.Categoria },
                commandType: CommandType.StoredProcedure);
            return new Response<Captura>(captura, "Foto actualizada");
        }

        public async Task<Response<bool>> Eliminar(int id)
        {
            var captura = (await ById(id)).Result!;

            using var db = new SqlConnection(_connectionString);
            await db.ExecuteScalarAsync<int>("spCapturaEliminar", new { PkCaptura = id }, commandType: CommandType.StoredProcedure);
            _archivos.Eliminar(captura.ImagenUrl);
            return new Response<bool>(true, "Foto eliminada");
        }

        private static void ValidarCategoria(string categoria)
        {
            if (!Categorias.Captura.Contains(categoria))
                throw new InvalidOperationException($"Categoría inválida. Usa: {string.Join(", ", Categorias.Captura)}");
        }
    }
}
