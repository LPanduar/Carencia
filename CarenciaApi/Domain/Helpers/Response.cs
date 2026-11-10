namespace Domain.Helpers
{
    // Envoltura estándar de todas las respuestas de la API
    public class Response<T>
    {
        public Response() { }

        public Response(T data, string? message = null)
        {
            Succeded = true;
            Message = message;
            Result = data;
        }

        public Response(string message)
        {
            Succeded = false;
            Message = message;
        }

        public bool Succeded { get; set; }
        public string? Message { get; set; }
        public T? Result { get; set; }
    }
}
