package com.budgetview.shared.utils;

public class MobileConstants {

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

  public static final String[] CYPHER_SUITES = {
    "TLS_DHE_DSS_WITH_AES_256_CBC_SHA",
    "TLS_DHE_DSS_WITH_AES_128_CBC_SHA",
    "TLS_DHE_RSA_WITH_AES_128_CBC_SHA",
    "TLS_DHE_RSA_WITH_AES_256_CBC_SHA",
    "TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA",
    "TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA",
    "TLS_ECDH_RSA_WITH_AES_128_CBC_SHA",
    "TLS_ECDH_RSA_WITH_AES_256_CBC_SHA",
    "TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA",
    "TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256",
    "TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA",
    "TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384",
    "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA",
    "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA",
    "TLS_RSA_WITH_AES_128_CBC_SHA",
    "TLS_RSA_WITH_AES_256_CBC_SHA",
  };

  public static final String SALT = "d48(cWqH";
}
