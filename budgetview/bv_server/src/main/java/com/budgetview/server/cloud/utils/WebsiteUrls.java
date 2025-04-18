package com.budgetview.server.cloud.utils;

import org.globsframework.utils.Strings;

public class WebsiteUrls {

  public static final String PROD_CLOUD_SERVER_URL = "https://www.budgetview.fr";
  public static final String LOCAL_SERVER_URL = "http://127.0.0.1:8081";
  public static final String WEBSITE_URL_PROPERTY = "budgetview.website.url";

  public static String emailSent() {
    return url("/boutique/email-envoye");
  }

  public static String subscriptionCreated() {
    return url("/boutique/abonnement-reussi");
  }

  public static String subscriptionLinkAlreadyUsed() {
    return url("/boutique/lien-deja-utilise");
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
    String host = System.getProperty(WEBSITE_URL_PROPERTY, PROD_CLOUD_SERVER_URL);
    if (Strings.isNullOrEmpty(host)) {
      host = PROD_CLOUD_SERVER_URL;
    }
    return host + path;
  }
}
