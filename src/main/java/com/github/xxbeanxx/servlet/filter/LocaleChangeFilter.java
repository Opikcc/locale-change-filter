package com.github.xxbeanxx.servlet.filter;

import java.io.IOException;
import java.util.Locale;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.xxbeanxx.servlet.filter.util.CookieUtils;
import com.github.xxbeanxx.servlet.filter.util.LocaleUtils;

/**
 * A servlet filter that overrides the request locale.
 *
 * @author Greg Baker
 */
public class LocaleChangeFilter implements Filter {

	/**
	 * Default language cookie name.
	 */
	public static final String DEFAULT_COOKIE_NAME = "locale";

	/**
	 * Default locale querystring parameter, if none is specified in filter's InitParams
	 */
	public static final String DEFAULT_LOCALE_PARAM = "locale";
	
	/**
	 * InitParam key that specifies what querystring parameter will trigger the locale switch.
	 */
	public static final String LOCALE_PARAM_NAME_KEY = "localeParamName";
	
	private static final Logger LOGGER = LoggerFactory.getLogger(LocaleChangeFilter.class);

	private String localeParamName = DEFAULT_LOCALE_PARAM;
	
	private String cookieName = DEFAULT_COOKIE_NAME;
	
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		final String paramNameInitParameter = filterConfig.getInitParameter(LOCALE_PARAM_NAME_KEY);
		if (paramNameInitParameter != null) {
			this.localeParamName = paramNameInitParameter;
		}
	}

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
	    final HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
	    final HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;

	    final Locale acceptHeaderLocale = findAcceptHeaderLocale(httpServletRequest);
	    final Locale cookieLocale = findCookieLocale(httpServletRequest);
		final Locale querystringLocale = findQuerystringLocale(httpServletRequest);
		final Locale systemLocale = findSystemLocale();
		final Locale locale = determineOverridingLocale(acceptHeaderLocale, cookieLocale, querystringLocale, systemLocale);
		
		bakeLocaleCookie(httpServletRequest, httpServletResponse, locale);
		filterChain.doFilter(new LocaleOverridingRequest(httpServletRequest, locale), servletResponse);
	}

	@Override
	public void destroy() {
		/* intentionally left blank */
	}

	protected void bakeLocaleCookie(HttpServletRequest request, HttpServletResponse response, Locale locale) {
		Cookie cookie = CookieUtils.getCookie(request, cookieName);
		
		if (cookie == null) {
			// TODO nulls
			cookie = CookieUtils.createCookie(cookieName, locale.toString(), null, null);
		}
		else {
			cookie.setValue(locale.toString());
		}
		
		// TODO nulls
		CookieUtils.addCookie(response, cookie, null, null);
	}

	protected Locale determineOverridingLocale(Locale acceptHeaderLocale, Locale cookieLocale, Locale querystringLocale, Locale systemLocale) {
		if (querystringLocale != null) {
			return querystringLocale;
		}
		else if (cookieLocale != null) {
			return cookieLocale;
		}
		else if (acceptHeaderLocale != null) {
			return acceptHeaderLocale;
		}
		else {
			return systemLocale;
		}
	}

	protected Locale findAcceptHeaderLocale(HttpServletRequest request) {
		return request.getLocale();
	}

	protected Locale findCookieLocale(HttpServletRequest httpServletRequest) {
		final Cookie cookie = CookieUtils.getCookie(httpServletRequest, cookieName);
		
		if (cookie != null) {
			final String value = cookie.getValue();
			return stringToLocale(value);
		}
		
		return null;
	}

	protected Locale findQuerystringLocale(HttpServletRequest request) {
		final String localParam = request.getParameter(localeParamName);
		return stringToLocale(localParam);
	}

	protected Locale findSystemLocale() {
		return Locale.getDefault();
	}

	private Locale stringToLocale(String string) {
		if (string != null) {
			try {
				return LocaleUtils.toLocale(string);
			}
			catch (final IllegalArgumentException illegalArgumentException) {
				LOGGER.warn("Invalid locale string: {}; returning null", string);
			}
		}
		
		return null;
	}
}