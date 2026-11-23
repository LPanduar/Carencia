using System.ComponentModel.DataAnnotations;

namespace CarenciaAPi.Requests
{
    // multipart/form-data: POST api/Novedad (sólo Admin)
    public class NovedadCreateRequest
    {
        [Required, MaxLength(150)] public string Titulo { get; set; } = string.Empty;
        [Required, MaxLength(300)] public string Resumen { get; set; } = string.Empty;
        [Required] public string Contenido { get; set; } = string.Empty;
        [Required, MaxLength(30)] public string Categoria { get; set; } = string.Empty;
        public bool Destacada { get; set; }
        public IFormFile? Imagen { get; set; }
    }

    // multipart/form-data: POST api/Captura (sólo Admin: foto oficial de la galería)
    public class CapturaCreateRequest
    {
        [Required, MaxLength(100)] public string Titulo { get; set; } = string.Empty;
        [Required, MaxLength(30)] public string Categoria { get; set; } = "Escenarios";
        [Required] public IFormFile Imagen { get; set; } = null!;
    }

    // multipart/form-data: POST api/Inicio/personaje (sólo Admin)
    public class PersonajeCreateRequest
    {
        [Required, MaxLength(60)] public string Nombre { get; set; } = string.Empty;
        [Required, MaxLength(40)] public string Rol { get; set; } = string.Empty;
        [Required, MaxLength(500)] public string Descripcion { get; set; } = string.Empty;
        [MaxLength(200)] public string? Rasgos { get; set; }
        public IFormFile? Imagen { get; set; }
    }

    // multipart/form-data: PATCH api/Novedad/{id}/imagen
    public class ImagenRequest
    {
        [Required] public IFormFile Imagen { get; set; } = null!;
    }
}
