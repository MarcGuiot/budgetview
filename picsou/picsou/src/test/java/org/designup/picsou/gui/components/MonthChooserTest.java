package org.designup.picsou.gui.components;

import org.designup.picsou.functests.checkers.MonthChooserChecker;
import org.globsframework.gui.GuiTestCase;
import org.uispec4j.Trigger;
import org.uispec4j.Window;
import org.uispec4j.interception.WindowInterceptor;

import javax.swing.*;

public class MonthChooserTest extends GuiTestCase {
  private int selectedMonth;

  public void testStandard() throws Exception {
    MonthChooserChecker month = createChooser();
    month.checkVisibleYears(2007, 2008, 2009)
      .checkSelectedInCurrentMonth(5)
      .nextYear()
      .checkVisibleYears(2008, 2009, 2010)
      .checkSelectedInPreviousMonth(5)
      .previousYear()
      .checkVisibleYears(2007, 2008, 2009)
      .previousYear()
      .previousYear()
      .checkNoneSelected()
      .nextYear()
      .selectMonthInPrevious(12);
  }

  public void testMovePage() throws Exception {
    MonthChooserChecker month = createChooser();
    month.checkVisibleYears(2007, 2008, 2009)
      .checkSelectedInCurrentMonth(5)
      .nextPage()
      .checkVisibleYears(2010, 2011, 2012)
      .checkNoneSelected()
      .previousPage()
      .checkVisibleYears(2007, 2008, 2009)
      .previousPage()
      .checkVisibleYears(2004, 2005, 2006)
      .selectMonthInCurrent(1);
    assertEquals(200501, selectedMonth);
  }

  public void testEnable() throws Exception {
    final MonthChooser monthChooser = new MonthChooser(directory);
    Window window = WindowInterceptor.getModalDialog(new Trigger() {
      public void run() throws Exception {
        selectedMonth = monthChooser.show(new JFrame(), 200805, -1, 200705);
      }
    });
    MonthChooserChecker month = new MonthChooserChecker(window);
    month.checkVisibleYears(2005, 2006, 2007)
      .checkEnabled(4)
      .checkEnabled(5)
      .nextYear()
      .checkEnabled(4)
      .checkEnabled(5)
      .checkDisabled(6);
  }

  public void testWihoutSelectedMonth() throws Exception {
    final MonthChooser monthChooser = new MonthChooser(directory);
    Window window = WindowInterceptor.getModalDialog(new Trigger() {
      public void run() throws Exception {
        selectedMonth = monthChooser.show(new JFrame(), 200800, 0, 200801);
      }
    });
    MonthChooserChecker month = new MonthChooserChecker(window);
    month.checkNoneSelected();
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
        selectedMonth = monthChooser.show(new JFrame(), 200805, 0, 200805);
      }
    });
    return new MonthChooserChecker(window);
  }
}
