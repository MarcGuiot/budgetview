package com.budgetview.server.cloud.utils;

import org.globsframework.utils.Utils;

public class WebsiteUrls {

  public static final String PROD_CLOUD_SERVER_URL = "http://www.mybudgetview.fr";
  public static final String WEBSITE_URL_PROPERTY = "budgetview.website.url";
  public static final String LOCAL_SERVER_URL = "http://127.0.0.1:8081";

  public static String emailSent() {
    return url("/boutique/email-envoye");
  }

  public static String subscriptionCreated() {
    return url("/boutique/abonnement-reussi");
  }

  public static String cardUpdated() {
    return url("/boutique/nouvelle-carte");
  }

  public static String invalidCode() {
    return url("/boutique/code-invalide");
  }

  public static String codeTimeout() {
    return url("/boutique/code-expire");
  }

  public static String error() {
    return url("/boutique/erreur");
  }

  private static String url(String path) {
    String url = PROD_CLOUD_SERVER_URL + path;
    Utils.beginRemove();
    url = System.getProperty(WEBSITE_URL_PROPERTY, PROD_CLOUD_SERVER_URL) + path;
    Utils.endRemove();
    return url;
  }
}
