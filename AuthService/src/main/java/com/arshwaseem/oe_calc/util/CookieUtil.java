package com.arshwaseem.oe_calc.util;

import com.arshwaseem.oe_calc.configuration.CookieProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CookieUtil {

    private final CookieProperties cookieProperties;

    public static final String ACCESS_TOKEN_COOKIE = "accessToken";
    public static final String REFRESH_TOKEN_COOKIE = "refreshToken";

    public Cookie createAccessTokenCookie(String token) {
        Cookie cookie = new Cookie(ACCESS_TOKEN_COOKIE,token);
        cookie.setPath("/");
        cookie.setHttpOnly(cookieProperties.isHttpOnly());
        cookie.setSecure(cookieProperties.isSecure());
        cookie.setMaxAge(cookieProperties.getMaxAge());

        if(cookie.getDomain() != null){
            cookie.setDomain(cookie.getDomain());
        }

        return cookie;
    }

    public Cookie createRefreshTokenCookie(String token) {
        Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE,token);
        cookie.setPath("/auth/refresh");
        cookie.setHttpOnly(cookieProperties.isHttpOnly());
        cookie.setSecure(cookieProperties.isSecure());
        cookie.setMaxAge(cookieProperties.getRefreshInterval());

        if(cookie.getDomain() != null){
            cookie.setDomain(cookie.getDomain());
        }

        return cookie;
    }

    public void setAuthCookie(HttpServletResponse response, String accessToken, String refreshToken){
        setAccessTokenCookie(response, accessToken);
        setRefreshTokenCookie(response, refreshToken);
    }

    public void setAccessTokenCookie(HttpServletResponse response, String accessToken){
        Cookie cookie = createAccessTokenCookie(accessToken);

        String cookieHeader = String.format(
                "%s=%s; Path=%s; Max-Age=%d; HttpOnly=%s; SameSite=%s",
                cookie.getName(),
                cookie.getValue(),
                cookie.getPath(),
                cookie.getMaxAge(),
                cookieProperties.isSecure() ? "; Secure" : "",
                cookieProperties.getSameSite()
        );
        response.addHeader("Set-Cookie", cookieHeader);
    }

    public void setRefreshTokenCookie(HttpServletResponse response, String refreshToken){
        Cookie cookie = createRefreshTokenCookie(refreshToken);

        String cookieHeader = String.format(
                "%s=%s; Path=%s; Max-Age=%d; HttpOnly=%s; SameSite=%s",
                cookie.getName(),
                cookie.getValue(),
                cookie.getPath(),
                cookie.getMaxAge(),
                cookieProperties.isSecure() ? "; Secure" : "",
                cookieProperties.getSameSite()
        );
        response.addHeader("Set-Cookie", cookieHeader);
    }

    public void clearAuthCookie(HttpServletResponse response){
        Cookie accessCookie = new Cookie(ACCESS_TOKEN_COOKIE, null);
        accessCookie.setPath("/");
        accessCookie.setMaxAge(0);
        accessCookie.setHttpOnly(true);
        response.addCookie(accessCookie);


        Cookie refreshCookie = new Cookie(REFRESH_TOKEN_COOKIE, null);
        refreshCookie.setPath("/api/auth/refresh");
        refreshCookie.setMaxAge(0);
        refreshCookie.setHttpOnly(true);
        response.addCookie(refreshCookie);
    }


    public Optional<String> getCookieValue(HttpServletRequest request, String cookieName){
        if(request.getCookies() == null){
            return Optional.empty();
        }

        return Arrays.stream(request.getCookies())
                .filter(cookie -> cookieName.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst();
    }

    public Optional<String> getAccessTokenCookie(HttpServletRequest request){
        return getCookieValue(request, ACCESS_TOKEN_COOKIE);
    }

    public Optional<String> getRefreshTokenCookie(HttpServletRequest request){
        return getCookieValue(request, REFRESH_TOKEN_COOKIE);
    }

}
