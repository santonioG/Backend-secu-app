# AGENT.md

> Guía de referencia para agentes de IA y desarrolladores que trabajen en este repositorio.

---

## 1. Stack Técnico

| Capa              | Tecnología                   | Versión mínima |
| ----------------- | ---------------------------- | -------------- |
| Lenguaje          | Java                         | 17             |
| Framework         | Spring Boot                  | 3.x            |
| Seguridad         | Spring Security + JWT (jjwt) | —              |
| Persistencia      | Spring Data JPA / Hibernate  | —              |
| Base de datos     | MySQL 8                      | 8.0            |
| Generación de PDF | iText (itextpdf)             | 7.x            |
| Build             | Maven                        | 3.8+           |
| Contenedores      | Docker / Docker Compose      | —              |

---

## 2. Estructura del Proyecto

```
com.duoc.backend/
├── BackendApplication.java          # Entry point (@SpringBootApplication)
├── Constants.java                   # Constantes JWT y seguridad
├── JWTAuthenticationConfig.java     # Generación de tokens JWT
├── JWTAuthorizationFilter.java      # Filtro de validación JWT (OncePerRequestFilter)
├── LoginController.java             # POST /login
├── SecuredController.java           # Endpoints de prueba protegidos
├── WebSecurityConfig.java           # Configuración de Spring Security
│
├── appointment/
│   ├── Appointment.java             # Entidad JPA
│   ├── AppointmentController.java   # REST controller
│   ├── AppointmentRepository.java   # CrudRepository
│   └── AppointmentService.java      # Lógica de negocio
│
├── care/
│   ├── Care.java
│   ├── CareController.java
│   ├── CareRepository.java
│   └── CareService.java
│
├── invoice/
│   ├── Invoice.java                 # Entidad con relaciones @ManyToMany
│   ├── InvoiceController.java       # Incluye endpoint GET /invoice/pdf/{id}
│   ├── InvoiceRepository.java
│   └── InvoiceService.java          # Calcula totalCost, valida FK
│
├── medication/
│   ├── Medication.java
│   ├── MedicationController.java
│   ├── MedicationRepository.java
│   └── MedicationService.java
│
├── patient/
│   ├── Patient.java
│   ├── PatientController.java
│   ├── PatientRepository.java
│   └── PatientService.java
│
└── user/
    ├── User.java                    # Implementa UserDetails
    ├── UserRepository.java          # Incluye findByUsername()
    └── MyUserDetailsService.java    # @Service + @Configuration, expone PasswordEncoder
```

```
src/main/resources/
└── application.properties           # Configuración de BD, JPA, logging y puerto
```

---

## 3. Convenciones de Código

### Nomenclatura

- **Clases**: `PascalCase` — `AppointmentService`, `InvoiceController`
- **Métodos y variables**: `camelCase` — `getAllAppointments()`, `totalCost`
- **Constantes**: `UPPER_SNAKE_CASE` — `SUPER_SECRET_KEY`, `LOGIN_URL`
- **Paquetes**: `com.duoc.backend.<dominio>` en minúsculas

### Organización por dominio

Cada dominio de negocio (appointment, care, invoice, medication, patient, user) tiene su **propio paquete** con las cuatro capas: `Entity`, `Repository`, `Service`, `Controller`. No se mezclan clases de distintos dominios en un mismo archivo.

### Entidades JPA

```java
@Entity
public class NombreEntidad {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    // campos ...
    // solo getters y setters, sin lógica de negocio
}
```

### Repositorios

Extender únicamente `CrudRepository<Entidad, TipoId>`. No agregar queries personalizadas a menos que sean estrictamente necesarias; declararlas con convención de nombres de Spring Data antes de usar `@Query`.

### Servicios

- Anotados con `@Service`.
- Inyección de dependencias exclusivamente por `@Autowired` (consistente con el resto del proyecto).
- Toda validación de integridad referencial se realiza en el Service, **no** en el Controller.
- El cálculo de campos derivados (ej. `totalCost` en `InvoiceService`) es responsabilidad exclusiva del Service.

### Controllers

- Anotados con `@RestController` + `@RequestMapping("/<recurso-en-plural>")`.
- Solo delegan al Service; no contienen lógica de negocio.
- Usar los verbos HTTP estándar: `GET`, `POST`, `DELETE`, `PUT`/`PATCH`.

---

## 4. Patrones Arquitectónicos

### Patrón Controller → Service → Repository

```
Request → Controller → Service → Repository → DB
```

El Controller nunca accede directamente al Repository (excepción ya existente en `CareController`: debe corregirse en el futuro usando `CareService`).

### Seguridad: JWT Stateless

1. `POST /login` devuelve un token `Bearer <jwt>`.
2. Todas las demás rutas requieren el header `Authorization: Bearer <token>`.
3. `JWTAuthorizationFilter` valida el token en cada request antes de llegar al Controller.
4. Las credenciales y clave secreta se leen desde `Constants.java`.

### Generación de PDF

