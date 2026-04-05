# yourapp/urls.py  (auth routes)
# Include this in your project's main urls.py:
#
#   from django.urls import path, include
#   urlpatterns = [
#       ...
#       path("api/", include("yourapp.urls")),
#   ]

from django.urls import path
from rest_framework_simplejwt.views import TokenRefreshView

from .views import RegisterView, LoginView, LogoutView, MeView

urlpatterns = [
    path("auth/register/", RegisterView.as_view(),  name="auth-register"),
    path("auth/login/",    LoginView.as_view(),     name="auth-login"),
    path("auth/logout/",   LogoutView.as_view(),    name="auth-logout"),
    path("auth/refresh/",  TokenRefreshView.as_view(), name="auth-refresh"),
    path("auth/me/",       MeView.as_view(),        name="auth-me"),
]