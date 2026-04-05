import java.io.*;
import java.net.*;
import java.net.http.*;
import java.time.Duration;
import java.util.Scanner;

/**
 * JetCharterAuthClient — Zero dependencies. Pure Java 11+ standard library only.
 *
 * Compile:
 *   javac JetCharterAuthClient.java
 *
 * Run (always from terminal, NOT VSCode Run button — password masking needs a real terminal):
 *   java JetCharterAuthClient
 */
public class JetCharterAuthClient {

    // ── Config ────────────────────────────────────────────────────────────────
    private static final String BASE_URL     = "http://127.0.0.1:8000/api";
    private static final String URL_LOGIN    = BASE_URL + "/auth/login/";
    private static final String URL_REGISTER = BASE_URL + "/auth/register/";
    private static final String URL_ME       = BASE_URL + "/auth/me/";
    private static final String URL_LOGOUT   = BASE_URL + "/auth/logout/";
    private static final String URL_REFRESH  = BASE_URL + "/auth/refresh/";

    // ── Session state ─────────────────────────────────────────────────────────
    private static String accessToken  = null;
    private static String refreshToken = null;

    // ── Stored profile fields ─────────────────────────────────────────────────
    private static String profileUsername = "";
    private static String profileRole     = "";
    private static String profileEmail    = "";
    private static String profileFirst    = "";
    private static String profileLast     = "";
    private static String profilePhone    = "";
    private static String profileCompany  = "";
    private static String profileJoined   = "";

    // ── HTTP client ───────────────────────────────────────────────────────────
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    // ── Entry point ───────────────────────────────────────────────────────────
    public static void main(String[] args) {
        clearScreen();
        printBanner();

        Scanner sc = new Scanner(System.in);

        while (true) {
            if (accessToken == null) {
                printGuestMenu();
                String choice = prompt(sc, "Select");
                switch (choice.trim()) {
                    case "1" -> doLogin(sc);
                    case "2" -> doRegister(sc);
                    case "0" -> { print("\nGoodbye. ✈\n"); return; }
                    default  -> print("  ⚠  Invalid option.\n");
                }
            } else {
                printUserMenu();
                String choice = prompt(sc, "Select");
                switch (choice.trim()) {
                    case "1" -> showProfile();
                    case "2" -> doRefreshToken();
                    case "3" -> doLogout(sc);
                    case "0" -> { print("\nGoodbye. ✈\n"); return; }
                    default  -> print("  ⚠  Invalid option.\n");
                }
            }
        }
    }

    // ── Menus ─────────────────────────────────────────────────────────────────
    private static void printBanner() {
        print("╔══════════════════════════════════════════════╗");
        print("║     ✈  JET CHARTER PLATFORM  ✈              ║");
        print("║        Auth Console  v1.0                   ║");
        print("╚══════════════════════════════════════════════╝\n");
    }

    private static void printGuestMenu() {
        print("\n┌─────────────────────────────┐");
        print("│         MAIN MENU           │");
        print("├─────────────────────────────┤");
        print("│  1. Login                   │");
        print("│  2. Register                │");
        print("│  0. Exit                    │");
        print("└─────────────────────────────┘");
    }

    private static void printUserMenu() {
        print("\n┌─────────────────────────────────────┐");
        print(String.format("│  Logged in as: %-20s │", profileUsername));
        print(String.format("│  Role        : %-20s │", profileRole));
        print("├─────────────────────────────────────┤");
        print("│  1. View my profile                 │");
        print("│  2. Refresh access token            │");
        print("│  3. Logout                          │");
        print("│  0. Exit                            │");
        print("└─────────────────────────────────────┘");
    }

    // ── Actions ───────────────────────────────────────────────────────────────

    private static void doLogin(Scanner sc) {
        print("\n── LOGIN ────────────────────────────────");
        String username = prompt(sc, "Username or Email");
        String password = promptPassword(sc, "Password");

        String body = "{"
                + "\"username\":\"" + escape(username) + "\","
                + "\"password\":\"" + escape(password) + "\""
                + "}";

        try {
            String response = post(URL_LOGIN, body, null);
            if (response == null) return;

            if (response.contains("\"access\"")) {
                extractAndStoreTokens(response);
                extractAndStoreProfile(response);
                print("\n  ✅  " + extractField(response, "message"));
                printProfileSummary();
            } else {
                printError(response);
            }
        } catch (Exception e) {
            print("  ❌  Connection error: " + e.getMessage());
        }
    }