El endpoint `GET /invoice/pdf/{id}` genera el PDF en memoria con iText y lo retorna como `ResponseEntity<byte[]>` con `Content-Type: application/pdf`. No se escribe al disco.

### Relaciones entre entidades

Las relaciones `@ManyToMany` (Invoice ↔ Care, Invoice ↔ Medication) se mapean con `@JoinTable` explícito para control total sobre los nombres de tablas intermedias.

---

## 5. Prohibiciones

### ❌ No hacer

- **No colocar lógica de negocio en los Controllers.** Toda validación, cálculo y orquestación va en el Service.
- **No acceder al Repository directamente desde el Controller** (corregir el patrón actual de `CareController`).
- **No hardcodear credenciales de BD** en el código fuente. Usar variables de entorno o un vault en producción.
- **No exponer la `SUPER_SECRET_KEY` en logs ni en respuestas de la API.**
- **No lanzar excepciones genéricas (`RuntimeException`)** sin un mensaje claro; usar excepciones personalizadas o `ResponseStatusException`.
- **No implementar los métodos de `UserDetails` con `UnsupportedOperationException`** en código de producción. Deben retornar valores reales (`true`/`false` según corresponda).
- **No comparar contraseñas en texto plano** (`password.equals(...)`). Usar `PasswordEncoder.matches()`.
- **No omitir manejo de errores** en la generación de PDF; siempre devolver un status HTTP significativo.
- **No crear clases God Object** que mezclen responsabilidades de múltiples dominios.
- **No usar `@SuppressWarnings("null")` como solución definitiva**; resolver el warning en su origen.

---

## 6. Flujo de Trabajo

### Ramas

```
main          ← producción estable
develop       ← integración continua
feature/<nombre>    ← nuevas funcionalidades
fix/<nombre>        ← correcciones de bugs
hotfix/<nombre>     ← parches urgentes en producción
```

### Ciclo de una feature

```
1. Crear rama desde develop:
   git checkout -b feature/invoice-update-endpoint

2. Desarrollar con commits atómicos (ver sección de commits)

3. Asegurarse de que la build pasa:
   mvn clean verify

4. Abrir Pull Request hacia develop
   - Título descriptivo
   - Descripción con contexto y decisiones tomadas
   - Asignar al menos 1 revisor

5. Merge con squash o merge commit según política del equipo

6. Eliminar la rama remota tras el merge
```

### Antes de cada PR

- [ ] La aplicación levanta sin errores (`mvn spring-boot:run`)
- [ ] Los endpoints nuevos o modificados fueron probados en Postman / curl
- [ ] No hay credenciales hardcodeadas en el diff
- [ ] Se actualizó `application.properties` si se añadió configuración nueva (con valores de ejemplo, no reales)

---

## 7. Estilo de Commits

Seguir la convención **Conventional Commits**:

```
<tipo>(<scope>): <descripción en imperativo, minúsculas>
```

### Tipos permitidos

| Tipo       | Cuándo usarlo                                |
| ---------- | -------------------------------------------- |
| `feat`     | Nueva funcionalidad                          |
| `fix`      | Corrección de bug                            |
| `refactor` | Cambio de código sin alterar comportamiento  |
| `docs`     | Solo documentación                           |
| `test`     | Agregar o modificar tests                    |
| `chore`    | Tareas de build, dependencias, configuración |
| `perf`     | Mejora de rendimiento                        |
| `security` | Parche de seguridad                          |

### Scopes sugeridos

`appointment`, `care`, `invoice`, `medication`, `patient`, `user`, `auth`, `config`, `pdf`

### Ejemplos

```
feat(invoice): agregar endpoint para descarga de PDF
fix(auth): corregir comparación de contraseñas usando BCrypt
refactor(care): mover lógica del controller al service
security(jwt): rotar clave secreta y moverla a variable de entorno
docs(agent): agregar AGENT.md con convenciones del proyecto
chore(deps): actualizar versión de itext a 7.2.5
```

### Estilo de PRs

- **Título**: igual al commit principal (Conventional Commits)
- **Descripción**: incluir _qué_ se hizo, _por qué_ y cualquier _decisión de diseño_ relevante
- **Tamaño recomendado**: < 400 líneas modificadas por PR
- **Un PR = un propósito**: no mezclar features con refactors ni con fixes

---

## 8. Variables de Entorno (Referencia)

| Variable      | Descripción                 | Ejemplo                              |
| ------------- | --------------------------- | ------------------------------------ |
| `DB_URL`      | URL de conexión JDBC        | `jdbc:mysql://mysql:3306/backend_db` |
| `DB_USERNAME` | Usuario de BD               | `back_user`                          |
| `DB_PASSWORD` | Contraseña de BD            | —                                    |
| `JWT_SECRET`  | Clave de firma JWT (Base64) | —                                    |
| `SERVER_PORT` | Puerto del servidor         | `8080`                               |

> En desarrollo local se pueden definir en `application.properties` (no commitear valores reales). En producción usar variables de entorno del contenedor o un secrets manager.
