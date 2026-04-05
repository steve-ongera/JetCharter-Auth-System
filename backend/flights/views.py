from rest_framework              import generics, permissions, status
from rest_framework.response    import Response
from rest_framework.views       import APIView
from rest_framework_simplejwt.tokens import RefreshToken

from django.contrib.auth import get_user_model

from .serializers import RegisterSerializer, UserProfileSerializer

User = get_user_model()


def _token_pair(user):
    """Return access + refresh token dict for a user."""
    refresh = RefreshToken.for_user(user)
    return {
        "refresh": str(refresh),
        "access":  str(refresh.access_token),
    }


# ── Register ──────────────────────────────────────────────────────────────────
class RegisterView(generics.CreateAPIView):
    """
    POST /api/auth/register/
    Body: username, email, password, password2,
          first_name, last_name, phone, company, role (optional)
    """
    queryset           = User.objects.all()
    serializer_class   = RegisterSerializer
    permission_classes = [permissions.AllowAny]

    def create(self, request, *args, **kwargs):
        serializer = self.get_serializer(data=request.data)
        serializer.is_valid(raise_exception=True)
        user = serializer.save()
        return Response(
            {
                "message": "Account created successfully.",
                "user":    UserProfileSerializer(user).data,
                "tokens":  _token_pair(user),
            },
            status=status.HTTP_201_CREATED,
        )


# ── Login ─────────────────────────────────────────────────────────────────────
class LoginView(APIView):
    """
    POST /api/auth/login/
    Body: username (or email), password
    Returns: access + refresh JWT tokens + user profile
    """
    permission_classes = [permissions.AllowAny]

    def post(self, request):
        identifier = request.data.get("username") or request.data.get("email", "")
        password   = request.data.get("password", "")

        if not identifier or not password:
            return Response(
                {"error": "Both username/email and password are required."},
                status=status.HTTP_400_BAD_REQUEST,
            )

        # Support login via email OR username
        user = (
            User.objects.filter(username=identifier).first()
            or User.objects.filter(email=identifier).first()
        )

        if user is None or not user.check_password(password):
            return Response(
                {"error": "Invalid credentials. Please try again."},
                status=status.HTTP_401_UNAUTHORIZED,
            )

        if not user.is_active:
            return Response(
                {"error": "This account has been disabled."},
                status=status.HTTP_403_FORBIDDEN,
            )

        return Response(
            {
                "message": f"Welcome back, {user.get_full_name() or user.username}!",
                "user":    UserProfileSerializer(user).data,
                "tokens":  _token_pair(user),
            },
            status=status.HTTP_200_OK,
        )


# ── Logout (blacklist refresh token) ─────────────────────────────────────────
class LogoutView(APIView):
    """
    POST /api/auth/logout/
    Header: Authorization: Bearer <access_token>
    Body:   { "refresh": "<refresh_token>" }
    """
    permission_classes = [permissions.IsAuthenticated]

    def post(self, request):
        try:
            token = RefreshToken(request.data.get("refresh"))
            token.blacklist()
            return Response({"message": "Logged out successfully."}, status=status.HTTP_200_OK)
        except Exception:
            return Response({"error": "Invalid or expired refresh token."}, status=status.HTTP_400_BAD_REQUEST)


# ── Token Refresh ─────────────────────────────────────────────────────────────
# Use SimpleJWT's built-in view (wired in urls.py):
#   from rest_framework_simplejwt.views import TokenRefreshView


# ── Me (profile) ─────────────────────────────────────────────────────────────
class MeView(generics.RetrieveUpdateAPIView):
    """
    GET  /api/auth/me/   → current user profile
    PATCH/PUT            → update profile fields (not password)
    Header: Authorization: Bearer <access_token>
    """
    serializer_class   = UserProfileSerializer
    permission_classes = [permissions.IsAuthenticated]

    def get_object(self):
        return self.request.user