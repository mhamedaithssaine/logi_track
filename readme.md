🔐 Spring Security – Sécurisation Initiale de l’API Logistique (Basic Auth)

Ce document constitue la documentation officielle de la phase de sécurisation du projet logistique.
Il définit les fondations de Spring Security moderne (Spring Boot 3 / Spring Security 6) et inclut :

Théorie complète (authentification, authorization, CSRF, CORS…)

Architecture interne (Filters, Provider, Manager…)

Explication de Basic Auth

Implémentation complète : SecurityFilterChain + BCrypt + InMemory Users

Schémas de flux internes

Documentation des endpoints sécurisés

Exemples de tests avec Postman / cURL

📘 Sommaire

Introduction

Authentification vs Autorisation

Architecture Spring Security Moderne

La Security Filter Chain — Explication des filtres

Basic Auth — Théorie & Sécurité

Configuration Spring Security (POC Basic Auth)

Rôles et accès des endpoints

Tests avec Postman / cURL

Schémas internes Spring Security

Bonnes pratiques de sécurité

🔰 Introduction

Cette phase du projet vise à mettre en place une première couche de sécurité pour l’API logistique en utilisant Basic Authentication.

Aucune autre technologie n’est abordée dans cette phase :

❌ JWT
❌ OAuth2
❌ Sessions avancées
❌ Docker
❌ CI/CD

L’objectif est :

✔ comprendre Spring Security en profondeur
✔ construire un POC Basic Auth solide
✔ documenter clairement les mécanismes internes

🧩 Authentification vs Autorisation
Concept	Définition
Authentification	Vérifier qui est l’utilisateur.
Autorisation	Vérifier ce qu’il a le droit de faire.

Exemple :

"Tu es Ahmed" → Authentification

"Tu peux accéder à /api/admin" → Autorisation

🏗 Architecture Spring Security Moderne

Depuis Spring Security 6 :

✔ plus de WebSecurityConfigurerAdapter
✔ configuration 100% via beans
✔ pipeline basé sur :

🔑 Composants principaux
Composant	Rôle
SecurityFilterChain	Définit les règles de sécurité HTTP
DelegatingFilterProxy	Pont entre Spring Security et le container servlet
AuthenticationManager	Orchestre l’authentification
AuthenticationProvider	Exécute la logique d’authentification
UserDetailsService	Charge les utilisateurs
PasswordEncoder	Hash des mots de passe (BCrypt recommandé)
Schéma (flux général)
HTTP Request
│
▼
┌──────────────────────────┐
│   SecurityFilterChain    │
└──────────────────────────┘
│
▼
AuthenticationManager
│
▼
AuthenticationProvider
│
▼
UserDetailsService + PasswordEncoder

🛡 La Security Filter Chain — Explication des filtres

Spring Security fonctionne comme une chaîne de filtres.

Voici les filtres principaux utiles pour Basic Auth :

Filtre	Rôle
SecurityContextPersistenceFilter	Charge/mets à jour le SecurityContext
BasicAuthenticationFilter	Analyse le header Authorization: Basic xxx
UsernamePasswordAuthenticationFilter	Gère formLogin (pas utilisé ici)
AuthorizationFilter	Vérifie les permissions (roles/authorities)
🔍 Zoom : BasicAuthenticationFilter
1. Vérifie la présence du header Authorization
2. Décodage Base64 → "username:password"
3. Authentification via AuthenticationManager
4. Création du SecurityContext si succès
5. Retour 401 si échec

ASCII Diagramme intégré au README :
Client → HTTP Request
Authorization: Basic dXNlcjpwYXNz
│
▼
BasicAuthenticationFilter
│
decode Base64 → username:password
│
▼
AuthenticationManager
│
▼
AuthenticationProvider
│
├── compare password (BCrypt)
▼
✔ Success → SecurityContext stored
✘ Failure → 401 Unauthorized

🔑 Basic Auth — Théorie & Sécurité
➤ Définition

Basic Auth envoie les credentials dans le header :

Authorization: Basic base64("username:password")


Attention : Base64 ≠ sécurité
C’est juste une encodage, pas un chiffrement.

👉 Basic Auth doit impérativement être utilisé avec HTTPS

➤ Exemple d'encodage
username: admin
password: 1234
"admin:1234" → Base64 → YWRtaW46MTIzNA==

➤ Limites

❌ vulnérable sans HTTPS
❌ mots de passe envoyés à chaque requête
❌ pas adapté aux applications modernes (mobile, SPA)

⚙ Configuration Spring Security — POC Basic Auth
📄 SecurityConfig.java
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

📁 Rôles et accès des endpoints
Endpoint	ADMIN	WAREHOUSE_MANAGER	CLIENT
/api/admin/**	✔	✘	✘
/api/inventory/**	✔	✔	✘
/api/products/**	✔	✔	✘
/api/orders/**	✔	✘	✔
/api/shipments/**	✔	✔	✘
🧪 Tests avec Postman / cURL
➤ 1. Test via Postman

Auth → Type : Basic Auth

Username : admin

Password : admin123

➤ 2. Test via cURL
curl -u admin:admin123 http://localhost:8080/api/admin/products


Réponses attendues :

200 → succès

401 → pas authentifié

403 → authentifié mais pas autorisé

🧬 Schéma complet : Flux interne Basic Auth
┌────────────────────────────┐
│        Client API          │
└────────────────────────────┘
│
▼
Authorization: Basic <Base64>
│
▼
┌──────────────────────────────────┐
│     BasicAuthenticationFilter    │
└──────────────────────────────────┘
│
decode Base64 → username/password
│
▼
┌──────────────────────────────────┐
│       AuthenticationManager      │
└──────────────────────────────────┘
│
▼
┌──────────────────────────────────┐
│       AuthenticationProvider     │
└──────────────────────────────────┘
│
compares password (BCrypt)
│
┌────────────┴───────────┐
│                        │
(success)                 (failure)
│                        │
▼                        ▼
SecurityContext created         401 Unauthorized
│
▼
AccessDecisionManager (roles)
│
▼
Endpoint Controller REST

🛡 Bonnes pratiques

✔ Toujours utiliser HTTPS
✔ Ne jamais stocker un mot de passe en clair
✔ Toujours utiliser BCrypt
✔ Minimiser les permissions
✔ Logger les tentatives d’accès non autorisé
✔ Préférer JWT ou OAuth2 pour la version finale