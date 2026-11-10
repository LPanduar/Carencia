using System.Text.Json.Serialization;

namespace Domain.Entities
{
    public class Rol
    {
        public int PkRol { get; set; }
        public string Nombre { get; set; } = string.Empty;
    }

    public class Usuario
    {
        public int PkUsuario { get; set; }
        public string Nombre { get; set; } = string.Empty;
        public string UserName { get; set; } = string.Empty;

        [JsonIgnore]
        public string Password { get; set; } = string.Empty;

        public bool Estatus { get; set; }
        public int FkRol { get; set; }
        public string? RolNombre { get; set; }
        public DateTime FechaRegistro { get; set; }
    }
}