    private static void doRegister(Scanner sc) {
        print("\n── REGISTER ─────────────────────────────");
        String username  = prompt(sc, "Username");
        String email     = prompt(sc, "Email");
        String firstName = prompt(sc, "First Name");
        String lastName  = prompt(sc, "Last Name");
        String phone     = prompt(sc, "Phone       (press Enter to skip)");
        String company   = prompt(sc, "Company     (press Enter to skip)");
        String password  = promptPassword(sc, "Password    (min 8 chars)");
        String password2 = promptPassword(sc, "Confirm Password");

        print("  Role: 1 = Client   2 = Owner");
        String roleChoice = prompt(sc, "Role [1]");
        String role = roleChoice.trim().equals("2") ? "owner" : "client";

        String body = "{"
                + "\"username\":\""   + escape(username)  + "\","
                + "\"email\":\""      + escape(email)     + "\","
                + "\"first_name\":\"" + escape(firstName) + "\","
                + "\"last_name\":\""  + escape(lastName)  + "\","
                + "\"password\":\""   + escape(password)  + "\","
                + "\"password2\":\""  + escape(password2) + "\","
                + "\"role\":\""       + role              + "\","
                + "\"phone\":\""      + escape(phone)     + "\","
                + "\"company\":\""    + escape(company)   + "\""
                + "}";

        try {
            String response = post(URL_REGISTER, body, null);
            if (response == null) return;

            if (response.contains("\"access\"")) {
                extractAndStoreTokens(response);
                extractAndStoreProfile(response);
                print("\n  ✅  " + extractField(response, "message"));
                printProfileSummary();
            } else {
                printError(response);
            }
        } catch (Exception e) {
            print("  ❌  Connection error: " + e.getMessage());
        }
    }

    private static void showProfile() {
        print("\n── MY PROFILE ───────────────────────────");
        try {
            String response = get(URL_ME, accessToken);
            if (response == null) return;

            if (response.contains("\"username\"")) {
                // Refresh stored profile from live data
                extractAndStoreProfileDirect(response);
                print(String.format("  %-15s %s %s", "Name:",    profileFirst, profileLast));
                print(String.format("  %-15s %s",    "Username:", profileUsername));
                print(String.format("  %-15s %s",    "Email:",    profileEmail));
                print(String.format("  %-15s %s",    "Role:",     profileRole));
                print(String.format("  %-15s %s",    "Phone:",    profilePhone.isBlank()   ? "—" : profilePhone));
                print(String.format("  %-15s %s",    "Company:",  profileCompany.isBlank() ? "—" : profileCompany));
                print(String.format("  %-15s %s",    "Joined:",   profileJoined));
            } else {
                printError(response);
            }
        } catch (Exception e) {
            print("  ❌  Error: " + e.getMessage());
        }
    }

    private static void doRefreshToken() {
        print("\n── REFRESH TOKEN ────────────────────────");
        if (refreshToken == null) { print("  ⚠  No refresh token available."); return; }

        String body = "{\"refresh\":\"" + escape(refreshToken) + "\"}";
        try {
            String response = post(URL_REFRESH, body, null);
            if (response == null) return;

            String newAccess = extractField(response, "access");
            if (!newAccess.isEmpty()) {
                accessToken = newAccess;
                String newRefresh = extractField(response, "refresh");
                if (!newRefresh.isEmpty()) refreshToken = newRefresh;
                print("  ✅  Access token refreshed successfully.");
            } else {
                printError(response);
            }
        } catch (Exception e) {
            print("  ❌  Error: " + e.getMessage());
        }
    }

    private static void doLogout(Scanner sc) {
        print("\n── LOGOUT ───────────────────────────────");
        String confirm = prompt(sc, "Are you sure? (y/N)");
        if (!confirm.trim().equalsIgnoreCase("y")) { print("  Cancelled."); return; }

        try {
            String body = "{\"refresh\":\"" + escape(refreshToken != null ? refreshToken : "") + "\"}";
            String response = post(URL_LOGOUT, body, accessToken);
            if (response != null && response.contains("message")) {
                print("  ✅  " + extractField(response, "message"));
            }
        } catch (Exception e) {
            print("  ⚠  Server logout failed — clearing session locally.");
        }

        accessToken = null; refreshToken = null;
        profileUsername = ""; profileRole = ""; profileEmail = "";
        profileFirst = ""; profileLast = ""; profilePhone = "";
        profileCompany = ""; profileJoined = "";
        print("  Session cleared.");
    }

    // ── HTTP helpers ──────────────────────────────────────────────────────────

