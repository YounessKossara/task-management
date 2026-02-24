# 📓 Journal de Bord — Task Manager

## Projet
- **Nom** : Task Manager (Gestion de projets et tâches)
- **Stack** : Java 17, Spring Boot 4.0.0, PostgreSQL, Keycloak 26, RustFS (MinIO), Angular 21
- **Date de début** : 23 février 2026
- **Durée** : 1 semaine

---

# Jour 1 — Infrastructure & Setup

## 1. pom.xml

Fichier Maven avec toutes les dépendances :

| Dépendance | Rôle |
|-----------|------|
| spring-boot-starter-web | API REST |
| spring-boot-starter-data-jpa | ORM Hibernate/JPA |
| spring-boot-starter-validation | Validation des champs |
| spring-boot-starter-actuator | Health checks Kubernetes |
| spring-boot-starter-oauth2-resource-server | Validation JWT Keycloak |
| spring-boot-starter-oauth2-client | Communication Keycloak Admin |
| keycloak-admin-client 26.0.0 | CRUD utilisateurs Keycloak |
| postgresql | Driver BDD |
| minio 8.5.14 | SDK S3 pour RustFS |
| springdoc-openapi 2.8.0 | Swagger UI |
| lombok | Réduire le boilerplate |
| mapstruct 1.6.3 | Mapping DTO ↔ Entity |
| spring-boot-starter-test | JUnit 5, Mockito, AssertJ |
| testcontainers | Tests d'intégration |
| jacoco-maven-plugin | Couverture de code ≥80% |
| sonar-maven-plugin | Analyse SonarQube |

---

## 2. Docker Compose

Fichier `docker-compose.yml` avec 3 services :

```yaml
services:
  postgres:        # Port 5432 — BDD PostgreSQL 16
  keycloak:        # Port 8180 — IAM Keycloak 26
  rustfs:          # Port 9000 (API) + 9001 (Console) — Stockage MinIO
```

- **Commande** : `docker compose up -d`
- Les données sont persistées via des volumes Docker

---

## 3. Configuration Keycloak

### Realm
- **Nom** : `task-manager`

### Rôles
- `ADMIN` — accès total
- `USER` — accès limité

### Client Backend (API)
- **Client ID** : `task-manager-api`
- **Type** : Confidential (Client authentication ON)
- **Flow** : Standard flow + Service accounts roles
- **Redirect URI** : `http://localhost:8080/*`
- **Service Account Roles** : `manage-users`, `view-users`, `manage-realm`

### Client Frontend
- **Client ID** : `task-manager-frontend`
- **Type** : Public (Client authentication OFF)
- **Flow** : Standard flow + Direct access grants
- **Redirect URI** : `http://localhost:4200/*`

### Utilisateurs de test
| Username | Rôle | Password |
|----------|------|----------|
| admin1 | ADMIN | admin123 |
| user1 | USER | user123 |

---

## 4. Configuration RustFS (MinIO)

- **URL Console** : http://localhost:9001
- **Credentials** : minioadmin / minioadmin
- **Bucket créé** : `identity-documents`

---

## 5. application.properties

```properties
server.port=8080

# PostgreSQL
spring.datasource.url=jdbc:postgresql://localhost:5432/taskmanager
spring.datasource.username=taskmanager
spring.datasource.password=taskmanager
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# Keycloak JWT
spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:8180/realms/task-manager

# MinIO / RustFS
minio.url=http://localhost:9000
minio.access-key=minioadmin
minio.secret-key=minioadmin
minio.bucket-name=identity-documents

# Keycloak Admin
keycloak.auth-server-url=http://localhost:8180
keycloak.realm=task-manager
keycloak.client-id=task-manager-api
keycloak.client-secret=<SECRET>
```

**Choix** : `ddl-auto=update` au lieu de Flyway → Hibernate crée les tables automatiquement.

---

## 6. SecurityConfig.java

**Package** : `org.example.task_project.config`

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        // CORS pour Angular (localhost:4200)
        // CSRF désactivé (API REST avec JWT)
        // Session STATELESS (pas de session serveur)
        // Swagger + Actuator → accès public (permitAll)
        // Tout le reste → JWT obligatoire (authenticated)
        // OAuth2 Resource Server → validation JWT Keycloak
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        // Autorise localhost:4200 (Angular)
        // Méthodes : GET, POST, PUT, PATCH, DELETE, OPTIONS
    }
}
```

---

## 7. MinioConfig.java

**Package** : `org.example.task_project.config`

```java
@Configuration
public class MinioConfig {
    // Lit minio.url, minio.access-key, minio.secret-key depuis properties
    // Crée un @Bean MinioClient pour l'injection dans les services
}
```

---

## 8. KeycloakAdminConfig.java

**Package** : `org.example.task_project.config`

```java
@Configuration
public class KeycloakAdminConfig {
    // Lit keycloak.auth-server-url, realm, client-id, client-secret
    // Crée un @Bean Keycloak (Admin Client) avec grant_type=client_credentials
    // Permet au backend de gérer les utilisateurs sans connexion humaine
}
```

---

## 9. Premier lancement ✅

- **Commande** : `./mvnw spring-boot:run`
- **Résultat** : `Started TaskProjectApplication in 5.9 seconds`
- **Swagger UI** : http://localhost:8080/swagger-ui/index.html ✅
- **Actuator** : http://localhost:8080/actuator ✅
- **PostgreSQL** : connexion OK (HikariPool)
- **Sécurité** : 401 sur les endpoints non-publics ✅

---

## Structure du projet actuelle

```
task_project/
├── pom.xml
├── docker-compose.yml
├── src/main/java/org/example/task_project/
│   ├── TaskProjectApplication.java
│   └── config/
│       ├── SecurityConfig.java
│       ├── MinioConfig.java
│       └── KeycloakAdminConfig.java
├── src/main/resources/
│   └── application.properties
└── src/test/java/org/example/task_project/
    └── TaskProjectApplicationTests.java
```

---

# Prochaines étapes

- [ ] GlobalExceptionHandler.java + ResourceNotFoundException.java
- [ ] Entités JPA : UserRef, Project, Task (+ enums)
- [ ] Services : KeycloakUserService, ProjectService, TaskService
- [ ] Controllers REST : UserController, ProjectController, TaskController
- [ ] FileStorageService (upload PJ vers RustFS)
- [ ] Tests unitaires + intégration (≥80%)
- [ ] Frontend Angular 21
- [ ] Docker + Kubernetes + CI/CD
- [ ] SonarQube (0 bug)
