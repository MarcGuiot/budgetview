package com.budgetview.server.utils;

import org.globsframework.utils.Strings;

public class Args {
  public static String toEmail(String[] args, int index) {
    if (args.length <= index || !Strings.looksLikeAnEmail(args[index])) {
      return null;
    }
    return args[index];
  }

  public static Integer toInt(String[] args, int index) {
    if (args.length <= index) {
      return null;
    }
    String value = args[index];
    try {
      return Integer.parseInt(value);
    }
    catch (NumberFormatException e) {
      return null;
    }
  }

  public static String toString(String[] args, int index) {
    if (args.length <= index) {
      return null;
    }
    return args[index];
  }
}