    private static String post(String url, String jsonBody, String bearer) throws Exception {
        HttpRequest.Builder req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(15))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody));
        if (bearer != null) req.header("Authorization", "Bearer " + bearer);

        HttpResponse<String> resp = HTTP.send(req.build(), HttpResponse.BodyHandlers.ofString());
        return resp.body();
    }

    private static String get(String url, String bearer) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(15))
                .header("Authorization", "Bearer " + bearer)
                .GET()
                .build();
        HttpResponse<String> resp = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
        return resp.body();
    }

    // ── Simple JSON field extractor (no library needed) ───────────────────────
    // Handles:  "key":"value"  and  "key": "value"

    private static String extractField(String json, String key) {
        String search = "\"" + key + "\"";
        int ki = json.indexOf(search);
        if (ki < 0) return "";
        int colon = json.indexOf(":", ki + search.length());
        if (colon < 0) return "";
        int start = json.indexOf("\"", colon + 1);
        if (start < 0) return "";
        int end = json.indexOf("\"", start + 1);
        // Handle escaped quotes inside value
        while (end > 0 && json.charAt(end - 1) == '\\') {
            end = json.indexOf("\"", end + 1);
        }
        if (end < 0) return "";
        return json.substring(start + 1, end);
    }

    /** Extract a field that lives inside a nested object (e.g. tokens.access) */
    private static String extractNested(String json, String outerKey, String innerKey) {
        String search = "\"" + outerKey + "\"";
        int oi = json.indexOf(search);
        if (oi < 0) return "";
        int braceOpen = json.indexOf("{", oi);
        if (braceOpen < 0) return "";
        int braceClose = json.indexOf("}", braceOpen);
        if (braceClose < 0) return "";
        String sub = json.substring(braceOpen, braceClose + 1);
        return extractField(sub, innerKey);
    }

    private static void extractAndStoreTokens(String json) {
        accessToken  = extractNested(json, "tokens", "access");
        refreshToken = extractNested(json, "tokens", "refresh");
    }

    private static void extractAndStoreProfile(String json) {
        // profile is inside the "user" object
        String search = "\"user\"";
        int ui = json.indexOf(search);
        if (ui < 0) return;
        int braceOpen = json.indexOf("{", ui);
        if (braceOpen < 0) return;
        // find matching closing brace
        int depth = 0; int braceClose = braceOpen;
        for (int i = braceOpen; i < json.length(); i++) {
            if (json.charAt(i) == '{') depth++;
            else if (json.charAt(i) == '}') { depth--; if (depth == 0) { braceClose = i; break; } }
        }
        String userJson = json.substring(braceOpen, braceClose + 1);
        extractAndStoreProfileDirect(userJson);
    }

    private static void extractAndStoreProfileDirect(String userJson) {
        profileUsername = extractField(userJson, "username");
        profileRole     = extractField(userJson, "role");
        profileEmail    = extractField(userJson, "email");
        profileFirst    = extractField(userJson, "first_name");
        profileLast     = extractField(userJson, "last_name");
        profilePhone    = extractField(userJson, "phone");
        profileCompany  = extractField(userJson, "company");
        profileJoined   = extractField(userJson, "created_at");
    }

    private static void printProfileSummary() {
        print("  ┌──────────────────────────────────────");
        print(String.format("  │  Name     : %s %s", profileFirst, profileLast));
        print(String.format("  │  Username : %s",    profileUsername));
        print(String.format("  │  Role     : %s",    profileRole));
        print("  └──────────────────────────────────────");
    }

    private static void printError(String json) {
        // Try common error keys DRF returns
        for (String key : new String[]{"error", "detail", "non_field_errors", "username", "email", "password"}) {
            String val = extractField(json, key);
            if (!val.isEmpty()) {
                print("  ❌  " + key + ": " + val);
            }
        }
    }

    // ── UI helpers ────────────────────────────────────────────────────────────

    private static String prompt(Scanner sc, String label) {
        System.out.print("  " + label + ": ");
        return sc.nextLine();
    }

    private static String promptPassword(Scanner sc, String label) {
        Console console = System.console();
        if (console != null) {
            char[] pwd = console.readPassword("  %s: ", label);
            return new String(pwd);
        }
        // Fallback for IDEs — input will be visible
        System.out.print("  " + label + " (run from terminal to hide): ");
        return sc.nextLine();
    }

    /** Escape special characters for safe JSON string embedding */
    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private static void print(String s) {
        System.out.println(s);
    }

    private static void clearScreen() {
        try {
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        } catch (Exception ignored) {}
    }
}