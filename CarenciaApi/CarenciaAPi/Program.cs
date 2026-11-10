using System.Reflection;
using System.Text;
using CarenciaAPi.Helpers;
using CarenciaAPi.Services.Interfaces;
using CarenciaAPi.Services.Services;
using Domain.Entities;
using Domain.Helpers;
using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.AspNetCore.Identity;
using Microsoft.AspNetCore.Mvc;
using Microsoft.IdentityModel.Tokens;
using Microsoft.OpenApi;

var builder = WebApplication.CreateBuilder(args);

builder.Services.AddControllers()
    .ConfigureApiBehaviorOptions(options =>
    {
        // Errores de validación con el mismo formato Response<T> que el resto de la API
        options.InvalidModelStateResponseFactory = context =>
        {
            var errores = context.ModelState
                .Where(e => e.Value?.Errors.Count > 0)
                .Select(e => $"{e.Key}: {e.Value!.Errors[0].ErrorMessage}");
            return new BadRequestObjectResult(new Response<object>(string.Join(" | ", errores)));
        };
    });

// ---------- Swagger con botón "Authorize" para el JWT ----------
builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen(c =>
{
    c.SwaggerDoc("v1", new OpenApiInfo
    {
        Title = "Carencia API",
        Version = "v1",
        Description = "API de la app móvil de Carencia. Para probar endpoints de Admin: " +
                      "POST /api/Usuario/login-admin con la cuenta de administrador, copia el token y pulsa Authorize."
    });

    c.AddSecurityDefinition("Bearer", new OpenApiSecurityScheme
    {
        Type = SecuritySchemeType.Http,
        Scheme = "bearer",
        BearerFormat = "JWT",
        Description = "Pega sólo el token (sin la palabra Bearer)."
    });
    c.AddSecurityRequirement(document => new OpenApiSecurityRequirement
    {
        [new OpenApiSecuritySchemeReference("Bearer", document)] = new List<string>()
    });

    var xml = Path.Combine(AppContext.BaseDirectory, $"{Assembly.GetExecutingAssembly().GetName().Name}.xml");
    if (File.Exists(xml)) c.IncludeXmlComments(xml);
});

// ---------- Autenticación JWT + roles ----------
var jwt = builder.Configuration.GetSection("Jwt");
// La clave NO está en appsettings.json (se sube a Git). Se lee de User Secrets en desarrollo
// o de la variable de entorno Jwt__Key en producción.
var claveJwt = jwt["Key"];
if (string.IsNullOrWhiteSpace(claveJwt) || claveJwt.Length < 32)
    throw new InvalidOperationException(
        "Falta la clave JWT (mínimo 32 caracteres). En la carpeta del proyecto ejecuta: " +
        "dotnet user-secrets set \"Jwt:Key\" \"<una clave larga y aleatoria>\"");

builder.Services
    .AddAuthentication(JwtBearerDefaults.AuthenticationScheme)
    .AddJwtBearer(options =>
    {
        options.TokenValidationParameters = new TokenValidationParameters
        {
            ValidateIssuer = true,
            ValidateAudience = true,
            ValidateLifetime = true,
            ValidateIssuerSigningKey = true,
            ValidIssuer = jwt["Issuer"],
            ValidAudience = jwt["Audience"],
            IssuerSigningKey = new SymmetricSecurityKey(Encoding.UTF8.GetBytes(claveJwt))
        };
        // 401/403 también con formato Response<T>
        options.Events = new JwtBearerEvents
        {
            OnChallenge = async ctx =>
            {
                ctx.HandleResponse();
                ctx.Response.StatusCode = StatusCodes.Status401Unauthorized;
                await ctx.Response.WriteAsJsonAsync(new Response<object>("Inicia sesión para continuar"));
            },
            OnForbidden = async ctx =>
            {
                ctx.Response.StatusCode = StatusCodes.Status403Forbidden;
                await ctx.Response.WriteAsJsonAsync(new Response<object>("Necesitas permisos de administrador"));
            }
        };
    });
builder.Services.AddAuthorization();

// ---------- Servicios (Dapper) ----------
builder.Services.AddSingleton<IPasswordHasher<Usuario>, PasswordHasher<Usuario>>();
builder.Services.AddSingleton<ITokenService, TokenService>();
builder.Services.AddSingleton<IArchivoService, ArchivoService>();
builder.Services.AddTransient<IUsuarioService, UsuarioService>();
builder.Services.AddTransient<INivelService, NivelService>();
builder.Services.AddTransient<INovedadService, NovedadService>();
builder.Services.AddTransient<ICapturaService, CapturaService>();
builder.Services.AddTransient<IEstadisticaService, EstadisticaService>();

var app = builder.Build();

app.UseManejoDeErrores();

if (app.Environment.IsDevelopment())
{
    app.UseSwagger();    // /swagger/v1/swagger.json
    app.UseSwaggerUI();  // interfaz en /swagger
}

// Sin redirección a HTTPS: el emulador de Android consume la API por http://10.0.2.2:5101
app.UseStaticFiles();    // wwwroot/img (oficiales) y wwwroot/uploads (subidas)
app.UseAuthentication();
app.UseAuthorization();
app.MapControllers();

app.Run();
