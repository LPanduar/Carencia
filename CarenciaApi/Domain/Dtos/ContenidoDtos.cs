using System.ComponentModel.DataAnnotations;

namespace Domain.Dtos
{
    public static class Categorias
    {
        public static readonly string[] Novedad = { "Interfaz", "Mecánicas", "Arte", "Anuncio" };
        public static readonly string[] Captura = { "Escenarios", "Personajes", "Arte" };
    }

    // PUT api/Novedad/{id}
    public class NovedadUpdateDto
    {
        [Required, MaxLength(150)] public string Titulo { get; set; } = string.Empty;
        [Required, MaxLength(300)] public string Resumen { get; set; } = string.Empty;
        [Required] public string Contenido { get; set; } = string.Empty;
        [Required, MaxLength(30)] public string Categoria { get; set; } = string.Empty;
        public bool Destacada { get; set; }
    }

    // PATCH api/Novedad/{id}/destacar
    public class NovedadDestacarDto
    {
        public bool Destacada { get; set; }
    }

    // PUT api/Captura/{id}
    public class CapturaUpdateDto
    {
        [Required, MaxLength(100)] public string Titulo { get; set; } = string.Empty;
        [Required, MaxLength(30)] public string Categoria { get; set; } = string.Empty;
    }

    // PUT api/Nivel/{id} (Admin)
    public class NivelUpdateDto
    {
        [Required, MaxLength(120)] public string Titulo { get; set; } = string.Empty;
        [Required] public string Descripcion { get; set; } = string.Empty;
        [Range(1, 600)] public int DuracionMin { get; set; }
    }
}
