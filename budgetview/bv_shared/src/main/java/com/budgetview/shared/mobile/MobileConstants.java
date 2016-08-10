package com.budgetview.shared.mobile;

import org.globsframework.utils.Utils;

public class MobileConstants {

  public static final String APPNAME = "budgetview";
  public static final String SERVER_URL_PROPERTY = APPNAME + ".mobile.url";
  public static final String PROD_SERVER_URL = "http://register.mybudgetview.fr:8080";

  public static final String MAJOR_VERSION_NAME = "MAJOR_VERSION";
  public static final String MINOR_VERSION_NAME = "MINOR_VERSION";

  public static final String CREATE_MOBILE_USER = "/createMobileUser";
  public static final String GET_MOBILE_DATA = "/getMobileData";
  public static final String POST_MOBILE_DATA = "/sendMobileData";
  public static final String SEND_MAIL_TO_CONFIRM_MOBILE = "/sendMailToCreateMobileUser";
  public static final String SEND_MAIL_REMINDER_FROM_MOBILE = "/sendMailFromMobile";
  public static final String DELETE_MOBILE_ACCOUNT = "/deleteMobileAccount";

  public static final String STATUS = "STATUS";
  public static final String CRYPTED_INFO = "info";
  public static final String MAIL = "mail";
  public static final String HEADER_LANG = "lang";

  public static final String SALT = "d48(cWqH";

  public static String getServerUrl(String path) {
    String url = PROD_SERVER_URL;
    Utils.beginRemove();
    url = System.getProperty(SERVER_URL_PROPERTY, PROD_SERVER_URL) + path;
    Utils.endRemove();
    return url;
  }
}
