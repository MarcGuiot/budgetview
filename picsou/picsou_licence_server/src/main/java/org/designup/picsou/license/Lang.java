package org.designup.picsou.license;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

public class Lang {
  private static ResourceBundle bundle = ResourceBundle.getBundle("i18n/mail", Locale.FRENCH);

  public static String get(String key, String lang, String... arg) {
    String message = bundle.getString(key + "." + lang);
    if (message == null) {
      message = bundle.getString(key + "." + "en");
    }
    if (arg.length != 0) {
      MessageFormat formatter = new MessageFormat(message.replace("'", "''"));
      formatter.setLocale(Locale.FRENCH);
      return formatter.format(arg);
    }
    else {
      return message;
    }
  }
}
