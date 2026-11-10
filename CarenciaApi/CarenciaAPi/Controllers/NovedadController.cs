using CarenciaAPi.Helpers;
using CarenciaAPi.Requests;
using CarenciaAPi.Services.Interfaces;
using Domain.Dtos;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace CarenciaAPi.Controllers
{
    // Lectura pública; publicar/editar/borrar anuncios sólo con rol Admin
    [Route("api/[controller]")]
    [ApiController]
    public class NovedadController : ControllerBase
    {
        private readonly INovedadService _novedadService;

        public NovedadController(INovedadService novedadService) => _novedadService = novedadService;

        /// <summary>Lista de novedades. Filtros opcionales: categoria (Interfaz, Mecánicas, Arte, Anuncio) y buscar (texto).</summary>
        [HttpGet]
        public async Task<IActionResult> Obtener([FromQuery] string? categoria, [FromQuery] string? buscar) =>
            Ok(await _novedadService.Obtener(categoria, buscar));

        /// <summary>Última novedad publicada (la app la consulta para lanzar notificaciones).</summary>
        [HttpGet("ultima")]
        public async Task<IActionResult> Ultima() =>
            Ok(await _novedadService.Ultima());

        [HttpGet("{id:int}")]
        public async Task<IActionResult> ById(int id) =>
            Ok(await _novedadService.ById(id));

        /// <summary>Publica una novedad con imagen opcional (multipart/form-data). Sólo Admin.</summary>
        [Authorize(Roles = Roles.Admin)]
        [HttpPost]
        [Consumes("multipart/form-data")]
        public async Task<IActionResult> Crear([FromForm] NovedadCreateRequest request) =>
            Ok(await _novedadService.Crear(request.Titulo, request.Resumen, request.Contenido,
                request.Categoria, request.Destacada, request.Imagen, User.GetUserId()));

        /// <summary>Edita el texto y la categoría de una novedad. Sólo Admin.</summary>
        [Authorize(Roles = Roles.Admin)]
        [HttpPut("{id:int}")]
        public async Task<IActionResult> Actualizar(int id, [FromBody] NovedadUpdateDto request) =>
            Ok(await _novedadService.Actualizar(id, request));

        /// <summary>Marca o desmarca como destacada (sólo una a la vez). Sólo Admin.</summary>
        [Authorize(Roles = Roles.Admin)]
        [HttpPatch("{id:int}/destacar")]
        public async Task<IActionResult> Destacar(int id, [FromBody] NovedadDestacarDto request) =>
            Ok(await _novedadService.Destacar(id, request.Destacada));

        /// <summary>Reemplaza la imagen de la novedad. Sólo Admin.</summary>
        [Authorize(Roles = Roles.Admin)]
        [HttpPatch("{id:int}/imagen")]
        [Consumes("multipart/form-data")]
        public async Task<IActionResult> CambiarImagen(int id, [FromForm] ImagenRequest request) =>
            Ok(await _novedadService.CambiarImagen(id, request.Imagen));

        [Authorize(Roles = Roles.Admin)]
        [HttpDelete("{id:int}")]
        public async Task<IActionResult> Eliminar(int id) =>
            Ok(await _novedadService.Eliminar(id));
    }
}
