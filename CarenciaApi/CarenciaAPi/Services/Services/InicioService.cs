using System.Data;
using CarenciaAPi.Services.Interfaces;
using Dapper;
using Domain.Dtos;
using Domain.Entities;
using Domain.Helpers;
using Microsoft.Data.SqlClient;

namespace CarenciaAPi.Services.Services
{
    // Contenido de la pantalla de Inicio: descripción, etiquetas, personajes y "Acerca de"
    public class InicioService : IInicioService
    {
        private readonly string _connectionString;
        private readonly IArchivoService _archivos;

        public InicioService(IConfiguration configuration, IArchivoService archivos)
        {
            _connectionString = configuration.GetConnectionString("DefaultConnection")!;
            _archivos = archivos;
        }

        // Una sola llamada con los 4 conjuntos de resultados
        public async Task<Response<Inicio>> Obtener()
        {
            using var db = new SqlConnection(_connectionString);
            using var multi = await db.QueryMultipleAsync("spInicioObtener", commandType: CommandType.StoredProcedure);
            var inicio = new Inicio
            {
                Descripcion = await multi.ReadFirstOrDefaultAsync<string>() ?? string.Empty,
                Etiquetas = (await multi.ReadAsync<InicioEtiqueta>()).ToList(),
                Personajes = (await multi.ReadAsync<Personaje>()).ToList(),
                Acerca = (await multi.ReadAsync<AcercaItem>()).ToList()
            };
            return new Response<Inicio>(inicio);
        }

        public async Task<Response<string>> ActualizarDescripcion(string descripcion)
        {
            using var db = new SqlConnection(_connectionString);
            var texto = await db.QuerySingleAsync<string>(
                "spInicioDescripcionActualizar", new { Descripcion = Requerido(descripcion, "la descripción") },
                commandType: CommandType.StoredProcedure);
            return new Response<string>(texto, "Descripción actualizada");
        }

        // ---------- Etiquetas ----------
        public async Task<Response<InicioEtiqueta>> CrearEtiqueta(InicioEtiquetaCreateDto request)
        {
            if (!Categorias.Etiqueta.Contains(request.Tipo))
                throw new InvalidOperationException($"Tipo inválido. Usa: {string.Join(", ", Categorias.Etiqueta)}");
            using var db = new SqlConnection(_connectionString);
            var etiqueta = await db.QuerySingleAsync<InicioEtiqueta>(
                "spInicioEtiquetaCrear", new { request.Tipo, Texto = Requerido(request.Texto, "el texto") },
                commandType: CommandType.StoredProcedure);
            return new Response<InicioEtiqueta>(etiqueta, "Etiqueta agregada");
        }

        public async Task<Response<InicioEtiqueta>> ActualizarEtiqueta(int id, string texto)
        {
            var limpio = Requerido(texto, "el texto");
            if (limpio.Length > 40) throw new InvalidOperationException("La etiqueta admite máximo 40 caracteres");
            using var db = new SqlConnection(_connectionString);
            var etiqueta = await db.QueryFirstOrDefaultAsync<InicioEtiqueta>(
                "spInicioEtiquetaActualizar", new { PkInicioEtiqueta = id, Texto = limpio },
                commandType: CommandType.StoredProcedure)
                ?? throw new KeyNotFoundException("No se encontró la etiqueta");
            return new Response<InicioEtiqueta>(etiqueta, "Etiqueta actualizada");
        }

        public async Task<Response<bool>> EliminarEtiqueta(int id)
        {
            using var db = new SqlConnection(_connectionString);
            var filas = await db.ExecuteScalarAsync<int>(
                "spInicioEtiquetaEliminar", new { PkInicioEtiqueta = id }, commandType: CommandType.StoredProcedure);
            if (filas == 0) throw new KeyNotFoundException("No se encontró la etiqueta");
            return new Response<bool>(true, "Etiqueta eliminada");
        }

        // ---------- Personajes ----------
        public async Task<Response<Personaje>> PersonajeById(int id)
        {
            using var db = new SqlConnection(_connectionString);
            var personaje = await db.QueryFirstOrDefaultAsync<Personaje>(
                "spPersonajeById", new { PkPersonaje = id }, commandType: CommandType.StoredProcedure)
                ?? throw new KeyNotFoundException("No se encontró el personaje");
            return new Response<Personaje>(personaje);
        }

