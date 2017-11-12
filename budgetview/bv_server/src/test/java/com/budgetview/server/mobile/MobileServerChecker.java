package com.budgetview.server.mobile;

import com.budgetview.server.license.LicenseServer;
import com.budgetview.shared.mobile.MobileConstants;
import org.globsframework.utils.Files;
import org.globsframework.utils.TestUtils;

import javax.servlet.http.HttpServlet;
import java.io.File;

import static junit.framework.TestCase.assertTrue;

public class MobileServerChecker {
  private MobileServer server;
  private boolean started;

  public static final File MOBILE_DATA_DIR = new File("/var/tmp/bv_mobile/");

  public MobileServerChecker() throws Exception {
    System.setProperty(MobileServer.MOBILE_PATH_PROPERTY, MOBILE_DATA_DIR.getAbsolutePath());
    System.setProperty(MobileConstants.SERVER_URL_PROPERTY, "http://localhost:8088");

    server = new MobileServer("budgetview/bv_server/server_admin/config/bv_license_test.properties");
  }

  public void init() throws Exception {
    Files.deleteSubtreeOnly(MOBILE_DATA_DIR);
    MOBILE_DATA_DIR.mkdir();
    server.init();
  }

  public void start() throws Exception {
    server.start();
    started = true;
  }

  public void stop() throws Exception {
    if (started) {
      server.stop();
    }
    started = false;
  }

  public void dispose() throws Exception {
    stop();
    server = null;
  }

  public void clearDataDirectory() {
    Files.deleteWithSubtree(MobileServerChecker.MOBILE_DATA_DIR);
  }

  public void checkData() throws Exception {
    TestUtils.retry(new Runnable() {
      public void run() {
        String[] list = MobileServerChecker.MOBILE_DATA_DIR.list();
        assertTrue(list != null && list.length == 1);
        list = new File(MobileServerChecker.MOBILE_DATA_DIR, list[0]).list();
        assertTrue(list != null && list.length == 2);
        assertTrue(list[0].startsWith("pending"));
        assertTrue(list[1].startsWith("pending"));
      }
    });
  }
}
