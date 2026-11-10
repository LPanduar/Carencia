using CarenciaAPi.Helpers;
using CarenciaAPi.Requests;
using CarenciaAPi.Services.Interfaces;
using Domain.Dtos;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace CarenciaAPi.Controllers
{
    // Galería oficial del juego: todos la ven (y pueden descargar las fotos);
    // sólo el administrador sube, edita o borra.
    [Route("api/[controller]")]
    [ApiController]
    public class CapturaController : ControllerBase
    {
        private readonly ICapturaService _capturaService;

        public CapturaController(ICapturaService capturaService) => _capturaService = capturaService;

        /// <summary>Galería. Filtros: categoria (Escenarios, Personajes, Arte) y buscar (texto).</summary>
        [HttpGet]
        public async Task<IActionResult> Obtener([FromQuery] string? categoria, [FromQuery] string? buscar) =>
            Ok(await _capturaService.Obtener(categoria, buscar, null));

        [HttpGet("{id:int}")]
        public async Task<IActionResult> ById(int id) =>
            Ok(await _capturaService.ById(id));

        /// <summary>Sube una foto oficial (multipart/form-data). Sólo Admin.</summary>
        [Authorize(Roles = Roles.Admin)]
        [HttpPost]
        [Consumes("multipart/form-data")]
        public async Task<IActionResult> Crear([FromForm] CapturaCreateRequest request) =>
            Ok(await _capturaService.Crear(request.Titulo, request.Categoria, request.Imagen, User.GetUserId()));

        /// <summary>Cambia título o categoría. Sólo Admin.</summary>
        [Authorize(Roles = Roles.Admin)]
        [HttpPut("{id:int}")]
        public async Task<IActionResult> Actualizar(int id, [FromBody] CapturaUpdateDto request) =>
            Ok(await _capturaService.Actualizar(id, request));

        /// <summary>Elimina la foto y su archivo. Sólo Admin.</summary>
        [Authorize(Roles = Roles.Admin)]
        [HttpDelete("{id:int}")]
        public async Task<IActionResult> Eliminar(int id) =>
            Ok(await _capturaService.Eliminar(id));
    }
}
