package com.budgetview.license;

import com.budgetview.client.ClientParams;
import com.budgetview.gui.PicsouApplication;

public class Client {

  public static void main(String[] args) throws Exception {
    System.setProperty(ClientParams.COM_APP_LICENSE_URL, "http://localhost:8443");
    System.setProperty(PicsouApplication.LOG_TO_SOUT, "true");
    System.setProperty(PicsouApplication.LOCAL_PREVAYLER_PATH_PROPERTY, "/tmp/budgetview");
    PicsouApplication.main("-l", "fr");
  }
}
