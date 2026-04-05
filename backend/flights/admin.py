from django.contrib import admin
from django.contrib.auth.admin import UserAdmin
from django.utils.html import format_html

from .models import User


@admin.register(User)
class CustomUserAdmin(UserAdmin):

    # ── List view ─────────────────────────────────────────────────────────────
    list_display  = (
        "username", "full_name", "email",
        "role_badge", "phone", "company",
        "is_active", "is_staff", "created_at",
    )
    list_filter   = ("role", "is_active", "is_staff", "created_at")
    search_fields = ("username", "email", "first_name", "last_name", "phone", "company")
    ordering      = ("-created_at",)
    list_per_page = 25

    # ── Detail / edit form ────────────────────────────────────────────────────
    fieldsets = (
        ("Account", {
            "fields": ("username", "password"),
        }),
        ("Personal Info", {
            "fields": (
                "first_name", "last_name", "email",
                "phone", "company", "avatar_url",
            ),
        }),
        ("Role & Access", {
            "fields": ("role", "is_active", "is_staff", "is_superuser"),
        }),
        ("Groups & Permissions", {
            "classes": ("collapse",),
            "fields": ("groups", "user_permissions"),
        }),
        ("Important Dates", {
            "classes": ("collapse",),
            "fields": ("last_login", "date_joined", "created_at"),
        }),
    )
    readonly_fields = ("created_at", "last_login", "date_joined")

    # ── Add user form (shown when creating a new user) ────────────────────────
    add_fieldsets = (
        ("Account", {
            "classes": ("wide",),
            "fields": ("username", "email", "password1", "password2"),
        }),
        ("Personal Info", {
            "classes": ("wide",),
            "fields": ("first_name", "last_name", "phone", "company", "avatar_url"),
        }),
        ("Role", {
            "classes": ("wide",),
            "fields": ("role",),
        }),
    )

    # ── Custom display columns ────────────────────────────────────────────────

    @admin.display(description="Full Name")
    def full_name(self, obj):
        return f"{obj.first_name} {obj.last_name}".strip() or "—"

    @admin.display(description="Role")
    def role_badge(self, obj):
        colours = {
            "client": "#0d6efd",   # blue
            "owner":  "#198754",   # green
            "admin":  "#dc3545",   # red
        }
        colour = colours.get(obj.role, "#6c757d")
        return format_html(
            '<span style="'
            'background:{};color:#fff;padding:2px 10px;'
            'border-radius:12px;font-size:11px;font-weight:600;">'
            '{}</span>',
            colour,
            obj.get_role_display(),
        )

    # ── Quick actions ─────────────────────────────────────────────────────────

    @admin.action(description="Activate selected users")
    def activate_users(self, request, queryset):
        updated = queryset.update(is_active=True)
        self.message_user(request, f"{updated} user(s) activated.")

    @admin.action(description="Deactivate selected users")
    def deactivate_users(self, request, queryset):
        updated = queryset.update(is_active=False)
        self.message_user(request, f"{updated} user(s) deactivated.")

    @admin.action(description="Set role → Client")
    def set_role_client(self, request, queryset):
        queryset.update(role="client")
        self.message_user(request, "Selected users set to Client.")

    @admin.action(description="Set role → Owner")
    def set_role_owner(self, request, queryset):
        queryset.update(role="owner")
        self.message_user(request, "Selected users set to Owner.")

    actions = [
        "activate_users",
        "deactivate_users",
        "set_role_client",
        "set_role_owner",
    ]