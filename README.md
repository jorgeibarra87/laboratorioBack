# Sistema de Gestión de Exámenes de Laboratorio - Backend

## Descripción

API REST desarrollada en Spring Boot para la gestión de exámenes de laboratorio tomados. Este sistema permite registrar, consultar y administrar los exámenes realizados a pacientes, proporcionando funcionalidades completas de CRUD y consultas especializadas.

## Características Principales

- **Gestión completa de exámenes tomados**: CRUD completo para exámenes de laboratorio
- **Múltiples tipos de consulta**: Por historia clínica, folio, ingreso, responsable, prioridad, etc.
- **Paginación**: Soporte para consultas paginadas para manejar grandes volúmenes de datos
- **Validación de datos**: Validaciones robustas usando Bean Validation
- **Operaciones en lote**: Creación múltiple de exámenes en una sola operación
- **Logging**: Sistema de logs detallado para auditoría y debugging
- **Transacciones**: Manejo transaccional para garantizar integridad de datos

## Tecnologías Utilizadas

- **Java 17+**
- **Spring Boot 3.x**
- **Spring Data JPA**: Para el manejo de datos y repositorios
- **Spring Web**: Para la creación de REST APIs
- **Jakarta Persistence (JPA)**: Para el mapeo objeto-relacional
- **Jakarta Validation**: Para validación de datos
- **Lombok**: Para reducir código boilerplate
- **SLF4J**: Para logging
- **Base de datos relacional** (PostgreSQL/MySQL/SQL Server)

## Estructura del Proyecto

```
src/main/java/laboratorio/
├── controller/
│   └── ExamenesTomadosController.java    # Controlador REST
├── model/
│   ├── entity/
│   │   └── ExamenesTomados.java          # Entidad JPA
│   └── dto/
│       ├── request/
│       │   └── ExamenTomadoRequest.java  # DTO para requests
│       └── response/
│           └── ExamenTomadoResponse.java # DTO para responses
├── repository/
│   └── ExamenesTomadosRepository.java    # Repositorio JPA
└── service/
    ├── ExamenesTomadosService.java       # Interfaz del servicio
    └── impl/
        └── ExamenesTomadosServiceImpl.java # Implementación del servicio
```

## Modelo de Datos

### Entidad ExamenesTomados

La entidad principal que representa un examen de laboratorio tomado contiene:

#### Datos del Paciente
- `historia`: Historia clínica del paciente (obligatorio)
- `nomPaciente`: Nombre del paciente (obligatorio)
- `edad`: Edad del paciente

#### Datos de la Solicitud
- `numeroIngreso`: Número de ingreso hospitalario (obligatorio)
- `numeroFolio`: Número de folio de la solicitud (obligatorio)
- `cama`: Número de cama
- `nomCama`: Nombre/descripción de la cama
- `areaSolicitante`: Área que solicitó el examen
- `prioridad`: Prioridad del examen (Urgente, Prioritaria, Rutinario)

#### Datos del Examen
- `codServicio`: Código del servicio/examen (obligatorio)
- `nomServicio`: Nombre del servicio/examen (obligatorio)
- `observaciones`: Observaciones adicionales

#### Datos del Procesamiento
- `fechaTomado`: Fecha y hora cuando se tomó la muestra (obligatorio)
- `fechaSolicitud`: Fecha y hora de la solicitud
- `responsable`: Persona responsable del procesamiento
- `estadoResultado`: Estado actual del resultado

## API Endpoints

### Operaciones CRUD Básicas

#### Crear un examen
```http
POST /examenes-tomados
Content-Type: application/json

{
  "historia": "123456",
  "nomPaciente": "Juan Pérez",
  "edad": 45,
  "numeroIngreso": "ING001",
  "numeroFolio": "FOL001",
  "cama": "101",
  "nomCama": "Habitación 101-A",
  "areaSolicitante": "Medicina Interna",
  "prioridad": "Rutinario",
  "codServicio": "LAB001",
  "nomServicio": "Hemograma Completo",
  "observaciones": "Paciente en ayunas",
  "fechaTomado": "2025-10-16T08:30:00",
  "fechaSolicitud": "2025-10-16T07:00:00",
  "responsable": "Dr. Smith",
  "estadoResultado": "COMPLETADO"
}
```

