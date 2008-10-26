package org.designup.picsou.gui.help;

import org.designup.picsou.functests.checkers.HelpChecker;
import org.designup.picsou.gui.PicsouGuiTestCase;
import org.globsframework.gui.splits.ImageLocator;
import org.globsframework.gui.splits.TextLocator;
import org.globsframework.gui.splits.utils.DummyImageLocator;
import org.globsframework.gui.splits.utils.DummyTextLocator;
import org.globsframework.utils.exceptions.ItemNotFound;
import org.uispec4j.Trigger;

import javax.swing.*;

public class HelpDialogTest extends PicsouGuiTestCase {
  private HelpChecker checker;

  protected void setUp() throws Exception {
    super.setUp();
    directory.add(new JFrame());
    directory.add(ImageLocator.class, new DummyImageLocator());
    directory.add(TextLocator.class, new DummyTextLocator());
  }

  private void init(final String ref) {
    final HelpService helpService = new HelpService(repository, directory);
    helpService.setSource(new DummyHelpSource());
    directory.add(helpService);

    checker = HelpChecker.open(new Trigger() {
      public void run() throws Exception {
        helpService.show(ref, directory.get(JFrame.class));
      }
    });
  }

  public void test() throws Exception {
    init("page1");

    checker.checkTitle("page1Title");
    checker.checkContains("page1 content - <a href=\"help:page2\">page2</a>");

    checker.clickLink("page2");
    checker.checkTitle("page2Title");
    checker.checkContains("page2 content - <a href=\"help:page1\">page1</a>");
  }

  public void testBackForward() throws Exception {
    init("page1");

    checker.checkNavigation(false, false);

    checker.clickLink("page2");
    checker.checkNavigation(true, false);

    checker.back();
    checker.checkTitle("page1Title");
    checker.checkNavigation(false, true);

    checker.forward();
    checker.checkTitle("page2Title");
    checker.checkNavigation(true, false);

    checker.clickLink("page1");
    checker.checkTitle("page1Title");
    checker.back();
    checker.checkTitle("page2Title");
    checker.back();
    checker.checkTitle("page1Title");
    checker.checkNavigation(false, true);

    checker.clickLink("page2");
    checker.checkNavigation(true, false);
  }

  public void testHome() throws Exception {
    init("page1");
    checker.checkHomeEnabled(true);

    checker.home();
    checker.checkTitle("indexTitle");
    checker.checkNavigation(true, false);
    checker.checkHomeEnabled(false);

    checker.clickLink("page2");
    checker.checkTitle("page2Title");
    checker.checkNavigation(true, false);
    checker.checkHomeEnabled(true);

    checker.back();
    checker.checkTitle("indexTitle");
    checker.checkNavigation(true, true);
    checker.checkHomeEnabled(false);

    checker.clickLink("page2");
    checker.checkTitle("page2Title");
    checker.checkNavigation(true, false);
    checker.checkHomeEnabled(true);

    checker.home();
    checker.checkTitle("indexTitle");
    checker.checkNavigation(true, false);
    checker.checkHomeEnabled(false);
  }

  private static class DummyHelpSource implements HelpSource {

    public String getTitle(String ref) {
      return ref + "Title";
    }

    public String getContent(String ref) {
      if (ref.equals("index")) {
        return "home - <a href='help:page1'>page1</a> - <a href='help:page2'>page2</a>";
      }
      if (ref.equals("page1")) {
        return "page1 content - <a href='help:page2'>page2</a>";
      }
      if (ref.equals("page2")) {
        return "page2 content - <a href='help:page1'>page1</a>";
      }
      throw new ItemNotFound(ref);
    }
  }
}
