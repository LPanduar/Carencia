using System.Security.Claims;

namespace CarenciaAPi.Helpers
{
    public static class Roles
    {
        public const string Admin = "Admin";
        public const string Usuario = "Usuario";
    }

    public static class ClaimsExtensions
    {
        public static int GetUserId(this ClaimsPrincipal user) =>
            int.Parse(user.FindFirstValue(ClaimTypes.NameIdentifier)
                      ?? throw new UnauthorizedAccessException("Sesión inválida"));

        public static bool IsAdmin(this ClaimsPrincipal user) => user.IsInRole(Roles.Admin);
    }
}
