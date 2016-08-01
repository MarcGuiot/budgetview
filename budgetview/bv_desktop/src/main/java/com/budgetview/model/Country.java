package com.budgetview.model;

import com.budgetview.utils.Lang;

public enum Country {
  BELGIUM("be"),
  CANADA("ca"),
  SWITZERLAND("ch"),
  FRANCE("fr"),
  UNITED_STATES("us");

  private String code;

  Country(String code) {
    this.code = code;
  }

  public String getCode() {
    return code;
  }

  public String getLabel() {
    return Lang.get("country." + code);
  }

  public static String getLabelForAll() {
    return Lang.get("country.all");
  }

  public static Country get(String code) {
    for (Country country : values()) {
      if (country.code.equalsIgnoreCase(code)) {
        return country;
      }
    }
    return null;
  }
}
