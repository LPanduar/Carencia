using CarenciaAPi.Helpers;
using CarenciaAPi.Services.Interfaces;
using Domain.Dtos;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace CarenciaAPi.Controllers
{
    // Los jugadores no necesitan cuenta: sólo existe el login del administrador.
    [Route("api/[controller]")]
    [ApiController]
    public class UsuarioController : ControllerBase
    {
        private readonly IUsuarioService _usuarioService;

        public UsuarioController(IUsuarioService usuarioService) => _usuarioService = usuarioService;

        /// <summary>Login del administrador. Copia el "token" y pégalo en el botón Authorize de Swagger. Responde 403 si la cuenta no es Admin.</summary>
        [HttpPost("login-admin")]
        public async Task<IActionResult> LoginAdmin([FromBody] LoginDto request) =>
            Ok(await _usuarioService.LoginAdmin(request));

        /// <summary>Datos del administrador de la sesión actual.</summary>
        [Authorize(Roles = Roles.Admin)]
        [HttpGet("yo")]
        public async Task<IActionResult> Yo() =>
            Ok(await _usuarioService.ById(User.GetUserId()));

        /// <summary>Lista de administradores.</summary>
        [Authorize(Roles = Roles.Admin)]
        [HttpGet]
        public async Task<IActionResult> Obtener() =>
            Ok(await _usuarioService.Obtener());

        [Authorize(Roles = Roles.Admin)]
        [HttpGet("{id:int}")]
        public async Task<IActionResult> ById(int id) =>
            Ok(await _usuarioService.ById(id));

        /// <summary>Crea otra cuenta de administrador para el equipo.</summary>
        [Authorize(Roles = Roles.Admin)]
        [HttpPost]
        public async Task<IActionResult> Crear([FromBody] RegistroDto request) =>
            Ok(await _usuarioService.CrearAdmin(request));

        /// <summary>Cambia nombre o estatus (activo/inactivo) de un administrador.</summary>
        [Authorize(Roles = Roles.Admin)]
        [HttpPut("{id:int}")]
        public async Task<IActionResult> Actualizar(int id, [FromBody] UsuarioUpdateDto request) =>
            Ok(await _usuarioService.Actualizar(id, request));

        [Authorize(Roles = Roles.Admin)]
        [HttpDelete("{id:int}")]
        public async Task<IActionResult> Eliminar(int id) =>
            Ok(await _usuarioService.Eliminar(id));
    }
}
