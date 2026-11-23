namespace Domain.Entities
{
    // Contenido editable de la pantalla de Inicio de la app
    public class Inicio
    {
        public string Descripcion { get; set; } = string.Empty;
        public List<InicioEtiqueta> Etiquetas { get; set; } = new();
        public List<Personaje> Personajes { get; set; } = new();
        public List<AcercaItem> Acerca { get; set; } = new();
    }

    // Tipo "Info" (PC, 20 min, Demo v0.1) o "Genero" (Terror, Point & click...)
    public class InicioEtiqueta
    {
        public int PkInicioEtiqueta { get; set; }
        public string Tipo { get; set; } = string.Empty;
        public string Texto { get; set; } = string.Empty;
        public int Orden { get; set; }
    }

    public class Personaje
    {
        public int PkPersonaje { get; set; }
        public string Nombre { get; set; } = string.Empty;
        public string Rol { get; set; } = string.Empty;
        public string Descripcion { get; set; } = string.Empty;
        public string? Rasgos { get; set; }   // separados por coma
        public string? ImagenUrl { get; set; }
        public int Orden { get; set; }
    }

    // Renglón de "Acerca de": sección Mecánicas, Controles o Tecnologías
    public class AcercaItem
    {
        public int PkAcercaItem { get; set; }
        public string Seccion { get; set; } = string.Empty;
        public string Texto { get; set; } = string.Empty;
        public int Orden { get; set; }
    }
}
