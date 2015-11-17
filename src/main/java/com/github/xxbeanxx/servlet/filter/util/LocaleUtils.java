package com.github.xxbeanxx.servlet.filter.util;

import java.util.Locale;

/**
 * The methods in this class were copied from the Apache commons-lang project.
 * 
 * @author Greg Baker
 */
public abstract class LocaleUtils {

	/**
	 * <p>Converts a String to a Locale.</p>
	 *
	 * <p>This method takes the string format of a locale and creates the
	 * locale object from it.</p>
	 *
	 * <pre>
	 *   LocaleUtils.toLocale("")           = new Locale("", "")
	 *   LocaleUtils.toLocale("en")         = new Locale("en", "")
	 *   LocaleUtils.toLocale("en_GB")      = new Locale("en", "GB")
	 *   LocaleUtils.toLocale("en_GB_xxx")  = new Locale("en", "GB", "xxx")   (#)
	 * </pre>
	 *
	 * <p>(#) The behaviour of the JDK variant constructor changed between JDK1.3 and JDK1.4.
	 * In JDK1.3, the constructor upper cases the variant, in JDK1.4, it doesn't.
	 * Thus, the result from getVariant() may vary depending on your JDK.</p>
	 *
	 * <p>This method validates the input strictly.
	 * The language code must be lowercase.
	 * The country code must be uppercase.
	 * The separator must be an underscore.
	 * The length must be correct.
	 * </p>
	 *
	 * @param str  the locale String to convert, null returns null
	 * @return a Locale, null if null input
	 * @throws IllegalArgumentException if the string is an invalid format
	 * @see Locale#forLanguageTag(String)
	 */
	public static Locale toLocale(String str) {
		if (str == null) {
			return null;
		}
		
		if (str.isEmpty()) { // LANG-941 - JDK 8 introduced an empty locale where all fields are blank
			return new Locale("", "");
		}
		
		if (str.contains("#")) { // LANG-879 - Cannot handle Java 7 script & extensions
			throw new IllegalArgumentException("Invalid locale format: " + str);
		}
		
		final int len = str.length();
		
		if (len < 2) {
			throw new IllegalArgumentException("Invalid locale format: " + str);
		}
		
		final char ch0 = str.charAt(0);
		
		if (ch0 == '_') {
			if (len < 3) {
				throw new IllegalArgumentException("Invalid locale format: " + str);
			}
			
			final char ch1 = str.charAt(1);
			final char ch2 = str.charAt(2);
			
			if (!Character.isUpperCase(ch1) || !Character.isUpperCase(ch2)) {
				throw new IllegalArgumentException("Invalid locale format: " + str);
			}
			
			if (len == 3) {
				return new Locale("", str.substring(1, 3));
			}
			
			if (len < 5) {
				throw new IllegalArgumentException("Invalid locale format: " + str);
			}
			
			if (str.charAt(3) != '_') {
				throw new IllegalArgumentException("Invalid locale format: " + str);
			}
			
			return new Locale("", str.substring(1, 3), str.substring(4));
		}

		final String[] split = str.split("_", -1);
		final int occurrences = split.length -1;
		
		switch (occurrences) {
			case 0:
				if (LocaleUtils.isAllLowerCase(str) && (len == 2 || len == 3)) {
					return new Locale(str);
				}
				
				throw new IllegalArgumentException("Invalid locale format: " + str);
	
			case 1:
				if (LocaleUtils.isAllLowerCase(split[0]) && (split[0].length() == 2 || split[0].length() == 3) && split[1].length() == 2 && LocaleUtils.isAllUpperCase(split[1])) {
					return new Locale(split[0], split[1]);
				}
				
				throw new IllegalArgumentException("Invalid locale format: " + str);
	
			case 2:
				if (LocaleUtils.isAllLowerCase(split[0]) && (split[0].length() == 2 || split[0].length() == 3) && (split[1].length() == 0 || (split[1].length() == 2 && LocaleUtils.isAllUpperCase(split[1]))) && split[2].length() > 0) {
					return new Locale(split[0], split[1], split[2]);
				}
	
				throw new IllegalArgumentException("Invalid locale format: " + str);

			default:
				throw new IllegalArgumentException("Invalid locale format: " + str);
		}
	}

	private static boolean isAllLowerCase(CharSequence cs) {
		if (cs == null || cs.length() == 0) {
			return false;
		}
		
		final int sz = cs.length();
		
		for (int i = 0; i < sz; i++) {
			if (Character.isLowerCase(cs.charAt(i)) == false) {
				return false;
			}
		}
		
		return true;
	}
	
	private static boolean isAllUpperCase(CharSequence cs) {
		if (cs == null || cs.length() == 0) {
			return false;
		}
		
		final int sz = cs.length();
		
		for (int i = 0; i < sz; i++) {
			if (Character.isUpperCase(cs.charAt(i)) == false) {
				return false;
			}
		}
		
		return true;
	}
}
