using Domain.Dtos;
using Domain.Entities;
using Domain.Helpers;

namespace CarenciaAPi.Services.Interfaces
{
    public interface IUsuarioService
    {
        Task<Response<Usuario>> CrearAdmin(RegistroDto request);
        Task<Response<LoginResponseDto>> LoginAdmin(LoginDto request);
        Task<Response<bool>> CambiarContrasena(int id, CambiarContrasenaDto request);
        Task<Response<List<Usuario>>> Obtener();
        Task<Response<Usuario>> ById(int id);
        Task<Response<Usuario>> Actualizar(int id, UsuarioUpdateDto request);
        Task<Response<bool>> Eliminar(int id);
    }

    public interface INivelService
    {
        Task<Response<List<Nivel>>> Obtener();
        Task<Response<Nivel>> ById(int id);
        Task<Response<Nivel>> Actualizar(int id, NivelUpdateDto request);
    }

    public interface INovedadService
    {
        Task<Response<List<Novedad>>> Obtener(string? categoria, string? buscar);
        Task<Response<Novedad>> ById(int id);
        Task<Response<Novedad?>> Ultima();
        Task<Response<Novedad>> Crear(string titulo, string resumen, string contenido, string categoria,
                                      bool destacada, IFormFile? imagen, int autorId);
        Task<Response<Novedad>> Actualizar(int id, NovedadUpdateDto request);
        Task<Response<Novedad>> Destacar(int id, bool destacada);
        Task<Response<Novedad>> CambiarImagen(int id, IFormFile imagen);
        Task<Response<bool>> Eliminar(int id);
    }

    public interface ICapturaService
    {
        Task<Response<List<Captura>>> Obtener(string? categoria, string? buscar, int? usuarioId);
        Task<Response<Captura>> ById(int id);
        Task<Response<Captura>> Crear(string titulo, string categoria, IFormFile imagen, int usuarioId);
        Task<Response<Captura>> Actualizar(int id, CapturaUpdateDto request);
        Task<Response<bool>> Eliminar(int id);
    }

    public interface IEstadisticaService
    {
        Task<Response<Estadistica>> Obtener();
    }

    public interface IArchivoService
    {
        Task<string> Guardar(IFormFile archivo, string carpeta);
        void Eliminar(string? urlRelativa);
    }

    public interface ITokenService
    {
        LoginResponseDto Crear(Usuario usuario);
    }
}
