using System.Data;
using CarenciaAPi.Services.Interfaces;
using Dapper;
using Domain.Dtos;
using Domain.Entities;
using Domain.Helpers;
using Microsoft.Data.SqlClient;

namespace CarenciaAPi.Services.Services
{
    public class NivelService : INivelService
    {
        private readonly string _connectionString;

        public NivelService(IConfiguration configuration) =>
            _connectionString = configuration.GetConnectionString("DefaultConnection")!;

        public async Task<Response<List<Nivel>>> Obtener()
        {
            using var db = new SqlConnection(_connectionString);
            var niveles = await db.QueryAsync<Nivel>("spNivelObtener", commandType: CommandType.StoredProcedure);
            return new Response<List<Nivel>>(niveles.ToList());
        }

        // Un solo procedimiento devuelve 3 conjuntos de resultados (QueryMultiple)
        public async Task<Response<Nivel>> ById(int id)
        {
            using var db = new SqlConnection(_connectionString);
            using var multi = await db.QueryMultipleAsync(
                "spNivelById", new { PkNivel = id }, commandType: CommandType.StoredProcedure);

            var nivel = await multi.ReadFirstOrDefaultAsync<Nivel>()
                        ?? throw new KeyNotFoundException("No se encontró el nivel");
            nivel.Detalles = (await multi.ReadAsync<NivelDetalle>()).ToList();
            nivel.Capturas = (await multi.ReadAsync<NivelCaptura>()).ToList();
            return new Response<Nivel>(nivel);
        }

        public async Task<Response<Nivel>> Actualizar(int id, NivelUpdateDto request)
        {
            using var db = new SqlConnection(_connectionString);
            var nivel = await db.QueryFirstOrDefaultAsync<Nivel>(
                "spNivelActualizar",
                new { PkNivel = id, request.Titulo, request.Descripcion, request.DuracionMin },
                commandType: CommandType.StoredProcedure)
                ?? throw new KeyNotFoundException("No se encontró el nivel");
            return new Response<Nivel>(nivel, "Nivel actualizado");
        }
    }
}
