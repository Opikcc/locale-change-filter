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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A servlet filter that overrides the request locale.
 *
 * @author Greg Baker
 */
public class LocaleChangeFilter implements Filter {

	private static final Logger LOGGER = LoggerFactory.getLogger(LocaleChangeFilter.class);

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		/* intentionally left blank*/
	}

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
	    final HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
	    final HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;

		final Locale locale = Locale.ENGLISH;
		final LocaleRequestWrapper localeRequestWrapper = new LocaleRequestWrapper(httpServletRequest, locale);

		filterChain.doFilter(localeRequestWrapper, servletResponse);
	}

	@Override
	public void destroy() {
		/* intentionally left blank */
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