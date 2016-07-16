package com.budgetview.http;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

public class HttpBudgetViewConstants {
  public static final String LICENSE_SERVER_URL = "https://•register.mybudgetview.fr:443";
  public static final String MOBILE_SERVER_URL = "http://register.mybudgetview.fr:8080";
  public static final String FTP_SERVER_URL = "ftp://ftpjar.mybudgetview.fr";
  public static final String HEADER_MAIL = "mail";
  public static final String REQUEST_FOR_REGISTER = "/register";
  public static final String REQUEST_FOR_CONFIG = "/requestForConfig";
  public static final String REQUEST_FOR_MAIL = "/mailTo";
  public static final String REQUEST_SEND_MAIL = "/sendMailToUs";
  public static final String REQUEST_CLIENT_TO_SERVER_DATA = "/sendMobileData";
  public static final String CODING = "coding";
  public static final String HEADER_TO_MAIL = "toMail";
  public static final String HEADER_MAIL_TITLE = "title";
  public static final String HEADER_MAIL_CONTENT = "content";
  public static final String HEADER_SIGNATURE = "signature";
  public static final String HEADER_IS_VALIDE = "isValide";
  public static final String HEADER_CODE = "code";
  public static final String HEADER_COUNT = "count";
  public static final String HEADER_MAIL_SENT = "mailSent";
  public static final String HEADER_MAIL_SENT_FAILED = "mailSentFailed";
  public static final String HEADER_STATUS = "status";
  public static final String HEADER_MAIL_UNKNOWN = "mailUnknown";
  public static final String HEADER_ACTIVATION_CODE_NOT_VALIDE_MAIL_NOT_SENT = "activationCodeNotValideMailNotSent";
  public static final String HEADER_ACTIVATION_CODE_NOT_VALIDE_MAIL_SENT = "activationCodeNotValideMailSent";
  public static final String HEADER_APPLICATION_VERSION = "applicationVersion";
  public static final String HEADER_CONFIG_VERSION = "configVersion";
  public static final String HEADER_NEW_CONFIG_VERSION = "newConfigVersion";
  public static final String HEADER_JAR_VERSION = "jarVersion";
  public static final String HEADER_NEW_JAR_VERSION = "newJarVersion";
  public static final String HEADER_REPO_ID = "repoId";
  public static final String HEADER_USE_INFO = "use";
  public static final String HEADER_PENDING = "pending";
  public static final String SEND_USE_INFO = "/sendUseInfo";
  public static final String MOBILE_SALT = "d48(cWqH";
  public static final String HEADER_BAD_ADRESS = "badAdress";
  public static final String SOME_PASSWORD = "HdsB 8(Rfm";
  public static final String SUPPORT_EMAIL = "support";
  public static final String ADMIN_EMAIL = "admin";
  public static int RETRY_PERIOD = 10000;

  public static String encodeContent(String content) {
    try {
      return URLEncoder.encode(content, "utf-8");
    }
    catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  public static String decodeContent(String content) {
    try {
      return URLDecoder.decode(content, "utf-8");
    }
    catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }
}
