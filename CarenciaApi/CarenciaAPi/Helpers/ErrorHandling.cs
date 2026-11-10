using Domain.Helpers;
using Microsoft.AspNetCore.Diagnostics;
using Microsoft.Data.SqlClient;

namespace CarenciaAPi.Helpers
{
    // 403: el usuario está autenticado pero no puede tocar ese recurso
    public class ProhibidoException : Exception
    {
        public ProhibidoException(string message) : base(message) { }
    }

    // Convierte cualquier excepción en un Response<object> con el código HTTP correcto,
    // así los controladores quedan limpios y la app siempre recibe el mismo formato.
    public static class ErrorHandling
    {
        public static void UseManejoDeErrores(this WebApplication app)
        {
            app.UseExceptionHandler(builder => builder.Run(async context =>
            {
                var ex = context.Features.Get<IExceptionHandlerFeature>()?.Error;
                var (status, mensaje) = ex switch
                {
                    KeyNotFoundException e => (StatusCodes.Status404NotFound, e.Message),
                    UnauthorizedAccessException e => (StatusCodes.Status401Unauthorized, e.Message),
                    ProhibidoException e => (StatusCodes.Status403Forbidden, e.Message),
                    InvalidOperationException e => (StatusCodes.Status400BadRequest, e.Message),
                    SqlException { Number: >= 50000 } e => (StatusCodes.Status400BadRequest, e.Message),
                    _ => (StatusCodes.Status500InternalServerError, "Error interno del servidor")
                };

                if (status == StatusCodes.Status500InternalServerError)
                    app.Logger.LogError(ex, "Error no controlado");

                context.Response.StatusCode = status;
                await context.Response.WriteAsJsonAsync(new Response<object>(mensaje));
            }));
        }
    }
}
