package org.designup.picsou.license;

import org.designup.picsou.gui.PicsouApplication;

public class Client {

  public static void main(String[] args) throws Exception {
    System.setProperty(PicsouApplication.REGISTER_URL, "http://localhost:8443");
    PicsouApplication.main();
  }
}
