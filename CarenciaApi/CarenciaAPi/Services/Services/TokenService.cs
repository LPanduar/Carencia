using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;
using System.Text;
using CarenciaAPi.Services.Interfaces;
using Domain.Dtos;
using Domain.Entities;
using Microsoft.IdentityModel.Tokens;

namespace CarenciaAPi.Services.Services
{
    // Genera el JWT. El rol va como claim: así [Authorize(Roles = "Admin")] protege las novedades.
    public class TokenService : ITokenService
    {
        private readonly IConfiguration _config;

        public TokenService(IConfiguration config) => _config = config;

        public LoginResponseDto Crear(Usuario usuario)
        {
            var jwt = _config.GetSection("Jwt");
            var key = new SymmetricSecurityKey(Encoding.UTF8.GetBytes(jwt["Key"]!));
            var expira = DateTime.UtcNow.AddHours(jwt.GetValue("ExpiraHoras", 72));

            var claims = new[]
            {
                new Claim(ClaimTypes.NameIdentifier, usuario.PkUsuario.ToString()),
                new Claim(ClaimTypes.Name, usuario.UserName),
                new Claim(ClaimTypes.Role, usuario.RolNombre ?? "Usuario"),
                new Claim("nombre", usuario.Nombre)
            };

            var token = new JwtSecurityToken(
                issuer: jwt["Issuer"],
                audience: jwt["Audience"],
                claims: claims,
                expires: expira,
                signingCredentials: new SigningCredentials(key, SecurityAlgorithms.HmacSha256));

            return new LoginResponseDto
            {
                Token = new JwtSecurityTokenHandler().WriteToken(token),
                Expira = expira,
                Usuario = usuario
            };
        }
    }
}
