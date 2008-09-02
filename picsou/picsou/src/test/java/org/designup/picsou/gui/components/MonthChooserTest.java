package org.designup.picsou.gui.components;

import org.designup.picsou.functests.checkers.MonthChooserChecker;
import org.globsframework.gui.GuiTestCase;
import org.uispec4j.interception.WindowInterceptor;
import org.uispec4j.Trigger;
import org.uispec4j.Window;

import javax.swing.*;

public class MonthChooserTest extends GuiTestCase {
  private int selectedMonth;

  public void testStandart() throws Exception {
    MonthChooserChecker month = createChooser();
    month.checkVisibleYears(2007, 2008, 2009);
    month.checkSelectedInCurrentMonth(5);
    month.nextYear();
    month.checkVisibleYears(2008, 2009, 2010);
    month.checkSelectedInPreviousMonth(5);
    month.previousYear();
    month.checkVisibleYears(2007, 2008, 2009);
    month.previousYear();
    month.previousYear();
    month.checkNoneSelected();
    month.nextYear();
    month.selectMonthInPrevious(12);
  }

  public void testMovePage() throws Exception {
    MonthChooserChecker month = createChooser();
    month.checkVisibleYears(2007, 2008, 2009);
    month.checkSelectedInCurrentMonth(5);
    month.nextPage();
    month.checkVisibleYears(20010, 20011, 2012);
    month.checkNoneSelected();
    month.previousPage();
    month.checkVisibleYears(2007, 2008, 2009);
    month.previousPage();
    month.checkVisibleYears(2004, 2005, 2006);
    month.selectMonthInCurrent(1);
    assertEquals(200501, selectedMonth);
  }

  public void testCancel() throws Exception {
    MonthChooserChecker month = createChooser();
    month.cancel();
    assertEquals(-1, selectedMonth);
  }

  private MonthChooserChecker createChooser() {
    final MonthChooser monthChooser = new MonthChooser(directory);
    Window window = WindowInterceptor.getModalDialog(new Trigger() {
      public void run() throws Exception {
        selectedMonth = monthChooser.show(new JFrame(), 2008, 5);
      }
    });
    return new MonthChooserChecker(window);
  }
}
