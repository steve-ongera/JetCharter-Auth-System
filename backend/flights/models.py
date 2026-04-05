import uuid

from django.contrib.auth.models import AbstractUser
from django.db import models


# ─────────────────────────────────────────────────────────────────────────────
# USER  —  set AUTH_USER_MODEL = 'flights.User' in settings.py
# ─────────────────────────────────────────────────────────────────────────────

class User(AbstractUser):
    ROLE_CHOICES = [
        ('client', 'Membership Client'),
        ('owner',  'Fleet Owner'),
        ('admin',  'Platform Admin'),
    ]
    role       = models.CharField(max_length=10, choices=ROLE_CHOICES, default='client')
    phone      = models.CharField(max_length=30, blank=True)
    company    = models.CharField(max_length=200, blank=True)
    avatar_url = models.URLField(blank=True)
    created_at = models.DateTimeField(auto_now_add=True)

    def __str__(self):
        return f"{self.username} ({self.get_role_display()})"