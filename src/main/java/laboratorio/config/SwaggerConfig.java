package laboratorio.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication", // Nombre de tu esquema de seguridad (puedes llamarlo como quieras)
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer") // Especifica el esquema como "bearer"
                                        .bearerFormat("JWT") // Indica que el formato del token es JWT
                                        .in(SecurityScheme.In.HEADER) // Opcional, el token se envía en el encabezado
                                        .name("Authorization") // Opcional, el nombre del encabezado
                                        .description("Introduce el token JWT (Bearer Token)"))); // Descripción para el usuario
    }

}
