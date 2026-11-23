using CarenciaAPi.Helpers;
using CarenciaAPi.Requests;
using CarenciaAPi.Services.Interfaces;
using Domain.Dtos;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace CarenciaAPi.Controllers
{
    // Pantalla de Inicio: lectura pública; crear/editar/borrar sólo con rol Admin
    [Route("api/[controller]")]
    [ApiController]
    public class InicioController : ControllerBase
    {
        private readonly IInicioService _inicioService;

        public InicioController(IInicioService inicioService) => _inicioService = inicioService;

        /// <summary>Todo el contenido de Inicio: descripción, etiquetas, personajes y "Acerca de".</summary>
        [HttpGet]
        public async Task<IActionResult> Obtener() =>
            Ok(await _inicioService.Obtener());

        /// <summary>Cambia la descripción del juego. Sólo Admin.</summary>
        [Authorize(Roles = Roles.Admin)]
        [HttpPut("descripcion")]
        public async Task<IActionResult> Descripcion([FromBody] InicioDescripcionDto request) =>
            Ok(await _inicioService.ActualizarDescripcion(request.Descripcion));

        // ---------- Etiquetas (Tipo: Info o Genero) ----------
        [Authorize(Roles = Roles.Admin)]
        [HttpPost("etiqueta")]
        public async Task<IActionResult> CrearEtiqueta([FromBody] InicioEtiquetaCreateDto request) =>
            Ok(await _inicioService.CrearEtiqueta(request));

        [Authorize(Roles = Roles.Admin)]
        [HttpPut("etiqueta/{id:int}")]
        public async Task<IActionResult> ActualizarEtiqueta(int id, [FromBody] TextoUpdateDto request) =>
            Ok(await _inicioService.ActualizarEtiqueta(id, request.Texto));

        [Authorize(Roles = Roles.Admin)]
        [HttpDelete("etiqueta/{id:int}")]
        public async Task<IActionResult> EliminarEtiqueta(int id) =>
            Ok(await _inicioService.EliminarEtiqueta(id));

        // ---------- Personajes ----------
        /// <summary>Agrega un personaje con imagen opcional (multipart/form-data). Sólo Admin.</summary>
        [Authorize(Roles = Roles.Admin)]
        [HttpPost("personaje")]
        [Consumes("multipart/form-data")]
        public async Task<IActionResult> CrearPersonaje([FromForm] PersonajeCreateRequest request) =>
            Ok(await _inicioService.CrearPersonaje(request.Nombre, request.Rol, request.Descripcion, request.Rasgos, request.Imagen));

        [Authorize(Roles = Roles.Admin)]
        [HttpPut("personaje/{id:int}")]
        public async Task<IActionResult> ActualizarPersonaje(int id, [FromBody] PersonajeUpdateDto request) =>
            Ok(await _inicioService.ActualizarPersonaje(id, request));

        [Authorize(Roles = Roles.Admin)]
        [HttpPatch("personaje/{id:int}/imagen")]
        [Consumes("multipart/form-data")]
        public async Task<IActionResult> ImagenPersonaje(int id, [FromForm] ImagenRequest request) =>
            Ok(await _inicioService.CambiarImagenPersonaje(id, request.Imagen));

        [Authorize(Roles = Roles.Admin)]
        [HttpDelete("personaje/{id:int}")]
        public async Task<IActionResult> EliminarPersonaje(int id) =>
            Ok(await _inicioService.EliminarPersonaje(id));

        // ---------- Acerca de (Seccion: Mecánicas, Controles o Tecnologías) ----------
        [Authorize(Roles = Roles.Admin)]
        [HttpPost("acerca")]
        public async Task<IActionResult> CrearAcerca([FromBody] AcercaItemCreateDto request) =>
            Ok(await _inicioService.CrearAcerca(request));

        [Authorize(Roles = Roles.Admin)]
        [HttpPut("acerca/{id:int}")]
        public async Task<IActionResult> ActualizarAcerca(int id, [FromBody] TextoUpdateDto request) =>
            Ok(await _inicioService.ActualizarAcerca(id, request.Texto));

        [Authorize(Roles = Roles.Admin)]
        [HttpDelete("acerca/{id:int}")]
        public async Task<IActionResult> EliminarAcerca(int id) =>
            Ok(await _inicioService.EliminarAcerca(id));
    }
}
