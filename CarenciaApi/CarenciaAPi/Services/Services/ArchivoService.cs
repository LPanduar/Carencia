using CarenciaAPi.Services.Interfaces;

namespace CarenciaAPi.Services.Services
{
    // Guarda imágenes en wwwroot/uploads y devuelve la ruta relativa (/uploads/...)
    public class ArchivoService : IArchivoService
    {
        private static readonly Dictionary<string, string> Permitidos = new()
        {
            ["image/jpeg"] = ".jpg",
            ["image/png"] = ".png",
            ["image/webp"] = ".webp"
        };
        private const long MaxBytes = 8 * 1024 * 1024;

        private readonly IWebHostEnvironment _env;

        public ArchivoService(IWebHostEnvironment env) => _env = env;

        public async Task<string> Guardar(IFormFile archivo, string carpeta)
        {
            if (archivo.Length == 0) throw new InvalidOperationException("La imagen está vacía");
            if (archivo.Length > MaxBytes) throw new InvalidOperationException("La imagen supera 8 MB");
            if (!Permitidos.TryGetValue(archivo.ContentType.ToLowerInvariant(), out var extension))
                throw new InvalidOperationException("Formato no permitido (usa JPG, PNG o WEBP)");

            var raiz = _env.WebRootPath ?? Path.Combine(_env.ContentRootPath, "wwwroot");
            var directorio = Path.Combine(raiz, "uploads", carpeta);
            Directory.CreateDirectory(directorio);

            var nombre = $"{Guid.NewGuid():N}{extension}";
            await using (var stream = File.Create(Path.Combine(directorio, nombre)))
                await archivo.CopyToAsync(stream);

            return $"/uploads/{carpeta}/{nombre}";
        }

        public void Eliminar(string? urlRelativa)
        {
            // Sólo se borran archivos subidos; las imágenes oficiales (/img) se conservan
            if (string.IsNullOrWhiteSpace(urlRelativa) || !urlRelativa.StartsWith("/uploads/")) return;

            var raiz = _env.WebRootPath ?? Path.Combine(_env.ContentRootPath, "wwwroot");
            var ruta = Path.GetFullPath(Path.Combine(raiz, urlRelativa.TrimStart('/')));
            if (ruta.StartsWith(Path.GetFullPath(raiz)) && File.Exists(ruta))
                File.Delete(ruta);
        }
    }
}
