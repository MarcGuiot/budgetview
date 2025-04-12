package com.budgetview.shared.mobile;

import org.globsframework.utils.Utils;

public class MobileConstants {

  public static final String APPNAME = "budgetview";
  public static final String SERVER_URL_PROPERTY = APPNAME + ".mobile.url";

  private static final String PROD_SERVER_URL = "https://register.budgetview.fr";

  public static final String MAJOR_VERSION_NAME = "MAJOR_VERSION";
  public static final String MINOR_VERSION_NAME = "MINOR_VERSION";

  public static final String CREATE_MOBILE_USER = "/createMobileUser";
  public static final String GET_MOBILE_DATA = "/getMobileData";
  public static final String POST_MOBILE_DATA = "/sendMobileData";
  public static final String SEND_MAIL_TO_CONFIRM_MOBILE = "/sendMailToCreateMobileUser";
  public static final String SEND_MAIL_REMINDER_FROM_MOBILE = "/sendMailFromMobile";
  public static final String DELETE_MOBILE_ACCOUNT = "/deleteMobileAccount";
  public static final String SERVER_STATUS = "/server-status";

  public static final String WWW_MYBUDGETVIEW_FR = "https://www.budgetview.fr";
  public static final String WWW_MYBUDGETVIEW_COM = "http://www.budgetview.fr";

  public static final String STATUS = "STATUS";
  public static final String CRYPTED_INFO = "info";
  public static final String MAIL = "mail";
  public static final String HEADER_LANG = "lang";

  public static final String SALT = "d48(cWqH";

  private static Integer port = 1443;

  public static String getServerUrl(String path) {
    return getBaseUrl() + ":" + port + path;
  }

  private static String getBaseUrl() {
    String url = PROD_SERVER_URL;
    Utils.beginRemove();
    url = System.getProperty(SERVER_URL_PROPERTY, PROD_SERVER_URL);
    Utils.endRemove();
    return url;
  }

  public static String createUserUrl() {
    return getServerUrl(CREATE_MOBILE_USER);
  }

  public static void setPort(Integer port) {
    MobileConstants.port = port;
  }
}
