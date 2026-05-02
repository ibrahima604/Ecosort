# EcoSort — Application de Tri des Déchets Plastiques

> Projet Tutoré — Application Android + API REST Spring Boot  
> Base de données PostgreSQL hébergée sur **Supabase** (cloud, opérationnelle)

---

## Table des matières

1. [Présentation du projet](#1-présentation-du-projet)
2. [Architecture globale](#2-architecture-globale)
3. [Structure du dépôt](#3-structure-du-dépôt)
4. [Prérequis](#4-prérequis)
5. [Configuration — Backend](#5-configuration--backend)
6. [Lancer le backend](#6-lancer-le-backend)
7. [Lancer l'application Android](#7-lancer-lapplication-android)
8. [Fonctionnalités détaillées](#8-fonctionnalités-détaillées)
9. [API REST — Endpoints](#9-api-rest--endpoints)
10. [Modèle de données](#10-modèle-de-données)
11. [Modèle TFLite — Détection plastique](#11-modèle-tflite--détection-plastique)
12. [Rôles utilisateurs](#12-rôles-utilisateurs)
13. [Fichier application.properties (non versionné)](#13-fichier-applicationproperties-non-versionné)

---

## 1. Présentation du projet

**EcoSort** est une application mobile Android qui permet aux utilisateurs de **scanner des objets** (via caméra ou galerie) pour détecter s'ils sont en plastique ou non, grâce à un modèle de machine learning embarqué (TensorFlow Lite).

L'application propose également :
- Des **statistiques personnelles** de tri (nombre de scans, types de déchets)
- Des **conseils de tri** publiés par un administrateur
- Un **espace administrateur** pour gérer les utilisateurs et les conseils
- Une **synchronisation cloud** avec un backend Spring Boot connecté à PostgreSQL (Supabase)

---

## 2. Architecture globale

```
┌─────────────────────────────┐        REST/JSON        ┌──────────────────────────────┐
│   Application Android       │ ◄─────────────────────► │   Backend Spring Boot        │
│   (Java, CameraX, TFLite)   │      HTTP (Retrofit2)   │   (Java 17, Spring Boot 3)   │
└─────────────────────────────┘                          └──────────────┬───────────────┘
                                                                        │ JPA / Hibernate
                                                                        ▼
                                                         ┌──────────────────────────────┐
                                                         │   PostgreSQL — Supabase       │
                                                         │   (cloud, eu-central-1)       │
                                                         └──────────────────────────────┘
```

- L'app Android communique avec le backend via **Retrofit2**
- Le backend expose une **API REST** sur le port `8989`
- La base de données est hébergée sur **Supabase** (PostgreSQL), accessible en ligne
- La classification plastique/non-plastique est faite **en local** sur l'appareil via TFLite (aucun réseau nécessaire pour scanner)

---

## 3. Structure du dépôt

```
Ecosort/
├── Ecosort/                        # Projet Android (Android Studio)
│   └── app/src/main/
│       ├── java/com/example/ecosort/
│       │   ├── MainActivity.java           # Écran principal, caméra, drawer
│       │   ├── AdminActivity.java          # Interface administrateur
│       │   ├── StatsActivity.java          # Statistiques utilisateur
│       │   ├── StatsFragment.java          # Statistiques admin (bar chart)
│       │   ├── ProfileBottomSheet.java     # Modifier profil + supprimer compte
│       │   ├── PlasticClassifier.java      # Wrapper modèle TFLite
│       │   ├── ApiService.java             # Interface Retrofit (tous les endpoints)
│       │   ├── RetrofitClient.java         # Singleton Retrofit configuré
│       │   ├── DatabaseHelper.java         # SQLite local (cache utilisateur)
│       │   ├── UserRequest.java            # Modèle envoi utilisateur
│       │   ├── UserResponse.java           # Modèle réponse utilisateur
│       │   ├── DechetResponse.java         # Modèle réponse déchet
│       │   ├── TypeDechetResponse.java     # Modèle réponse type déchet
│       │   ├── ConseilResponse.java        # Modèle réponse conseil
│       │   └── AdminResponse.java          # Modèle réponse admin
│       ├── res/
│       │   ├── layout/                     # Tous les XML de mise en page
│       │   ├── drawable/                   # Icônes, backgrounds, formes
│       │   ├── values/colors.xml           # Palette de couleurs (vert EcoSort)
│       │   └── menu/main_menu.xml          # Menu de la barre latérale
│       └── assets/
│           └── model.tflite               # Modèle ML embarqué
│
└── backend/                        # Projet Spring Boot (Maven)
    └── src/main/
        ├── java/fr/ecosort/backend/
        │   ├── controllers/
        │   │   ├── UsersController.java    # CRUD utilisateurs
        │   │   ├── AdminController.java    # Stats admin + auth admin
        │   │   ├── ConseilController.java  # CRUD conseils
        │   │   └── DechetController.java   # Dechets par user + types
        │   ├── models/
        │   │   ├── Users.java              # Entité utilisateur (UUID, héritage)
        │   │   ├── Admin.java              # Hérite de Users, avec password
        │   │   ├── Dechet.java             # Entité déchet (date, type, user)
        │   │   ├── TypeDechet.java         # Type de déchet (label)
        │   │   └── Conseil.java            # Conseil de tri (titre, description)
        │   ├── repositories/               # Interfaces JpaRepository
        │   └── services/
        │       └── ConseilService.java     # Logique métier conseils
        └── resources/
            └── application.properties      #  NON versionné — voir section 13
```

---

## 4. Prérequis

### Backend
| Outil | Version minimale |
|-------|-----------------|
| Java JDK | 17 |
| Maven | 3.8+ |
| Accès Internet | Pour Supabase PostgreSQL |

### Android
| Outil | Version minimale |
|-------|-----------------|
| Android Studio | Hedgehog (2023.1) ou supérieur |
| SDK Android | API 26 (Android 8.0) minimum |
| Gradle | 8.x |
| Appareil/émulateur | Avec caméra (pour le scan) |

---

## 5. Configuration — Backend

Le backend nécessite un fichier `application.properties` dans :
```
backend/src/main/resources/application.properties
```

Ce fichier **n'est pas versionné** sur GitHub (données sensibles). Il est fourni **en pièce jointe** dans l'email du projet.

Voici son contenu complet :

```properties
# ── Base de données PostgreSQL Supabase ──────────────────────────
spring.datasource.url=jdbc:postgresql://aws-1-eu-central-1.pooler.supabase.com:6543/postgres
spring.datasource.username=postgres.yjjaywksvbbxhjcjxtfp
spring.datasource.password=EcoSort758730
spring.datasource.driver-class-name=org.postgresql.Driver

# ── JPA / Hibernate ──────────────────────────────────────────────
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# ── Serveur ──────────────────────────────────────────────────────
server.port=8989

# ── Logs de debug ────────────────────────────────────────────────
logging.level.org.springframework.web=DEBUG
logging.level.com.fasterxml.jackson=DEBUG
```

> La base de données est **active et opérationnelle** sur Supabase.  
> Les tables sont créées automatiquement au premier démarrage (`ddl-auto=update`).

---

## 6. Lancer le backend

```bash
# 1. Cloner le projet
git clone https://github.com/ibrahima604/Ecosort.git
cd Ecosort/backend

# 2. Placer le fichier application.properties dans :
#    src/main/resources/application.properties

# 3. Lancer avec Maven
mvn spring-boot:run

# OU compiler puis lancer le JAR
mvn clean package -DskipTests
java -jar target/backend-*.jar
```

Le serveur démarre sur **http://localhost:8989**

Pour vérifier que l'API fonctionne :
```
GET http://localhost:8989/api/users
GET http://localhost:8989/api/conseils
GET http://localhost:8989/api/admin/stats
```

---

## 7. Lancer l'application Android

1. Ouvrir le dossier `Ecosort/Ecosort/` dans **Android Studio**
2. Laisser Gradle synchroniser les dépendances
3. Dans `res/values/strings.xml`, vérifier que `base_url` pointe vers votre backend :
   ```xml
   <string name="base_url">http://10.0.2.2:8989/</string>
   <!-- Sur émulateur Android : 10.0.2.2 = localhost de la machine hôte -->
   <!-- Sur appareil physique : utiliser l'IP locale de votre machine, ex: http://192.168.1.X:8989/ -->
   ```
4. Brancher un appareil Android ou démarrer un émulateur
5. Cliquer sur **Run ▶**

> ⚠️ Le backend doit être démarré **avant** de lancer l'app pour que les appels réseau fonctionnent.

---

## 8. Fonctionnalités détaillées

### 👤 Côté Utilisateur

| Fonctionnalité | Description |
|---|---|
| **Inscription automatique** | Au premier lancement, un dialog demande nom, prénom, email. L'utilisateur est créé en base et sauvegardé localement (SQLite + SharedPreferences). |
| **Scan caméra** | La caméra s'ouvre en temps réel (CameraX). Le bouton "Scanner" capture le frame et l'analyse avec TFLite. |
| **Upload galerie** | L'utilisateur peut choisir une image depuis sa galerie pour analyse. |
| **Résultat de scan** | Un dialog affiche : Plastique / Non plastique / Incertain, avec le score de confiance en %. |
| **Statistiques** | Accessible via la barre latérale → affiche le total de scans, la répartition plastique/non-plastique, et un détail par type de déchet avec barres de progression. |
| **Conseils de tri** | Liste des conseils publiés par l'admin, avec titre et description. |
| **Modifier profil** | Via la barre latérale → BottomSheet permettant de modifier nom/prénom (synchronisé sur l'API + SQLite local). |
| **Supprimer compte** | Dans le même BottomSheet, section rouge avec confirmation → supprime en base et vide les données locales. |

### Côté Administrateur

L'accès admin est déclenché automatiquement si l'email saisi est `Admin604@gmail.com`.

| Fonctionnalité | Description |
|---|---|
| **Dashboard stats** | Bar chart avec MPAndroidChart : nombre de clients, de tris, de conseils. |
| **Gestion utilisateurs** | Liste de tous les utilisateurs avec possibilité de modifier (nom, prénom, email) ou supprimer. |
| **Gestion conseils** | Créer, modifier, supprimer des conseils de tri visibles par tous les utilisateurs. |

---

## 9. API REST — Endpoints

Base URL : `http://<host>:8989`

### Utilisateurs
| Méthode | Endpoint | Description |
|---------|----------|-------------|
| `POST` | `/api/users` | Créer un utilisateur |
| `GET` | `/api/users` | Lister tous les utilisateurs (hors admin) |
| `GET` | `/api/users/email/{email}` | Trouver un utilisateur par email |
| `PUT` | `/api/users/{id}` | Modifier nom/prénom/email |
| `DELETE` | `/api/users/{id}` | Supprimer un utilisateur |

### Admin
| Méthode | Endpoint | Description |
|---------|----------|-------------|
| `GET` | `/api/admin/email/{email}` | Vérifier si un email est admin |
| `GET` | `/api/admin/stats` | Statistiques globales (users, dechets, conseils) |

### Conseils
| Méthode | Endpoint | Description |
|---------|----------|-------------|
| `GET` | `/api/conseils` | Lister tous les conseils |
| `POST` | `/api/conseils` | Créer un conseil (admin) |
| `PUT` | `/api/conseils/{id}` | Modifier un conseil (admin) |
| `DELETE` | `/api/conseils/{id}` | Supprimer un conseil (admin) |

### Déchets
| Méthode | Endpoint | Description |
|---------|----------|-------------|
| `GET` | `/api/dechets/user/{idClient}` | Tous les déchets scannés par un utilisateur |
| `GET` | `/api/dechets/types` | Tous les types de déchets disponibles |

---

## 10. Modèle de données

```
┌──────────────┐       ┌──────────────┐       ┌──────────────┐
│    users     │       │    dechet    │       │ type_dechet  │
│──────────────│       │──────────────│       │──────────────│
│ id_client UUID PK│  │ id_dechet  PK│       │ id_type_dechet│
│ nom          │       │ date_tri     │       │ label        │
│ prenom       │◄──────│ id_client FK │       └──────────────┘
│ email        │       │ id_type FK   │──────►
└──────┬───────┘       └──────────────┘
       │ (héritage JOINED)
┌──────▼───────┐       ┌──────────────┐
│    admin     │       │   conseil    │
│──────────────│       │──────────────│
│ id_client FK │       │ id_conseil PK│
│ password     │◄──────│ titre        │
└──────────────┘       │ description  │
                       │ id_client FK │
                       └──────────────┘
```

- `Users` et `Admin` utilisent l'héritage JPA **JOINED** : `admin` ne stocke que le `password`, le reste est dans `users`
- Les UUIDs sont générés automatiquement par Hibernate (`GenerationType.UUID`)
- Les tables sont créées/mises à jour automatiquement au démarrage (`ddl-auto=update`)

---

## 11. Modèle TFLite — Détection plastique

Le fichier `model.tflite` est embarqué dans `app/src/main/assets/`.

- **Entrée** : image redimensionnée en `224×224` pixels, normalisée `[0,1]`
- **Sortie** : score `float` entre `0` et `1`
- **Interprétation** :
  - `score ≥ 0.65` →  **Plastique** (recyclable)
  - `score ≤ 0.35` →  **Non plastique**
  - Entre les deux →  **Incertain** (repositionner l'objet)

La classification est effectuée **hors ligne**, directement sur l'appareil — aucune donnée d'image n'est envoyée au serveur.

---

## 12. Rôles utilisateurs

| Email | Rôle | Accès |
|-------|------|-------|
| Tout autre email | Utilisateur | Scan, stats perso, conseils, profil |
| `Admin604@gmail.com` | Administrateur | Dashboard stats, gestion users, gestion conseils |

La détection du rôle se fait au premier lancement : si l'email saisi correspond à l'admin, l'app bascule automatiquement sur `AdminActivity`.

---

## 13. Fichier application.properties (non versionné)

Ce fichier contient les identifiants de connexion à la base de données Supabase. Il est **volontairement exclu du dépôt Git** (`.gitignore`) pour des raisons de sécurité.

**Il est fourni en pièce jointe dans l'email de rendu du projet.**

À placer dans :
```
backend/src/main/resources/application.properties
```

Sans ce fichier, le backend ne peut pas démarrer.

---

## Auteurs

Projet Tutoré — Développement Mobile & Backend  
Dépôt : [https://github.com/ibrahima604/Ecosort](https://github.com/ibrahima604/Ecosort)