#### Crear múltiples exámenes
```http
POST /examenes-tomados/bulk
Content-Type: application/json

[
  { /* examen 1 */ },
  { /* examen 2 */ },
  { /* examen N */ }
]
```

#### Obtener examen por ID
```http
GET /examenes-tomados/{id}
```

#### Actualizar examen
```http
PUT /examenes-tomados/{id}
Content-Type: application/json

{
  // Datos actualizados del examen
}
```

#### Eliminar examen
```http
DELETE /examenes-tomados/{id}
```

### Consultas Especializadas

#### Listar exámenes con paginación
```http
GET /examenes-tomados?page=0&size=10
```

#### Buscar por historia clínica
```http
GET /examenes-tomados/historia/{historia}
```

#### Buscar por número de folio
```http
GET /examenes-tomados/folio/{numeroFolio}
```

#### Buscar por número de ingreso
```http
GET /examenes-tomados/ingreso/{numeroIngreso}
```

#### Buscar por responsable
```http
GET /examenes-tomados/responsable/{responsable}
```

#### Buscar por prioridad
```http
GET /examenes-tomados/prioridad/{prioridad}
```

#### Buscar por fecha
```http
GET /examenes-tomados/fecha?fecha=2025-10-16T10:00:00
```

### Operaciones de Negocio

#### Marcar examen como completado
```http
PUT /examenes-tomados/{id}/completar?resultado=Normal&responsable=Dr.%20Smith
```

## Configuración de Base de Datos

### Tabla examenes_tomados

```sql
CREATE TABLE examenes_tomados (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    historia VARCHAR(255) NOT NULL,
    nom_paciente VARCHAR(255) NOT NULL,
    edad INTEGER,
    numero_ingreso VARCHAR(255) NOT NULL,
    numero_folio VARCHAR(255) NOT NULL,
    cama VARCHAR(50),
    nom_cama VARCHAR(100),
    area_solicitante VARCHAR(200),
    prioridad VARCHAR(20),
    cod_servicio VARCHAR(20) NOT NULL,
    nom_servicio VARCHAR(200) NOT NULL,
    observaciones VARCHAR(500),
    fecha_tomado DATETIME NOT NULL,
    fecha_solicitud DATETIME,
    responsable VARCHAR(100),
    estado_resultado VARCHAR(50)
);
```

### Índices Recomendados

```sql
CREATE INDEX idx_historia ON examenes_tomados(historia);
CREATE INDEX idx_numero_folio ON examenes_tomados(numero_folio);
CREATE INDEX idx_numero_ingreso ON examenes_tomados(numero_ingreso);
CREATE INDEX idx_fecha_tomado ON examenes_tomados(fecha_tomado);
CREATE INDEX idx_responsable ON examenes_tomados(responsable);
CREATE INDEX idx_prioridad ON examenes_tomados(prioridad);
CREATE INDEX idx_estado_resultado ON examenes_tomados(estado_resultado);
```

## Configuración del Proyecto

### application.properties
```properties
# Configuración de base de datos
spring.datasource.url=jdbc:postgresql://localhost:5432/laboratorio_db
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.datasource.driver-class-name=org.postgresql.Driver

# Configuración de JPA/Hibernate
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# Configuración de logging
logging.level.laboratorio=DEBUG
logging.level.org.springframework.web=INFO
logging.level.org.hibernate.SQL=DEBUG

# Configuración del servidor
server.port=8080
server.servlet.context-path=/api/v1
```

### pom.xml (dependencias principales)
```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

## Instalación y Ejecución

### Prerrequisitos
- Java 17 o superior
- Maven 3.6+
- Base de datos PostgreSQL/MySQL/SQL Server
- IDE (IntelliJ IDEA, Eclipse, VS Code)

### Pasos de instalación

1. **Clonar el repositorio**
```bash
git clone <repository-url>
cd laboratorio-backend
```

2. **Configurar la base de datos**
- Crear la base de datos `laboratorio_db`
- Ejecutar el script de creación de tablas
- Crear los índices recomendados

3. **Configurar application.properties**
- Actualizar las credenciales de base de datos
- Ajustar otros parámetros según el entorno

4. **Compilar y ejecutar**
```bash
mvn clean install
mvn spring-boot:run
```

5. **Verificar la instalación**
```bash
curl http://localhost:8080/api/v1/examenes-tomados?page=0&size=1
```

## Uso de la API

### Ejemplos con curl

#### Crear un examen
```bash
curl -X POST http://localhost:8080/api/v1/examenes-tomados \
  -H "Content-Type: application/json" \
  -d '{
    "historia": "123456",
    "nomPaciente": "Juan Pérez",
    "edad": 45,
    "numeroIngreso": "ING001",
    "numeroFolio": "FOL001",
    "codServicio": "LAB001",
    "nomServicio": "Hemograma Completo",
    "fechaTomado": "2025-10-16T08:30:00",
    "prioridad": "Rutinario",
    "estadoResultado": "COMPLETADO"
  }'
