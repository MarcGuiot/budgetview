package com.budgetview.server.license;

import com.budgetview.desktop.Application;
import com.budgetview.shared.license.LicenseConstants;

public class Client {

  public static void main(String[] args) throws Exception {
    System.setProperty(LicenseConstants.LICENSE_URL_PROPERTY, "http://localhost:8443");
    System.setProperty(Application.LOG_TO_SOUT, "true");
    System.setProperty(Application.LOCAL_PREVAYLER_PATH_PROPERTY, "/tmp/budgetview");
    Application.main("-l", "fr");
  }
}
