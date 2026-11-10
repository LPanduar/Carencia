namespace Domain.Entities
{
    public class Nivel
    {
        public int PkNivel { get; set; }
        public int Numero { get; set; }
        public string Titulo { get; set; } = string.Empty;
        public string Descripcion { get; set; } = string.Empty;
        public int DuracionMin { get; set; }
        public string? ImagenTarjeta { get; set; }
        public string? ImagenPortada { get; set; }

        // Se llenan sólo en el detalle (GET api/Nivel/{id})
        public List<NivelDetalle> Detalles { get; set; } = new();
        public List<NivelCaptura> Capturas { get; set; } = new();
    }

    // Filas de "Ambiente": Personajes, Mecánicas, Controles, Tecnologías
    public class NivelDetalle
    {
        public int PkNivelDetalle { get; set; }
        public int FkNivel { get; set; }
        public string Tipo { get; set; } = string.Empty;
        public string Texto { get; set; } = string.Empty;
        public int Orden { get; set; }
    }

    public class NivelCaptura
    {
        public int PkNivelCaptura { get; set; }
        public int FkNivel { get; set; }
        public string ImagenUrl { get; set; } = string.Empty;
        public int Orden { get; set; }
    }
}
