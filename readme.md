# 🔐 Spring Security – Sécurisation Initiale de l’API Logistique (Basic Auth)

Ce document constitue la documentation officielle de la phase de sécurisation du projet logistique.

Il définit les fondations de **Spring Security moderne (Spring Boot 3 / Spring Security 6)** et inclut :

* Authentification & Autorisation
* Architecture interne de Spring Security
* Implémentation complète de Basic Authentication
* Sécurisation des endpoints
* Tests avec Postman et cURL
* Bonnes pratiques de sécurité

---

## 📘 Sommaire

1. Introduction
2. Authentification vs Autorisation
3. Architecture Spring Security Moderne
4. Security Filter Chain
5. Basic Authentication – Théorie & Sécurité
6. Configuration Spring Security (POC)
7. Rôles et accès des endpoints
8. Tests Postman / cURL
9. Schémas internes
10. Bonnes pratiques

---

## 🔰 Introduction

Cette phase vise à mettre en place une première couche de sécurité pour l’API logistique en utilisant **Basic Authentication**.

Technologies volontairement exclues :

* ❌ JWT
* ❌ OAuth2
* ❌ Sessions avancées
* ❌ Docker
* ❌ CI/CD

### Objectifs

* Comprendre Spring Security en profondeur
* Construire un POC Basic Auth solide
* Documenter clairement les mécanismes internes

---

## 🧩 Authentification vs Autorisation

| Concept          | Définition                            |
| ---------------- | ------------------------------------- |
| Authentification | Vérifier qui est l’utilisateur        |
| Autorisation     | Vérifier ce qu’il a le droit de faire |

**Exemple :**

* "Tu es Ahmed" → Authentification
* "Tu peux accéder à /api/admin" → Autorisation

---

## 🏗 Architecture Spring Security Moderne

Depuis Spring Security 6 :

* Plus de `WebSecurityConfigurerAdapter`
* Configuration via **beans**
* Pipeline basé sur des filtres

### 🔑 Composants principaux

| Composant              | Rôle                           |
| ---------------------- | ------------------------------ |
| SecurityFilterChain    | Définit les règles HTTP        |
| DelegatingFilterProxy  | Pont avec le container servlet |
| AuthenticationManager  | Orchestration                  |
| AuthenticationProvider | Logique d’authentification     |
| UserDetailsService     | Chargement des utilisateurs    |
| PasswordEncoder        | Hash des mots de passe         |

### Flux général

```
HTTP Request
   |
   v
SecurityFilterChain
   |
AuthenticationManager
   |
AuthenticationProvider
   |
UserDetailsService + PasswordEncoder
```

---

## 🛡 Security Filter Chain

Spring Security fonctionne comme une chaîne de filtres.

### Filtres principaux

| Filtre                               | Rôle                            |
| ------------------------------------ | ------------------------------- |
| SecurityContextPersistenceFilter     | Gestion du contexte             |
| BasicAuthenticationFilter            | Analyse du header Authorization |
| UsernamePasswordAuthenticationFilter | Form login                      |
| AuthorizationFilter                  | Vérification des rôles          |

---

## 🔍 BasicAuthenticationFilter – Détail

1. Vérifie le header Authorization
2. Decode Base64
3. Appelle AuthenticationManager
4. Crée le SecurityContext
5. Retourne 401 en cas d’échec

```
Client
  |
Authorization: Basic xxx
  |
BasicAuthenticationFilter
  |
AuthenticationManager
  |
AuthenticationProvider
  |
✔ Success → SecurityContext
✘ Failure → 401
```

---

## 🔑 Basic Authentication – Théorie

Header utilisé :

```
Authorization: Basic base64(username:password)
```

⚠ Base64 n’est **pas un chiffrement**

➡ Toujours utiliser HTTPS

### Exemple

```
admin:1234 → YWRtaW46MTIzNA==
```

### Limites

* Credentials envoyés à chaque requête
* Vulnérable sans HTTPS
* Pas adapté aux SPA modernes

---

## ⚙ Configuration Spring Security

### SecurityConfig.java

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/inventory/**").hasAnyRole("ADMIN", "WAREHOUSE_MANAGER")
                .requestMatchers("/api/orders/**").hasAnyRole("CLIENT", "ADMIN")
                .anyRequest().authenticated()
            )
            .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public InMemoryUserDetailsManager userDetailsManager() {

        UserDetails admin = User.builder()
            .username("admin")
            .password(passwordEncoder().encode("admin123"))
            .roles("ADMIN")
            .build();

        UserDetails manager = User.builder()
            .username("manager")
            .password(passwordEncoder().encode("manager123"))
            .roles("WAREHOUSE_MANAGER")
            .build();

        UserDetails client = User.builder()
            .username("client")
            .password(passwordEncoder().encode("client123"))
            .roles("CLIENT")
            .build();

        return new InMemoryUserDetailsManager(admin, manager, client);
    }
}
```

---

## 📁 Rôles et accès

| Endpoint          | ADMIN | MANAGER | CLIENT |
| ----------------- | ----- | ------- | ------ |
| /api/admin/**     | ✔     | ✘       | ✘      |
| /api/inventory/** | ✔     | ✔       | ✘      |
| /api/orders/**    | ✔     | ✘       | ✔      |

---

## 🧪 Tests

### Postman

* Type : Basic Auth
* Username : admin
* Password : admin123

### cURL

```bash
curl -u admin:admin123 http://localhost:8080/api/admin/products
```

### Réponses

| Code | Signification   |
| ---- | --------------- |
| 200  | Succès          |
| 401  | Non authentifié |
| 403  | Non autorisé    |

---

## 🧬 Schéma interne complet

```
Client
  |
Authorization Header
  |
BasicAuthenticationFilter
  |
AuthenticationManager
  |
AuthenticationProvider
  |
BCrypt
  |
SecurityContext
  |
Authorization (roles)
  |
Controller REST
```

---

## 🛡 Bonnes pratiques

* Toujours utiliser HTTPS
* Utiliser BCrypt
* Ne jamais stocker les mots de passe en clair
* Limiter les rôles
* Logger les accès refusés
* Migrer vers JWT/OAuth2 en production

---

## ✅ Conclusion

Cette implémentation constitue une base solide pour comprendre Spring Security et sécuriser une API REST avec Basic Authentication avant d’évoluer vers des solutions modernes comme JWT ou OAuth2.
