package com.budgetview.functests.general;

import com.budgetview.functests.utils.LoggedInFunctionalTestCase;
import com.budgetview.desktop.startup.components.AppLogger;
import com.budgetview.utils.Lang;
import org.junit.Test;

import java.io.PrintStream;
import java.util.Date;

public class HelpTest extends LoggedInFunctionalTestCase {

  @Test
  public void testHelpMenu() throws Exception {
    operations.checkGotoSupport(Lang.get("site.support.url"));
  }

  @Test
  public void testDefaultLinks() throws Exception {
    operations.checkGotoSupport("http://www.budgetview.fr/support");
  }

  @Test
  public void testSendLogs() throws Exception {

    String text = new Date().toString();

    PrintStream stream = new PrintStream(AppLogger.getLogFile());
    stream.append("Date: " + text);
    stream.close();

    operations.openSendLogs()
      .checkTitle("Send logs")
      .checkMessageContains("support@budgetview.fr")
      .checkDetailsContain(text)
      .checkCopy()
      .close();
  }
}