from rest_framework import serializers
from django.contrib.auth import get_user_model

User = get_user_model()


class RegisterSerializer(serializers.ModelSerializer):
    password  = serializers.CharField(write_only=True, min_length=8)
    password2 = serializers.CharField(write_only=True, label="Confirm password")

    class Meta:
        model  = User
        fields = [
            "username", "email", "password", "password2",
            "first_name", "last_name", "phone", "company", "role",
        ]
        extra_kwargs = {
            "role": {"required": False},   # defaults to 'client'
            "email": {"required": True},
        }

    def validate(self, attrs):
        if attrs["password"] != attrs.pop("password2"):
            raise serializers.ValidationError({"password": "Passwords do not match."})
        return attrs

    def create(self, validated_data):
        return User.objects.create_user(
            username   = validated_data["username"],
            email      = validated_data["email"],
            password   = validated_data["password"],
            first_name = validated_data.get("first_name", ""),
            last_name  = validated_data.get("last_name",  ""),
            phone      = validated_data.get("phone",      ""),
            company    = validated_data.get("company",    ""),
            role       = validated_data.get("role",       "client"),
        )


class UserProfileSerializer(serializers.ModelSerializer):
    class Meta:
        model  = User
        fields = [
            "id", "username", "email",
            "first_name", "last_name",
            "phone", "company", "role",
            "avatar_url", "created_at",
        ]
        read_only_fields = ["id", "username", "created_at"]