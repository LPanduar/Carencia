using CarenciaAPi.Services.Interfaces;
using Microsoft.AspNetCore.Mvc;

namespace CarenciaAPi.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class EstadisticaController : ControllerBase
    {
        private readonly IEstadisticaService _estadisticaService;

        public EstadisticaController(IEstadisticaService estadisticaService) => _estadisticaService = estadisticaService;

        /// <summary>Totales y conteos por categoría de la comunidad.</summary>
        [HttpGet]
        public async Task<IActionResult> Obtener() =>
            Ok(await _estadisticaService.Obtener());
    }
}
