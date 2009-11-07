package org.designup.picsou.license;

import org.designup.picsou.gui.PicsouApplication;
import org.designup.picsou.gui.config.ConfigService;

public class Client {

  public static void main(String[] args) throws Exception {
    System.setProperty(ConfigService.COM_APP_LICENSE_URL, "http://localhost:8443");
    System.setProperty(PicsouApplication.LOG_SOUT, "true");
    System.setProperty(PicsouApplication.LOCAL_PREVAYLER_PATH_PROPERTY, "/tmp/cashpilot");
    PicsouApplication.main("-l", "fr");
  }
}
