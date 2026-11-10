namespace Domain.Entities
{
    public class ConteoCategoria
    {
        public string Categoria { get; set; } = string.Empty;
        public int Total { get; set; }
    }

    public class UsuarioTop
    {
        public string Nombre { get; set; } = string.Empty;
        public int Total { get; set; }
    }

    public class Estadistica
    {
        public int TotalUsuarios { get; set; }
        public int TotalCapturas { get; set; }
        public int TotalCapturasComunidad { get; set; }
        public int TotalNovedades { get; set; }
        public int TotalNiveles { get; set; }
        public List<ConteoCategoria> CapturasPorCategoria { get; set; } = new();
        public List<ConteoCategoria> NovedadesPorCategoria { get; set; } = new();
        public List<UsuarioTop> TopFotografos { get; set; } = new();
    }
}
