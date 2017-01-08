package com.budgetview.server.utils;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

public class Lang {
  private static Map<Locale, ResourceBundle> bundles = new HashMap<Locale, ResourceBundle>();

  static Locale getLocale(String lang) {
    if ("en".equals(lang)) {
      return Locale.ENGLISH;
    }
    if ("fr".equals(lang)) {
      return Locale.FRENCH;
    }
    return Locale.ENGLISH;
  }

  public static String get(String key, String lang, String... arg) {
    Locale locale = getLocale(lang);
    ResourceBundle resourceBundle = getResource(locale);
    String message = resourceBundle.getString(key);
    if (arg.length != 0) {
      MessageFormat formatter = new MessageFormat(message.replace("'", "''"));
      formatter.setLocale(locale);
      return formatter.format(arg);
    }
    else {
      return message;
    }
  }

  private synchronized static ResourceBundle getResource(Locale locale) {
    ResourceBundle resourceBundle = bundles.get(locale);
    if (resourceBundle == null) {
      resourceBundle = ResourceBundle.getBundle("i18n/mail", locale);
      bundles.put(locale, resourceBundle);
    }
    return resourceBundle;
  }
}
