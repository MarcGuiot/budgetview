package com.budgetview.server.cloud.utils;

import java.security.SecureRandom;

public class RandomStrings {

  private SecureRandom random = new SecureRandom();

  static final String AB = "0123456789abcdefghijklmnopqrstuvwxyz";

  public String next(int length) {
    StringBuilder builder = new StringBuilder(length);
    for (int i = 0; i < length; i++) {
      builder.append(AB.charAt(random.nextInt(AB.length())));
    }
    return builder.toString();
  }
}
