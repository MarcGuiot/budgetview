package com.budgetview.shared.cloud;

import org.globsframework.utils.Utils;

public class BudgeaConstants {
  public static final String APPNAME = "budgetview";
  public static final String SERVER_URL_PROPERTY = APPNAME + ".budgea.url";

  public static final String PROD_SERVER_URL = "https://budgetview.biapi.pro/2.0";
  public static final String LOCAL_SERVER_URL = "http://127.0.0.1:8085";

  public static String getServerUrl(String path) {
    String url = PROD_SERVER_URL;
    Utils.beginRemove();
    url = System.getProperty(SERVER_URL_PROPERTY, LOCAL_SERVER_URL) + path;
    Utils.endRemove();
    return url;
  }
  public static void setProd() {
    System.setProperty(SERVER_URL_PROPERTY, PROD_SERVER_URL);
  }

  public static void setLocal() {
    System.setProperty(SERVER_URL_PROPERTY, LOCAL_SERVER_URL);
  }
}
