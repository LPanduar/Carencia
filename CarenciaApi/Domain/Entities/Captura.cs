namespace Domain.Entities
{
    // Foto de la galería. Las oficiales no tienen usuario; las de la comunidad
    // se toman con la cámara ("El espejo") desde la app.
    public class Captura
    {
        public int PkCaptura { get; set; }
        public string Titulo { get; set; } = string.Empty;
        public string Categoria { get; set; } = string.Empty;
        public string ImagenUrl { get; set; } = string.Empty;
        public int? FkUsuario { get; set; }
        public string? UsuarioNombre { get; set; }
        public DateTime Fecha { get; set; }
    }
}
