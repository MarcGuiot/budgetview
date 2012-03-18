package org.designup.picsou.gui.components;

import org.designup.picsou.functests.checkers.MonthChooserChecker;
import org.designup.picsou.gui.components.dialogs.MonthChooserDialog;
import org.designup.picsou.gui.startup.components.OpenRequestManager;
import org.designup.picsou.gui.time.TimeService;
import org.globsframework.gui.GuiTestCase;
import org.globsframework.utils.Dates;
import org.uispec4j.Trigger;

import javax.swing.*;
import java.util.Arrays;

public class MonthChooserTest extends GuiTestCase {
  private int selectedMonth;
  private Boolean ok = Boolean.FALSE;

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
    checkMonthIs(200501);
  }

  public void checkMonthIs(int expectedMonthId) throws InterruptedException {
    synchronized (this) {
      if (!ok) {
        wait(1000);
      }
    }
    assertEquals(expectedMonthId, selectedMonth);
  }

  public void testEnable() throws Exception {
    final MonthChooserDialog monthChooser = new MonthChooserDialog(new JFrame(), directory);
    MonthChooserChecker month = MonthChooserChecker.open(new Trigger() {
      public void run() throws Exception {
        selectedMonth = monthChooser.show(200805, MonthRangeBound.LOWER, 200705);
      }
    });
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
    MonthChooserChecker month = MonthChooserChecker.open(new Trigger() {
      public void run() throws Exception {
        selectedMonth = monthChooser.show(200805, 200705, 200809, Arrays.asList(200707, 200804, 200805));
      }
    });
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

  public void testWithoutSelectedMonth() throws Exception {
    final MonthChooserDialog monthChooser = new MonthChooserDialog(new JFrame(), directory);
    MonthChooserChecker month = MonthChooserChecker.open(new Trigger() {
      public void run() throws Exception {
        selectedMonth = monthChooser.show(200800, MonthRangeBound.NONE, 200801);
      }
    });
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
    checkMonthIs(-1);
  }

  private MonthChooserChecker createChooser() {
    final MonthChooserDialog monthChooser = new MonthChooserDialog(new JFrame(), directory);
    return MonthChooserChecker.open(new Trigger() {
      public void run() throws Exception {
        selectedMonth = monthChooser.show(200805, MonthRangeBound.NONE, 200805);
        synchronized (this) {
          ok = true;
          notify();
        }
      }
    });
  }
}
