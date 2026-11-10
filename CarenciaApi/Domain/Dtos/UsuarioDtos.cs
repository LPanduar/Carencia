using System.ComponentModel.DataAnnotations;
using Domain.Entities;

namespace Domain.Dtos
{
    public class LoginDto
    {
        [Required] public string UserName { get; set; } = string.Empty;
        [Required] public string Password { get; set; } = string.Empty;
    }

    public class RegistroDto
    {
        [Required, MaxLength(100)] public string Nombre { get; set; } = string.Empty;
        [Required, MaxLength(50)] public string UserName { get; set; } = string.Empty;
        [Required, MinLength(6)] public string Password { get; set; } = string.Empty;
    }

    public class LoginResponseDto
    {
        public string Token { get; set; } = string.Empty;
        public DateTime Expira { get; set; }
        public Usuario Usuario { get; set; } = new();
    }

    // PUT api/Usuario/contrasena: el admin cambia su propia contraseña
    public class CambiarContrasenaDto
    {
        [Required] public string Actual { get; set; } = string.Empty;
        [Required, MinLength(8, ErrorMessage = "La nueva contraseña debe tener al menos 8 caracteres")] public string Nueva { get; set; } = string.Empty;
    }

    // PUT api/Usuario/{id} (sólo Admin): cambia nombre, estatus o rol
    public class UsuarioUpdateDto
    {
        [Required, MaxLength(100)] public string Nombre { get; set; } = string.Empty;
        public bool Estatus { get; set; } = true;
        [Range(1, 2)] public int FkRol { get; set; } = 2;
    }
}
