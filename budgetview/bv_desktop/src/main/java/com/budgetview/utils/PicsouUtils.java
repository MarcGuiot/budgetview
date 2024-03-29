package com.budgetview.utils;

public class PicsouUtils {

  public static String splitCardNumber(String id) {
    if ((id == null) || (id.length() % 4 != 0)) {
      return id;
    }
    StringBuilder result = new StringBuilder();
    int maxLength = id.length() - 4;
    for (int i = 0; i <= maxLength; i += 4) {
      if (i > 0) {
        result.append('-');
      }
      result.append(id.substring(i, i + 4));
    }
    return result.toString();
  }
}
