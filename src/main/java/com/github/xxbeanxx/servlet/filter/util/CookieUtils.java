package com.github.xxbeanxx.servlet.filter.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Greg Baker
 */
public abstract class CookieUtils {

	public static final String DEFAULT_COOKIE_PATH = "/";
	
	public static void addCookie(HttpServletResponse response, Cookie cookie, Integer maxAge, Boolean secure) {
		if (maxAge != null) {
			cookie.setMaxAge(maxAge);
		}
		
		if (secure != null) {
			cookie.setSecure(secure);
		}
		
		response.addCookie(cookie);
	}	
	
	public static Cookie createCookie(String name, String value, String domain, String path) {
		final Cookie cookie = new Cookie(name, value);
		
		if (domain != null) {
			cookie.setDomain(domain);
		}
		
		if (path == null) {
			cookie.setPath(CookieUtils.DEFAULT_COOKIE_PATH);
		}
		else {
			cookie.setPath(path);
		}
		
		return cookie;		
	}
	
	public static Cookie getCookie(HttpServletRequest request, String name) {
		final Cookie cookies[] = request.getCookies();
		
		if (cookies != null) {
			for (final Cookie cookie : cookies) {
				if (name.equals(cookie.getName())) {
					return cookie;
				}
			}
		}
		
		return null;
	}

}
