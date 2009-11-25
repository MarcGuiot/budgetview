package org.designup.picsou.gui.components;

import org.designup.picsou.functests.checkers.MonthChooserChecker;
import org.designup.picsou.gui.TimeService;
import org.designup.picsou.gui.components.dialogs.MonthChooserDialog;
import org.designup.picsou.gui.startup.OpenRequestManager;
import org.globsframework.gui.GuiTestCase;
import org.globsframework.utils.Dates;
import org.uispec4j.Trigger;
import org.uispec4j.Window;
import org.uispec4j.interception.WindowInterceptor;

import javax.swing.*;
import java.util.Arrays;

public class MonthChooserTest extends GuiTestCase {
  private int selectedMonth;

  protected void setUp() throws Exception {
    super.setUp();
    directory.add(new TimeService());
    directory.add(OpenRequestManager.class, new OpenRequestManager());
    TimeService.setCurrentDate(Dates.parseMonth("2008/06"));
  }

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
    final MonthChooserDialog monthChooser = new MonthChooserDialog(new JFrame(), directory);
    Window window = WindowInterceptor.getModalDialog(new Trigger() {
      public void run() throws Exception {
        selectedMonth = monthChooser.show(200805, MonthRangeBound.LOWER, 200705);
      }
    });
    MonthChooserChecker month = new MonthChooserChecker(window);
    month.checkVisibleYears(2005, 2006, 2007)
      .checkEnabledInCurrentYear(4)
      .checkEnabledInCurrentYear(5)
      .nextYear()
      .checkEnabledInCurrentYear(4)
      .checkEnabledInCurrentYear(5)
      .checkDisabledInCurrentYear(6);
  }

  public void testEnableMultipleMonth() throws Exception {
    final MonthChooserDialog monthChooser = new MonthChooserDialog(new JFrame(), directory);
    Window window = WindowInterceptor.getModalDialog(new Trigger() {
      public void run() throws Exception {
        selectedMonth = monthChooser.show(200805, 200705, 200809, Arrays.asList(200707, 200804, 200805));
      }
    });
    MonthChooserChecker month = new MonthChooserChecker(window);
    month.checkVisibleYears(2007, 2008, 2009)
      .checkIsDisabled(200707, 200804)
      .checkEnabledInCurrentYear(5)
      .checkDisabledInCurrentYear(4)
      .checkEnabledInCurrentYear(3)
      .checkEnabledInCurrentYear(7)
      .checkDisabledInCurrentYear(10)
      .previousYear()
      .checkDisabledInCurrentYear(7)
      .checkEnabledInCurrentYear(5)
      .checkDisabledInCurrentYear(3);
  }

  public void testWihoutSelectedMonth() throws Exception {
    final MonthChooserDialog monthChooser = new MonthChooserDialog(new JFrame(), directory);
    Window window = WindowInterceptor.getModalDialog(new Trigger() {
      public void run() throws Exception {
        selectedMonth = monthChooser.show(200800, MonthRangeBound.NONE, 200801);
      }
    });
    MonthChooserChecker month = new MonthChooserChecker(window);
    month.checkNoneSelected();
  }

  public void testGotoCenter() throws Exception {
    MonthChooserChecker month = createChooser();
    month.nextPage().nextPage().nextPage()
      .gotoCenter()
      .checkVisibleYears(2007, 2008, 2009);
  }

  public void testCancel() throws Exception {
    MonthChooserChecker month = createChooser();
    month.cancel();
    assertEquals(-1, selectedMonth);
  }

  private MonthChooserChecker createChooser() {
    final MonthChooserDialog monthChooser = new MonthChooserDialog(new JFrame(), directory);
    Window window = WindowInterceptor.getModalDialog(new Trigger() {
      public void run() throws Exception {
        selectedMonth = monthChooser.show(200805, MonthRangeBound.NONE, 200805);
      }
    });
    return new MonthChooserChecker(window);
  }
}
