package org.designup.picsou.utils;

import org.globsframework.gui.splits.TextLocator;
import org.globsframework.utils.exceptions.ItemNotFound;
import org.globsframework.utils.exceptions.ResourceAccessFailed;
import org.globsframework.utils.Files;

import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.io.InputStream;

public class Lang {
  private static ResourceBundle bundle;
  private static Locale LOCALE = Locale.ENGLISH;

  public static TextLocator TEXT_LOCATOR = new TextLocator() {
    public String get(String code) {
      return Lang.get(code);
    }
  };

  private Lang() {
  }

  public static void setLocale(Locale locale) {
    LOCALE = locale;
    bundle = null;
  }

  public static String get(String key, Object... arguments) throws ItemNotFound {
    if (arguments.length == 0) {
      return getMessage(key);
    }
    return getFormat(key).format(arguments);
  }

  public static String getWithDefault(String key, String defaultKey, Object... arguments) throws ItemNotFound {
    if (find(key) != null){
      return get(key, arguments);
    }
    return get(defaultKey, arguments);
  }

  public static MessageFormat getFormat(String key) {
    String message = getMessage(key);
    MessageFormat formatter = new MessageFormat(message.replace("'", "''"));
    formatter.setLocale(LOCALE);
    return formatter;
  }

  public static String getHelpFile(String fileName) {
    return getFile("help", fileName);
  }

  public static String getDocFile(String fileName) {
    return getFile("docs", fileName);
  }

  private static String getFile(String dir, String fileName) {
    String filePath = "/" + dir + "/" + LOCALE.getLanguage() + "/" + fileName;
    InputStream stream = Lang.class.getResourceAsStream(filePath);
    if (stream == null) {
      throw new ResourceAccessFailed("File " + filePath);
    }
    return Files.loadStreamToString(stream, "UTF-8");
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
