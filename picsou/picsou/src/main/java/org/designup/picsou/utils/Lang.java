package org.designup.picsou.utils;

import org.globsframework.gui.splits.TextLocator;
import org.globsframework.utils.Strings;
import org.globsframework.utils.exceptions.ItemNotFound;
import org.globsframework.utils.exceptions.ResourceAccessFailed;
import org.globsframework.utils.Files;

import java.text.MessageFormat;
import java.util.*;
import java.io.InputStream;

public class Lang {
  private static ResourceBundle bundle;

  public static final Locale ROOT = new Locale("");
  public static final Locale EN = Locale.ENGLISH;
  public static final Locale FR = Locale.FRENCH;
  private static Locale LOCALE = ROOT;

  public static TextLocator TEXT_LOCATOR = new TextLocator() {
    public String get(String code) {
      return Lang.get(code);
    }
  };

  private Lang() {
  }

  public static void setLocale(Locale locale) {
    Locale.setDefault(locale);
    LOCALE = locale;
    bundle = null;
  }

  public static Locale getLocale() {
    return LOCALE;
  }
  
  public static String getLang(){
    return LOCALE.getLanguage();
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
    return createMessageFormatFromText(message);
  }

  public static MessageFormat createMessageFormatFromText(String message) {
    MessageFormat formatter = new MessageFormat(message.replace("'", "''"));
    formatter.setLocale(LOCALE);
    return formatter;
  }

  public static String getHelpFile(String fileName) {
    return getFile("help", fileName);
  }

  public static String findHelpFile(String fileName) {
    return findFile("help", fileName);
  }

  public static String getDocFile(String fileName) {
    return getFile("docs", fileName);
  }

  private static String findFile(String dir, String fileName) {
    String filePath = getFilePath(dir, fileName);
    InputStream stream = Lang.class.getResourceAsStream(filePath);
    if (stream == null) {
      return null;
    }
    return Files.loadStreamToString(stream, "UTF-8");
  }

  private static String getFile(String dir, String fileName) {
    String content = findFile(dir, fileName);
    if (content == null) {
      throw new ResourceAccessFailed("File " + getFilePath(dir, fileName));
    }
    return content;
  }

  private static String getFilePath(String dir, String fileName) {
    String langDir = LOCALE.getLanguage();
    if (Strings.isNullOrEmpty(langDir) || langDir.equals("__")) {
      langDir = "en";
    }
    return "/" + dir + "/" + langDir + "/" + fileName;
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

  public static void setLang(String lang) {
    if (lang.equals("fr")) {
      Locale.setDefault(Locale.FRANCE);
      setLocale(Locale.FRANCE);
    }
    else if (lang.equals("en")) {
      Locale.setDefault(Locale.ENGLISH);
      setLocale(Locale.ENGLISH);
    }
  }
}