```

#### Buscar exámenes por historia
```bash
curl http://localhost:8080/api/v1/examenes-tomados/historia/123456
```

#### Listar con paginación
```bash
curl "http://localhost:8080/api/v1/examenes-tomados?page=0&size=5"
```

## Validaciones

El sistema implementa las siguientes validaciones:

### Campos Obligatorios
- Historia clínica
- Nombre del paciente
- Número de ingreso
- Número de folio
- Código de servicio
- Nombre del servicio
- Fecha de toma de muestra

### Validaciones de Tamaño
- Cama: máximo 50 caracteres
- Nombre de cama: máximo 100 caracteres
- Área solicitante: máximo 200 caracteres
- Prioridad: máximo 20 caracteres
- Código de servicio: máximo 20 caracteres
- Nombre de servicio: máximo 200 caracteres
- Observaciones: máximo 500 caracteres
- Responsable: máximo 100 caracteres

## Manejo de Errores

La API maneja los siguientes tipos de errores:

### 400 Bad Request
- Datos de entrada inválidos
- Validaciones fallidas
- Formato de fecha incorrecto

### 404 Not Found
- Examen no encontrado por ID
- Recurso solicitado no existe

### 500 Internal Server Error
- Errores de base de datos
- Errores inesperados del sistema

### Formato de respuesta de error
```json
{
  "timestamp": "2025-10-16T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "La historia es obligatoria",
  "path": "/api/v1/examenes-tomados"
}
```

## Logging

El sistema implementa logging en múltiples niveles:

### INFO Level
- Operaciones de creación, actualización y eliminación
- Inicio y fin de operaciones importantes

### DEBUG Level
- Consultas de búsqueda
- Detalles de operaciones de lectura

### ERROR Level
- Excepciones y errores del sistema
- Fallos en operaciones críticas

## Consideraciones de Rendimiento

### Paginación
- Todas las consultas que pueden retornar múltiples resultados soportan paginación
- Tamaño de página por defecto: 10 elementos
- Ordenamiento por fecha de toma descendente

### Índices de Base de Datos
- Índices en campos de búsqueda frecuente (historia, folio, ingreso)
- Índice en fecha_tomado para consultas temporales
- Índices en campos de filtrado (responsable, prioridad, estado)

### Transacciones
- Operaciones de escritura dentro de transacciones
- Operaciones de lectura marcadas como readOnly
- Propagación transaccional apropiada

## Extensibilidad

El sistema está diseñado para ser fácilmente extensible:

### Nuevos Campos
- Agregar campos a la entidad ExamenesTomados
- Actualizar DTOs de request y response
- Crear nuevas consultas en el repositorio si es necesario

### Nuevas Operaciones
- Implementar nuevos métodos en el servicio
- Crear endpoints adicionales en el controlador
- Mantener la consistencia de validaciones y logging

### Integración con Otros Sistemas
- Los DTOs están diseñados para facilitar la integración
- Separación clara entre capas facilita adaptaciones
- Manejo de fechas compatible con diferentes formatos

## Contribución

Para contribuir al proyecto:

1. Fork del repositorio
2. Crear rama feature (`git checkout -b feature/AmazingFeature`)
3. Commit los cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abrir un Pull Request

## Licencia

Este proyecto está bajo la Licencia MIT - ver el archivo [LICENSE](LICENSE) para más detalles.

## Contacto

Para preguntas o soporte técnico, contactar a:
- Email: [tu-email@dominio.com]
- Proyecto: [URL del repositorio]

## Changelog

### v1.0.0 (2025-10-16)
- Implementación inicial del sistema
- CRUD completo para exámenes tomados
- Consultas especializadas por múltiples criterios
- Sistema de validaciones robusto
- Operaciones en lote
- Paginación y ordenamiento
- Logging detallado

---

