#  JetCharter Auth System

A **Django REST Framework** backend with **JWT authentication** paired with a **Java terminal console client** for login and registration.

---

## 📁 Project Structure

```
jetcharter/
│
├── backend/                          # Django project root
│   ├── manage.py
│   ├── requirements.txt
│   │
│   ├── config/                       # Django project config (rename to your project name)
│   │   ├── __init__.py
│   │   ├── settings.py               # ← merge settings_snippet.py here
│   │   ├── urls.py                   # ← add path("api/", include("flights.urls"))
│   │   ├── asgi.py
│   │   └── wsgi.py
│   │
│   └── flights/                      # Your main Django app
│       ├── __init__.py
│       ├── models.py                 # Custom User + all booking models
│       ├── serializers.py            # ← copy from output
│       ├── views.py                  # ← copy from output
│       ├── urls.py                   # ← copy from output
│       ├── admin.py
│       ├── apps.py
│       │
│       └── management/
│           ├── __init__.py
│           └── commands/
│               ├── __init__.py
│               └── seed_data.py      # ← data seeder
│
└── java-client/                      # Java console client
    ├── pom.xml                       # ← copy from output
    ├── src/
    │   └── main/
    │       └── java/
    │           └── JetCharterAuthClient.java   # ← copy from output
    └── target/                       # auto-generated after mvn package
        └── auth-console-client-1.0.0.jar
```

---

## ⚙️ Prerequisites

Make sure the following are installed:

| Tool | Version | Check |
|------|---------|-------|
| Python | 3.10+ | `python --version` |
| pip | latest | `pip --version` |
| Java JDK | 17+ | `java --version` |
| Maven | 3.8+ | `mvn --version` |
| VSCode | latest | — |

---

## 🐍 Backend Setup (Django)

### 1. Create and activate a virtual environment

```bash
cd jetcharter/backend

# Windows
python -m venv venv
venv\Scripts\activate

# macOS / Linux
python -m venv venv
source venv/bin/activate
```

### 2. Install dependencies

```bash
pip install -r requirements.txt
```

Create `requirements.txt` in `backend/` with:

```
django>=4.2
djangorestframework>=3.15
djangorestframework-simplejwt>=5.3
```

### 3. Copy the generated files

| Output file | Copy to |
|---|---|
| `serializers.py` | `backend/flights/serializers.py` |
| `views.py` | `backend/flights/views.py` |
| `urls.py` | `backend/flights/urls.py` |
| `seed_data.py` | `backend/flights/management/commands/seed_data.py` |

> Create `management/__init__.py` and `management/commands/__init__.py` as empty files.

### 4. Update `settings.py`

Open `backend/config/settings.py` and add/merge the following:

```python
INSTALLED_APPS = [
    # ... existing apps ...
    "rest_framework",
    "rest_framework_simplejwt",
    "rest_framework_simplejwt.token_blacklist",
    "flights",
]

AUTH_USER_MODEL = "flights.User"

REST_FRAMEWORK = {
    "DEFAULT_AUTHENTICATION_CLASSES": (
        "rest_framework_simplejwt.authentication.JWTAuthentication",
    ),
    "DEFAULT_PERMISSION_CLASSES": (
        "rest_framework.permissions.IsAuthenticated",
    ),
}

from datetime import timedelta

SIMPLE_JWT = {
    "ACCESS_TOKEN_LIFETIME":    timedelta(minutes=60),
    "REFRESH_TOKEN_LIFETIME":   timedelta(days=7),
    "ROTATE_REFRESH_TOKENS":    True,
    "BLACKLIST_AFTER_ROTATION": True,
    "AUTH_HEADER_TYPES":        ("Bearer",),
}
```

### 5. Update `config/urls.py`

```python
from django.contrib import admin
from django.urls import path, include

urlpatterns = [
    path("admin/", admin.site.urls),
    path("api/", include("flights.urls")),
]
```

### 6. Run migrations

```bash
python manage.py makemigrations
python manage.py migrate
```

### 7. Create a superuser (optional)

```bash
python manage.py createsuperuser
```

### 8. Seed the database

```bash
# Seed with existing Airport / Aircraft / Yacht data
python manage.py seed_data

# Clear everything first, then re-seed
python manage.py seed_data --clear
```

### 9. Start the Django server

```bash
python manage.py runserver
```

Server runs at: **http://127.0.0.1:8000**

---

## 🌐 API Endpoints

| Method | Endpoint | Auth Required | Description |
|--------|----------|:-------------:|-------------|
| `POST` | `/api/auth/register/` | No | Create a new account |
| `POST` | `/api/auth/login/` | No | Login, receive JWT tokens |
| `POST` | `/api/auth/logout/` | Bearer token | Blacklist refresh token |
| `POST` | `/api/auth/refresh/` | No | Get new access token |
| `GET`  | `/api/auth/me/` | Bearer token | View current user profile |
| `PATCH`| `/api/auth/me/` | Bearer token | Update profile fields |

### Example — Login request

```json
POST /api/auth/login/
{
  "username": "johndoe",
  "password": "Pass1234!"
}
```

### Example — Login response

```json
{
  "message": "Welcome back, John Doe!",
  "user": {
    "id": 1,
    "username": "johndoe",
    "email": "john@example.com",
    "role": "client"
  },
  "tokens": {
    "access":  "eyJhbGci...",
    "refresh": "eyJhbGci..."
  }
}
```

