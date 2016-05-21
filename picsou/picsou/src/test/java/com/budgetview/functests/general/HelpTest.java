package com.budgetview.functests.general;

import com.budgetview.functests.utils.LoggedInFunctionalTestCase;
import com.budgetview.gui.startup.components.AppLogger;
import com.budgetview.utils.Lang;

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