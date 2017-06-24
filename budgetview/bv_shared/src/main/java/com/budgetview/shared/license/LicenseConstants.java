package com.budgetview.shared.license;

import org.globsframework.utils.Strings;
import org.globsframework.utils.Utils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

public class LicenseConstants {

  public static final String PROD_SERVER_URL = "https://register.mybudgetview.fr:443";
  public static final String FTP_SERVER_URL = "ftp://ftpjar.mybudgetview.fr";

  public static final String APPNAME = "budgetview";
  public static final String FTP_URL_PROPERTY = "bv.license.ftp.url";
  public static final String LICENSE_URL_PROPERTY = "bv.license.url";

  public static final String REQUEST_FOR_REGISTER = "/register";
  public static final String NEW_USER = "/newUser";
  public static final String REQUEST_FOR_CONFIG = "/requestForConfig";
  public static final String REQUEST_FOR_MAIL = "/mailTo";
  public static final String REQUEST_SEND_MAIL = "/sendMailToUs";
  public static final String SERVER_STATUS = "/server-status";
  public static final String SEND_USE_INFO = "/sendUseInfo";

  public static final String HEADER_MAIL_FROM = "mail";
  public static final String CODING = "coding";
  public static final String HEADER_MAIL_TO = "toMail";
  public static final String HEADER_MAIL_TITLE = "title";
  public static final String HEADER_MAIL_CONTENT = "content";
  public static final String HEADER_SIGNATURE = "signature";
  public static final String HEADER_IS_VALID = "isValide";
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
  public static final String HEADER_BAD_ADRESS = "badAdress";
  public static final String SOME_PASSWORD = "HdsB 8(Rfm";
  public static final String SUPPORT_EMAIL = "support";
  public static final String ADMIN_EMAIL = "admin";

  public static int RETRY_PERIOD = 10000;

  static {
    Utils.beginRemove();
    LicenseConstants.RETRY_PERIOD = 500;
    Utils.endRemove();
  }

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

  public static boolean isServerUrlSet() {
    return Strings.isNotEmpty(System.getProperty(LICENSE_URL_PROPERTY));
  }

  public static String getServerUrl(String path) {
    String url = PROD_SERVER_URL + path;
    Utils.beginRemove();
    url = System.getProperty(LICENSE_URL_PROPERTY, PROD_SERVER_URL) + path;
    Utils.endRemove();
    return url;
  }

  public static String getFtpServerUrl() {
    String url = FTP_SERVER_URL;
    Utils.beginRemove();
    url = System.getProperty(FTP_URL_PROPERTY, FTP_SERVER_URL);
    Utils.endRemove();
    return url;
  }
}
