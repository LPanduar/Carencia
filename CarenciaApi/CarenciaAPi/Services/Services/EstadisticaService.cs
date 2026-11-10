using System.Data;
using CarenciaAPi.Services.Interfaces;
using Dapper;
using Domain.Entities;
using Domain.Helpers;
using Microsoft.Data.SqlClient;

namespace CarenciaAPi.Services.Services
{
    public class EstadisticaService : IEstadisticaService
    {
        private readonly string _connectionString;

        public EstadisticaService(IConfiguration configuration) =>
            _connectionString = configuration.GetConnectionString("DefaultConnection")!;

        public async Task<Response<Estadistica>> Obtener()
        {
            using var db = new SqlConnection(_connectionString);
            using var multi = await db.QueryMultipleAsync("spEstadisticas", commandType: CommandType.StoredProcedure);

            var stats = await multi.ReadSingleAsync<Estadistica>();
            stats.CapturasPorCategoria = (await multi.ReadAsync<ConteoCategoria>()).ToList();
            stats.NovedadesPorCategoria = (await multi.ReadAsync<ConteoCategoria>()).ToList();
            stats.TopFotografos = (await multi.ReadAsync<UsuarioTop>()).ToList();
            return new Response<Estadistica>(stats);
        }
    }
}
