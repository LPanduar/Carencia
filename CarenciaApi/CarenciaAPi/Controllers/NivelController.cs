using CarenciaAPi.Helpers;
using CarenciaAPi.Services.Interfaces;
using Domain.Dtos;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace CarenciaAPi.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class NivelController : ControllerBase
    {
        private readonly INivelService _nivelService;

        public NivelController(INivelService nivelService) => _nivelService = nivelService;

        /// <summary>Lista de niveles del demo.</summary>
        [HttpGet]
        public async Task<IActionResult> Obtener() =>
            Ok(await _nivelService.Obtener());

        /// <summary>Detalle del nivel con su ambiente (personajes, mecánicas, controles, tecnologías) y capturas.</summary>
        [HttpGet("{id:int}")]
        public async Task<IActionResult> ById(int id) =>
            Ok(await _nivelService.ById(id));

        /// <summary>Edita título, descripción o duración (sólo Admin).</summary>
        [Authorize(Roles = Roles.Admin)]
        [HttpPut("{id:int}")]
        public async Task<IActionResult> Actualizar(int id, [FromBody] NivelUpdateDto request) =>
            Ok(await _nivelService.Actualizar(id, request));
    }
}