---

## Coffee Java Console Client Setup

### 1. Open the java-client folder in VSCode

```bash
cd jetcharter/java-client
code .
```

### 2. Install the Extension Pack for Java in VSCode

Open Extensions (`Ctrl+Shift+X`) and install:

```
Extension Pack for Java   (by Microsoft)
```

This gives you Maven support, Java debugging, and IntelliSense.

### 3. Copy the generated files

| Output file | Copy to |
|---|---|
| `JetCharterAuthClient.java` | `java-client/src/main/java/JetCharterAuthClient.java` |
| `pom.xml` | `java-client/pom.xml` |

### 4. Build the JAR (Maven)

Open the VSCode integrated terminal (`Ctrl+backtick`) and run:

```bash
mvn package
```

This creates: `target/auth-console-client-1.0.0.jar`

### 5. Run the client

> Always run from the terminal, not the VSCode Run button.
> Password hiding only works in a real terminal session.

```bash
# macOS / Linux
java -jar target/auth-console-client-1.0.0.jar

# Windows Command Prompt
java -jar target\auth-console-client-1.0.0.jar
```

---

## Terminal — What You'll See

```
╔══════════════════════════════════════════════╗
║     ✈  JET CHARTER PLATFORM  ✈              ║
║        Auth Console  v1.0                   ║
╚══════════════════════════════════════════════╝

┌─────────────────────────────┐
│         MAIN MENU           │
├─────────────────────────────┤
│  1. Login                   │
│  2. Register                │
│  0. Exit                    │
└─────────────────────────────┘

  Select:
```

After login:

```
┌─────────────────────────────────────┐
│  Logged in as: johndoe              │
│  Role        : client               │
├─────────────────────────────────────┤
│  1. View my profile                 │
│  2. Refresh access token            │
│  3. Logout                          │
│  0. Exit                            │
└─────────────────────────────────────┘
```

---

## VSCode Recommended Extensions

Open Extensions (`Ctrl+Shift+X`) and install these:

```
Python                    (by Microsoft)
Pylance                   (by Microsoft)
Django                    (by Baptiste Darthenay)
Extension Pack for Java   (by Microsoft)
REST Client               (by Huachao Mao)
```

### Test API directly in VSCode with REST Client

Create a file `test.http` in your project root:

```http
### Login
POST http://127.0.0.1:8000/api/auth/login/
Content-Type: application/json

{
  "username": "johndoe",
  "password": "Pass1234!"
}

### Get profile (replace token below)
GET http://127.0.0.1:8000/api/auth/me/
Authorization: Bearer YOUR_ACCESS_TOKEN_HERE

### Register
POST http://127.0.0.1:8000/api/auth/register/
Content-Type: application/json

{
  "username": "newuser",
  "email": "new@example.com",
  "password": "Pass1234!",
  "password2": "Pass1234!",
  "first_name": "New",
  "last_name": "User",
  "role": "client"
}
```

Click **Send Request** above each block to fire the request inline.

### Useful VSCode shortcuts

| Action | Shortcut (Windows/Linux) | Shortcut (macOS) |
|--------|--------------------------|-----------------|
| Open terminal | `Ctrl + backtick` | `Cmd + backtick` |
| Open command palette | `Ctrl + Shift + P` | `Cmd + Shift + P` |
| Toggle file explorer | `Ctrl + Shift + E` | `Cmd + Shift + E` |
| Split terminal | `Ctrl + Shift + 5` | `Cmd + Shift + 5` |
| Format document | `Shift + Alt + F` | `Shift + Option + F` |

---

## Running Both Together

Open two terminals side by side in VSCode (`Ctrl+Shift+5`):

**Terminal 1 — Django server:**

```bash
cd backend
source venv/bin/activate        # Windows: venv\Scripts\activate
python manage.py runserver
```

**Terminal 2 — Java console client:**

```bash
cd java-client
java -jar target/auth-console-client-1.0.0.jar
```

---

## Troubleshooting

| Problem | Fix |
|---------|-----|
| `AUTH_USER_MODEL` error on migrate | Set `AUTH_USER_MODEL = "flights.User"` before the very first `migrate` |
| `No module named rest_framework` | Run `pip install djangorestframework` inside your active venv |
| Java: password input is visible | Run from a real terminal — `System.console()` is null in VSCode's Run button |
| `Connection refused` on Java client | Make sure Django is running: `python manage.py runserver` |
| Token blacklist table missing | Add `rest_framework_simplejwt.token_blacklist` to `INSTALLED_APPS` then `python manage.py migrate` |
| Maven build fails | Confirm JDK 17+: `java --version`. Install from https://adoptium.net if needed |
| `seed_data` command not found | Check that `management/` and `commands/` both have empty `__init__.py` files |

---

## File Summary

| File | What it does |
|------|-------------|
| `serializers.py` | Validates register/login input, serializes User model |
| `views.py` | Register, Login, Logout, Me, token refresh API views |
| `urls.py` | Maps the 5 auth endpoints |
| `settings_snippet.py` | JWT + DRF config to merge into settings.py |
| `seed_data.py` | Management command — seeds 560+ records for Apr 2025–Apr 2026 |
| `JetCharterAuthClient.java` | Interactive terminal client — login, register, profile, logout |
| `pom.xml` | Maven build — packages a runnable JAR with Jackson |

---

## License

MIT — free for personal and commercial use.