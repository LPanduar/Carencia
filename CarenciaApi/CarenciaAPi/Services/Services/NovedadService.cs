using System.Data;
using CarenciaAPi.Services.Interfaces;
using Dapper;
using Domain.Dtos;
using Domain.Entities;
using Domain.Helpers;
using Microsoft.Data.SqlClient;

namespace CarenciaAPi.Services.Services
{
    public class NovedadService : INovedadService
    {
        private readonly string _connectionString;
        private readonly IArchivoService _archivos;

        public NovedadService(IConfiguration configuration, IArchivoService archivos)
        {
            _connectionString = configuration.GetConnectionString("DefaultConnection")!;
            _archivos = archivos;
        }

        // Búsqueda por texto + filtro por categoría
        public async Task<Response<List<Novedad>>> Obtener(string? categoria, string? buscar)
        {
            using var db = new SqlConnection(_connectionString);
            var lista = await db.QueryAsync<Novedad>(
                "spNovedadObtener",
                new { Categoria = Vacio(categoria), Buscar = Vacio(buscar) },
                commandType: CommandType.StoredProcedure);
            return new Response<List<Novedad>>(lista.ToList());
        }

        public async Task<Response<Novedad>> ById(int id)
        {
            using var db = new SqlConnection(_connectionString);
            var novedad = await db.QueryFirstOrDefaultAsync<Novedad>(
                "spNovedadById", new { PkNovedad = id }, commandType: CommandType.StoredProcedure)
                ?? throw new KeyNotFoundException("No se encontró la novedad");
            return new Response<Novedad>(novedad);
        }

        public async Task<Response<Novedad?>> Ultima()
        {
            using var db = new SqlConnection(_connectionString);
            var novedad = await db.QueryFirstOrDefaultAsync<Novedad>("spNovedadUltima", commandType: CommandType.StoredProcedure);
            return new Response<Novedad?>(novedad);
        }

        public async Task<Response<Novedad>> Crear(string titulo, string resumen, string contenido, string categoria,
                                                   bool destacada, IFormFile? imagen, int autorId)
        {
            ValidarCategoria(categoria);
            string? url = imagen != null ? await _archivos.Guardar(imagen, "novedades") : null;

            using var db = new SqlConnection(_connectionString);
            var novedad = await db.QuerySingleAsync<Novedad>(
                "spNovedadCrear",
                new
                {
                    Titulo = titulo, Resumen = resumen, Contenido = contenido, Categoria = categoria,
                    ImagenUrl = url, Destacada = destacada, FkAutor = autorId
                },
                commandType: CommandType.StoredProcedure);
            return new Response<Novedad>(novedad, "Novedad publicada");
        }

        public async Task<Response<Novedad>> Actualizar(int id, NovedadUpdateDto request)
        {
            ValidarCategoria(request.Categoria);
            using var db = new SqlConnection(_connectionString);
            var novedad = await db.QueryFirstOrDefaultAsync<Novedad>(
                "spNovedadActualizar",
                new
                {
                    PkNovedad = id, request.Titulo, request.Resumen, request.Contenido,
                    request.Categoria, request.Destacada
                },
                commandType: CommandType.StoredProcedure)
                ?? throw new KeyNotFoundException("No se encontró la novedad");
            return new Response<Novedad>(novedad, "Novedad actualizada");
        }

        public async Task<Response<Novedad>> Destacar(int id, bool destacada)
        {
            using var db = new SqlConnection(_connectionString);
            var novedad = await db.QueryFirstOrDefaultAsync<Novedad>(
                "spNovedadDestacar", new { PkNovedad = id, Destacada = destacada },
                commandType: CommandType.StoredProcedure)
                ?? throw new KeyNotFoundException("No se encontró la novedad");
            return new Response<Novedad>(novedad);
        }

        public async Task<Response<Novedad>> CambiarImagen(int id, IFormFile imagen)
        {
            var anterior = (await ById(id)).Result!;
            var url = await _archivos.Guardar(imagen, "novedades");

            using var db = new SqlConnection(_connectionString);
            var novedad = await db.QuerySingleAsync<Novedad>(
                "spNovedadImagen", new { PkNovedad = id, ImagenUrl = url }, commandType: CommandType.StoredProcedure);
            _archivos.Eliminar(anterior.ImagenUrl);
            return new Response<Novedad>(novedad, "Imagen actualizada");
        }

        public async Task<Response<bool>> Eliminar(int id)
        {
            var novedad = (await ById(id)).Result!;
            using var db = new SqlConnection(_connectionString);
            await db.ExecuteScalarAsync<int>("spNovedadEliminar", new { PkNovedad = id }, commandType: CommandType.StoredProcedure);
            _archivos.Eliminar(novedad.ImagenUrl);
            return new Response<bool>(true, "Novedad eliminada");
        }

        private static string? Vacio(string? s) => string.IsNullOrWhiteSpace(s) ? null : s.Trim();

        private static void ValidarCategoria(string categoria)
        {
            if (!Categorias.Novedad.Contains(categoria))
                throw new InvalidOperationException($"Categoría inválida. Usa: {string.Join(", ", Categorias.Novedad)}");
        }
    }
}
