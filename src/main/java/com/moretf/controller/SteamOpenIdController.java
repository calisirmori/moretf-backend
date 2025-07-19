package com.moretf.controller;

import com.moretf.service.SecretManagerService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

@RestController
@RequestMapping("/auth")
public class SteamOpenIdController {
    @Autowired
    private SecretManagerService secretManagerService;
    private final String STEAM_OPENID_URL = "https://steamcommunity.com/openid/login";
    private final String REALM = "http://localhost:8080"; // change to www.more.tf in prod
    private final String RETURN_TO = "http://localhost:8080/auth/verify";
//    private final String REALM = "https://api.more.tf";
//    private final String RETURN_TO = "https://api.more.tf/auth/verify";
    private final String FRONT_END = "http://localhost:5173";

    @GetMapping("/login")
    public void login(@RequestParam(value = "state", required = false) String state, HttpServletResponse response) throws IOException {
        String finalReturnTo = RETURN_TO;
        if (state != null) {
            finalReturnTo += "?state=" + URLEncoder.encode(state, StandardCharsets.UTF_8);
        }

        String url = STEAM_OPENID_URL +
                "?openid.ns=" + URLEncoder.encode("http://specs.openid.net/auth/2.0", StandardCharsets.UTF_8) +
                "&openid.mode=checkid_setup" +
                "&openid.return_to=" + URLEncoder.encode(finalReturnTo, StandardCharsets.UTF_8) +
                "&openid.realm=" + URLEncoder.encode(REALM, StandardCharsets.UTF_8) +
                "&openid.identity=" + URLEncoder.encode("http://specs.openid.net/auth/2.0/identifier_select", StandardCharsets.UTF_8) +
                "&openid.claimed_id=" + URLEncoder.encode("http://specs.openid.net/auth/2.0/identifier_select", StandardCharsets.UTF_8);

        response.sendRedirect(url);
    }

    @GetMapping("/verify")
    public void verify(@RequestParam Map<String, String> query, HttpServletResponse response, HttpServletRequest request) throws Exception {
        String claimedId = query.get("openid.claimed_id");

        if (claimedId == null || claimedId.isEmpty()) {
            response.sendRedirect(REALM + "/?error=missing_claimed_id");
            return;
        }

        String steamId = extractSteamId(claimedId);
        String sessionToken = hash(steamId);

        // 🧠 Fetch Steam profile info
        Map<String, String> profile = fetchSteamProfile(steamId);

        // 🔐 Save to session
        request.getSession().setAttribute("steamId", steamId);
        request.getSession().setAttribute("avatarUrl", profile.get("avatarUrl"));
        request.getSession().setAttribute("personaName", profile.get("personaName"));

        Cookie authCookie = new Cookie("auth", sessionToken);
        authCookie.setHttpOnly(true);
        authCookie.setSecure(false);
        authCookie.setPath("/");
        authCookie.setMaxAge(60 * 60 * 24 * 7);
        response.addCookie(authCookie);

        String state = request.getParameter("state");
        if (state == null || state.isBlank()) state = FRONT_END;
        response.sendRedirect(state);
    }

    private Map<String, String> fetchSteamProfile(String steamId) throws IOException {
        String apiKey = secretManagerService.getSteamApiKey();
        String url = "https://api.steampowered.com/ISteamUser/GetPlayerSummaries/v2/?key=" +
                apiKey + "&steamids=" + steamId;

        try (InputStream is = new URL(url).openStream();
             Scanner scanner = new Scanner(is)) {
            String json = scanner.useDelimiter("\\A").next();

            // 👇 crude but works, you can replace with Jackson if needed
            String avatarUrl = json.split("\"avatarfull\":\"")[1].split("\"")[0];
            String personaName = json.split("\"personaname\":\"")[1].split("\"")[0];

            Map<String, String> result = new HashMap<>();
            result.put("avatarUrl", avatarUrl);
            result.put("personaName", personaName);
            return result;
        }
    }

    private String extractSteamId(String claimedId) {
        return claimedId.replace("https://steamcommunity.com/openid/id/", "");
    }

    private String hash(String input) {
        // Use a strong hash like HMAC-SHA256 with secret key
        return DigestUtils.sha256Hex("secret_prefix" + input);
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status(@CookieValue(value = "auth", required = false) String authCookie, HttpServletRequest request) {
        Map<String, Object> status = new HashMap<>();
        if (authCookie != null && isValidHash(authCookie)) {
            status.put("loggedIn", true);
            status.put("steamId", request.getSession().getAttribute("steamId"));
            status.put("avatarUrl", request.getSession().getAttribute("avatarUrl"));
            status.put("personaName", request.getSession().getAttribute("personaName"));
        } else {
            status.put("loggedIn", false);
        }
        return ResponseEntity.ok(status);
    }

    @GetMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        // Delete the auth cookie
        Cookie cookie = new Cookie("auth", null);
        cookie.setMaxAge(0);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        response.addCookie(cookie);

        // Invalidate session
        request.getSession().invalidate();

        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    private boolean isValidHash(String cookie) {
        // validate hash (check against DB, or recreate and compare)
        return true;
    }
}
