package org.designup.picsou.licence;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

public class Lang {
  static ResourceBundle bundle = ResourceBundle.getBundle("i18n/mail", Locale.FRENCH);


  public static String get(String key, String lang, String... arg) {
    String message = bundle.getString(key + "." + lang);
    if (arg.length != 0) {
      MessageFormat formatter = new MessageFormat(message);
      formatter.setLocale(Locale.FRENCH);
      return formatter.format(arg);
    }
    else {
      return message;
    }
  }
}
