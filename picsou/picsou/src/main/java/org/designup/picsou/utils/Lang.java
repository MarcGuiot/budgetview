package org.designup.picsou.utils;

import org.crossbowlabs.globs.utils.exceptions.ItemNotFound;
import org.crossbowlabs.splits.TextLocator;

import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Lang {
  private static ResourceBundle bundle;
  private static final Locale LOCALE = Locale.FRANCE;

  public static TextLocator TEXT_LOCATOR = new TextLocator() {
    public String get(String code) {
      return Lang.get(code);
    }
  };

  private Lang() {
  }

  public static String get(String key, Object... arguments) throws ItemNotFound {
    if (arguments.length == 0) {
      return getMessage(key);
    }
    return getFormat(key).format(arguments);
  }

  public static MessageFormat getFormat(String key) {
    String message = getMessage(key);
    MessageFormat formatter = new MessageFormat(message);
    formatter.setLocale(LOCALE);
    return formatter;
  }

  private static String getMessage(String key) {
    String message = find(key);
    if (message == null) {
      throw new ItemNotFound("Key '" + key + "' not found in language file: " + LOCALE);
    }
      return message;
  }

  public static String find(String key) {
    if (bundle == null) {
      bundle = ResourceBundle.getBundle("i18n/lang", LOCALE);
    }
    try {
      return bundle.getString(key);
    }
    catch (MissingResourceException e) {
      return null;
    }
  }

  public static Enumeration<String> getKeys() {
    if (bundle == null) {
      bundle = ResourceBundle.getBundle("i18n/lang", LOCALE);
    }
    try {
      return bundle.getKeys();
    }
    catch (MissingResourceException e) {
      return null;
    }
  }
}
