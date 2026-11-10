namespace Domain.Entities
{
    // Anuncio / actualización del juego publicada por un administrador
    public class Novedad
    {
        public int PkNovedad { get; set; }
        public string Titulo { get; set; } = string.Empty;
        public string Resumen { get; set; } = string.Empty;
        public string Contenido { get; set; } = string.Empty;
        public string Categoria { get; set; } = string.Empty;
        public string? ImagenUrl { get; set; }
        public bool Destacada { get; set; }
        public DateTime Fecha { get; set; }
        public int? FkAutor { get; set; }
        public string? AutorNombre { get; set; }
    }
}
