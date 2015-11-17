package com.github.xxbeanxx.servlet.filter;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A servlet filter that overrides the request locale.
 *
 * @author Greg Baker
 */
public class LocaleChangeFilter implements Filter {

	public static final Locale[] DEFAULT_ALLOWED_LOCALES = new Locale[] { Locale.ENGLISH, Locale.FRENCH };
	public static final String DEFAULT_LOCALE_PARAM = "locale";
	public static final Locale DEFAULT_DEFAULT_LOCALE = Locale.ENGLISH;
	
	public static final String ALLOWED_LOCALES_KEY = "allowedLocales";
	public static final String LOCALE_PARAM_NAME_KEY = "localeParamName";
	public static final String DEFAULT_LOCALE_KEY = "defaultLocale";

	private static final String LOCALES_DELIMITER = ",; \t\n";

	private static final Logger LOGGER = LoggerFactory.getLogger(LocaleChangeFilter.class);
	
	private Locale[] allowedLocales = DEFAULT_ALLOWED_LOCALES;
	
	private Locale defaultLocale = DEFAULT_DEFAULT_LOCALE;
	
	private String localeParamName = DEFAULT_LOCALE_PARAM; 
	
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		final String paramNameInitParameter = filterConfig.getInitParameter(LOCALE_PARAM_NAME_KEY);
		if (paramNameInitParameter != null) {
			this.localeParamName = paramNameInitParameter;
		}

		final String defaultLocaleInitParameter = filterConfig.getInitParameter(DEFAULT_LOCALE_KEY);
		if (defaultLocaleInitParameter != null) {
			this.defaultLocale = LocaleUtils.toLocale(defaultLocaleInitParameter);
		}

		final String allowedLocalesInitParameter = filterConfig.getInitParameter(ALLOWED_LOCALES_KEY);
		if (allowedLocalesInitParameter != null) {
			final String[] localeStrings = StringUtils.split(allowedLocalesInitParameter, LOCALES_DELIMITER);
			this.allowedLocales = new Locale[localeStrings.length];
			for (int i = 0; i < localeStrings.length; i++) {
				this.allowedLocales[i] = LocaleUtils.toLocale(localeStrings[i]);
			}
		}
	}

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
	    final HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
	    final HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
	    
		final Locale requestLocale = findRequestLocale(httpServletRequest);
		final LocaleRequestWrapper localeRequestWrapper = new LocaleRequestWrapper(httpServletRequest, requestLocale);

		filterChain.doFilter(localeRequestWrapper, servletResponse);
	}

	@Override
	public void destroy() {
		/* intentionally left blank */
	}

	private Locale findRequestLocale(HttpServletRequest request) {
		final String localParam = request.getParameter(localeParamName);
		
		if (localParam != null) {
			try {
				return LocaleUtils.toLocale(localParam);
			}
			catch (final IllegalArgumentException illegalArgumentException) {
				LOGGER.warn("Invalid locale parameter: {}; returning default: {}", localParam, defaultLocale);
			}
		}
		
		return defaultLocale;
	}

	/**
	 * A request wrapper that facilitates locale overriding.
	 */
	private static class LocaleRequestWrapper extends HttpServletRequestWrapper {

		private final Locale locale;

		public LocaleRequestWrapper(HttpServletRequest request, Locale locale) {
			super(request);
			this.locale = locale;
		}

		@Override
		public Locale getLocale() {
			return locale != null ? locale : super.getLocale();
		}

		@Override
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public Enumeration getLocales() {
			if (locale == null) {
				return super.getLocales();
			}
			else {
				List<Locale> locales = Collections.list(super.getLocales());

				if (locales.contains(locale)) {
					locales.remove(locale);
				}

				locales.add(0, locale);
				return Collections.enumeration(locales);
			}
		}

	}

}