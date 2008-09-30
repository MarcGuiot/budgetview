package org.designup.picsou.gui.help;

import org.designup.picsou.functests.checkers.HelpChecker;
import org.designup.picsou.gui.PicsouGuiTestCase;
import org.uispec4j.Trigger;
import org.globsframework.gui.splits.utils.DummyIconLocator;
import org.globsframework.gui.splits.IconLocator;

import javax.swing.*;

public class HelpDialogTest extends PicsouGuiTestCase {
  private HelpChecker checker;

  protected void setUp() throws Exception {
    super.setUp();
    directory.add(new JFrame());
    directory.add(IconLocator.class, new DummyIconLocator());

    final HelpDialog helpDialog = new HelpDialog(new DummyHelpSource(), repository, directory);
    checker = HelpChecker.open(new Trigger() {
      public void run() throws Exception {
        helpDialog.show("page1");
      }
    });
  }

  public void test() throws Exception {
    checker.checkTitle("page1Title");
    checker.checkContains("page1 content - <a href=\"page:page2\">page2</a>");

    checker.clickLink("page2");
    checker.checkTitle("page2Title");
    checker.checkContains("page2 content - <a href=\"page:page1\">page1</a>");
  }

  public void testBackForward() throws Exception {
    checker.checkBackEnabled(false);
    checker.checkForwardEnabled(false);

    checker.clickLink("page2");
    checker.checkBackEnabled(true);
    checker.checkForwardEnabled(false);

    checker.back();
    checker.checkTitle("page1Title");
    checker.checkBackEnabled(false);
    checker.checkForwardEnabled(true);

    checker.forward();
    checker.checkTitle("page2Title");
    checker.checkBackEnabled(true);
    checker.checkForwardEnabled(false);

    checker.clickLink("page1");
    checker.checkTitle("page1Title");
    checker.back();
    checker.checkTitle("page2Title");
    checker.back();
    checker.checkTitle("page1Title");
    checker.checkBackEnabled(false);
    checker.checkForwardEnabled(true);
    
    checker.clickLink("page2");
    checker.checkBackEnabled(true);
    checker.checkForwardEnabled(false);
  }

  private static class DummyHelpSource implements HelpSource {

    public String getTitle(String ref) {
      return ref + "Title";
    }

    public String getContent(String ref) {
      if (ref.equals("page1")) {
        return "page1 content - <a href='page:page2'>page2</a>";
      }
      if (ref.equals("page2")) {
        return "page2 content - <a href='page:page1'>page1</a>";
      }
      return ref + " content";
    }
  }
}
