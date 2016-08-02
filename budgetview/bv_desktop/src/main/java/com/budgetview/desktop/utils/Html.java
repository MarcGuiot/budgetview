package com.budgetview.desktop.utils;

public class Html {
  public static final String EURO_SYMBOL = "&#8364;";

  private Html() {
  }

  public static String getEuroAmount(String amount) {
    return amount + EURO_SYMBOL;
  }
}
