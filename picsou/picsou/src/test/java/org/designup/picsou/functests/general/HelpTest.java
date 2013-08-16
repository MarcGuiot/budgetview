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
    operations.openHelp().checkTitle("Index").close();

    operations.checkGotoSupport(Lang.get("site.support.url"));
  }

  public void testHelpForCards() throws Exception {
    views.selectHome();
    operations.openHelp("Dashboard View").checkTitle("Dashboard View").close();

    views.selectCategorization();
    operations.openHelp("Categorization View").checkTitle("Categorization View").close();

    views.selectBudget();
    operations.openHelp("Budget View").checkTitle("Budget View").close();

    views.selectData();
    operations.openHelp("Operations View").checkTitle("Operations View").close();
  }

  public void testDefaultLinks() throws Exception {
    operations.openHelp()
      .checkBottomTextLink("support site", "http://www.mybudgetview.com/support")
      .checkSendContactLink("contact us")
      .close();
  }

  public void testSendLogs() throws Exception {

    String text = new Date().toString();

    Log.init(new PrintStream(AppLogger.getLogFile()));
    Log.write("Date: " + text);
    
    operations.openSendLogs()
      .checkTitle("Send logs")
      .checkMessageContains("support@mybudgetview.com")
      .checkDetailsContain(text)
      .checkCopy()
      .close();
    
    Log.init(System.out);
  }
}