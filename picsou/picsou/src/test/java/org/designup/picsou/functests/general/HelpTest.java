package org.designup.picsou.functests.general;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.gui.startup.components.AppLogger;
import org.designup.picsou.utils.Lang;
import org.globsframework.utils.Dates;
import org.globsframework.utils.Log;
import org.globsframework.utils.TestUtils;

import java.io.File;
import java.io.PrintStream;
import java.util.Date;

public class HelpTest extends LoggedInFunctionalTestCase {

  public void testHelpMenu() throws Exception {
    operations.checkGotoSupport(Lang.get("site.support.url"));
  }

  public void testDefaultLinks() throws Exception {
    operations.checkGotoSupport("http://www.mybudgetview.com/support");
  }

  public void testSendLogs() throws Exception {

    String text = new Date().toString();

    PrintStream stream = new PrintStream(AppLogger.getLogFile());
    stream.append("Date: " + text);
    stream.close();
    
    operations.openSendLogs()
      .checkTitle("Send logs")
      .checkMessageContains("support@mybudgetview.com")
      .checkDetailsContain(text)
      .checkCopy()
      .close();
  }
}