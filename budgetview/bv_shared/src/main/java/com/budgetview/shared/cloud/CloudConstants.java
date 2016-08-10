package com.budgetview.shared.cloud;

import org.globsframework.utils.Utils;

public class CloudConstants {
  public static final String EMAIL = "email";
  public static final String BUDGEA_TOKEN = "budgea_token";
  public static final String BUDGEA_USER_ID = "budgea_user_id";

  public static final String PROD_CLOUD_SERVER_URL = "https://register.mybudgetview.fr:443";
  public static final String LOCAL_SERVER_URL = "http://127.0.0.1:8080";

  public static final String APPNAME = "budgetview";
  public static final String CLOUD_URL_PROPERTY = APPNAME + ".cloud.url";

  public static String getServerUrl(String path) {
    String url = PROD_CLOUD_SERVER_URL;
    Utils.beginRemove();
    url = System.getProperty(CLOUD_URL_PROPERTY, LOCAL_SERVER_URL) + path;
    Utils.endRemove();
    return url;
  }
}
