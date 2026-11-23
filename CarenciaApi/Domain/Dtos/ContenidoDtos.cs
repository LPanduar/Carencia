using System.ComponentModel.DataAnnotations;

namespace Domain.Dtos
{
    public static class Categorias
    {
        public static readonly string[] Novedad = { "Interfaz", "Mecánicas", "Arte", "Anuncio" };
        public static readonly string[] Captura = { "Escenarios", "Personajes", "Arte" };
        public static readonly string[] Etiqueta = { "Info", "Genero" };
        public static readonly string[] Acerca = { "Mecánicas", "Controles", "Tecnologías" };
    }

    // ---------- Inicio (Admin) ----------
    // PUT api/Inicio/descripcion
    public class InicioDescripcionDto
    {
        [Required, MaxLength(1000)] public string Descripcion { get; set; } = string.Empty;
    }

    // POST api/Inicio/etiqueta  (Tipo: Info o Genero)
    public class InicioEtiquetaCreateDto
    {
        [Required, MaxLength(20)] public string Tipo { get; set; } = string.Empty;
        [Required, MaxLength(40)] public string Texto { get; set; } = string.Empty;
    }

    // POST api/Inicio/acerca  (Seccion: Mecánicas, Controles o Tecnologías)
    public class AcercaItemCreateDto
    {
        [Required, MaxLength(30)] public string Seccion { get; set; } = string.Empty;
        [Required, MaxLength(150)] public string Texto { get; set; } = string.Empty;
    }

    // PUT api/Inicio/etiqueta/{id} y PUT api/Inicio/acerca/{id}
    public class TextoUpdateDto
    {
        [Required, MaxLength(150)] public string Texto { get; set; } = string.Empty;
    }

    // PUT api/Inicio/personaje/{id}
    public class PersonajeUpdateDto
    {
        [Required, MaxLength(60)] public string Nombre { get; set; } = string.Empty;
        [Required, MaxLength(40)] public string Rol { get; set; } = string.Empty;
        [Required, MaxLength(500)] public string Descripcion { get; set; } = string.Empty;
        [MaxLength(200)] public string? Rasgos { get; set; }
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