        public async Task<Response<Personaje>> CrearPersonaje(string nombre, string rol, string descripcion,
                                                             string? rasgos, IFormFile? imagen)
        {
            string? url = imagen != null ? await _archivos.Guardar(imagen, "personajes") : null;
            using var db = new SqlConnection(_connectionString);
            var personaje = await db.QuerySingleAsync<Personaje>(
                "spPersonajeCrear",
                new
                {
                    Nombre = Requerido(nombre, "el nombre"), Rol = Requerido(rol, "el rol"),
                    Descripcion = Requerido(descripcion, "la descripción"), Rasgos = Opcional(rasgos), ImagenUrl = url
                },
                commandType: CommandType.StoredProcedure);
            return new Response<Personaje>(personaje, "Personaje agregado");
        }

        public async Task<Response<Personaje>> ActualizarPersonaje(int id, PersonajeUpdateDto request)
        {
            using var db = new SqlConnection(_connectionString);
            var personaje = await db.QueryFirstOrDefaultAsync<Personaje>(
                "spPersonajeActualizar",
                new
                {
                    PkPersonaje = id, Nombre = Requerido(request.Nombre, "el nombre"), Rol = Requerido(request.Rol, "el rol"),
                    Descripcion = Requerido(request.Descripcion, "la descripción"), Rasgos = Opcional(request.Rasgos)
                },
                commandType: CommandType.StoredProcedure)
                ?? throw new KeyNotFoundException("No se encontró el personaje");
            return new Response<Personaje>(personaje, "Personaje actualizado");
        }

        public async Task<Response<Personaje>> CambiarImagenPersonaje(int id, IFormFile imagen)
        {
            var anterior = (await PersonajeById(id)).Result!;
            var url = await _archivos.Guardar(imagen, "personajes");
            using var db = new SqlConnection(_connectionString);
            var personaje = await db.QuerySingleAsync<Personaje>(
                "spPersonajeImagen", new { PkPersonaje = id, ImagenUrl = url }, commandType: CommandType.StoredProcedure);
            _archivos.Eliminar(anterior.ImagenUrl);
            return new Response<Personaje>(personaje, "Imagen actualizada");
        }

        public async Task<Response<bool>> EliminarPersonaje(int id)
        {
            var personaje = (await PersonajeById(id)).Result!;
            using var db = new SqlConnection(_connectionString);
            await db.ExecuteScalarAsync<int>("spPersonajeEliminar", new { PkPersonaje = id }, commandType: CommandType.StoredProcedure);
            _archivos.Eliminar(personaje.ImagenUrl);
            return new Response<bool>(true, "Personaje eliminado");
        }

        // ---------- Acerca de ----------
        public async Task<Response<AcercaItem>> CrearAcerca(AcercaItemCreateDto request)
        {
            if (!Categorias.Acerca.Contains(request.Seccion))
                throw new InvalidOperationException($"Sección inválida. Usa: {string.Join(", ", Categorias.Acerca)}");
            using var db = new SqlConnection(_connectionString);
            var item = await db.QuerySingleAsync<AcercaItem>(
                "spAcercaItemCrear", new { request.Seccion, Texto = Requerido(request.Texto, "el texto") },
                commandType: CommandType.StoredProcedure);
            return new Response<AcercaItem>(item, "Agregado");
        }

        public async Task<Response<AcercaItem>> ActualizarAcerca(int id, string texto)
        {
            using var db = new SqlConnection(_connectionString);
            var item = await db.QueryFirstOrDefaultAsync<AcercaItem>(
                "spAcercaItemActualizar", new { PkAcercaItem = id, Texto = Requerido(texto, "el texto") },
                commandType: CommandType.StoredProcedure)
                ?? throw new KeyNotFoundException("No se encontró el elemento");
            return new Response<AcercaItem>(item, "Actualizado");
        }

        public async Task<Response<bool>> EliminarAcerca(int id)
        {
            using var db = new SqlConnection(_connectionString);
            var filas = await db.ExecuteScalarAsync<int>(
                "spAcercaItemEliminar", new { PkAcercaItem = id }, commandType: CommandType.StoredProcedure);
            if (filas == 0) throw new KeyNotFoundException("No se encontró el elemento");
            return new Response<bool>(true, "Eliminado");
        }

        private static string Requerido(string? s, string campo) =>
            string.IsNullOrWhiteSpace(s) ? throw new InvalidOperationException($"Escribe {campo}") : s.Trim();

        private static string? Opcional(string? s) => string.IsNullOrWhiteSpace(s) ? null : s.Trim();
    }
}
