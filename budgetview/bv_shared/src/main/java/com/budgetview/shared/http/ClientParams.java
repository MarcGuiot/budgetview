package com.budgetview.shared.http;

import com.budgetview.shared.http.HttpBudgetViewConstants;
import org.globsframework.utils.Strings;
import org.globsframework.utils.Utils;

public class ClientParams {

  public static final String APPNAME = "budgetview";
  public static final String COM_APP_LICENSE_URL = APPNAME + ".license.url";
  public static final String COM_APP_MOBILE_URL = APPNAME + ".mobile.url";
  public static final String COM_APP_FTP_URL = APPNAME + ".license.ftp.url";

  public static String FTP_SERVER_URL = HttpBudgetViewConstants.FTP_SERVER_URL;

  static {
    Utils.beginRemove();
    HttpBudgetViewConstants.RETRY_PERIOD = 500;
    Utils.endRemove();
  }

  public static boolean isLicenseServerUrlSet() {
    return Strings.isNotEmpty(System.getProperty(COM_APP_LICENSE_URL));
  }

  public static String getLicenseServerUrl(String path) {
    String url = HttpBudgetViewConstants.LICENSE_SERVER_URL;
    Utils.beginRemove();
    url = System.getProperty(COM_APP_LICENSE_URL, HttpBudgetViewConstants.LICENSE_SERVER_URL) + path;
    Utils.endRemove();
    return url;
  }

  public static String getMobileServerUrl(String path) {
    String url = HttpBudgetViewConstants.MOBILE_SERVER_URL;
    Utils.beginRemove();
    url = System.getProperty(COM_APP_MOBILE_URL, HttpBudgetViewConstants.MOBILE_SERVER_URL) + path;
    Utils.endRemove();
    return url;
  }

  public static String getFtpServerUrl() {
    String url = HttpBudgetViewConstants.FTP_SERVER_URL;
    Utils.beginRemove();
    url = System.getProperty(COM_APP_FTP_URL, HttpBudgetViewConstants.FTP_SERVER_URL);
    Utils.endRemove();
    return url;
  }
}
