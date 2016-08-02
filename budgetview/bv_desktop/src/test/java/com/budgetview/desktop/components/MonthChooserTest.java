package com.budgetview.desktop.components;

import com.budgetview.desktop.components.MonthRangeBound;
import com.budgetview.functests.checkers.MonthChooserChecker;
import com.budgetview.desktop.components.dialogs.MonthChooserDialog;
import com.budgetview.desktop.startup.components.OpenRequestManager;
import com.budgetview.desktop.time.TimeService;
import org.globsframework.gui.GuiTestCase;
import org.globsframework.gui.splits.layout.LayoutService;
import org.globsframework.gui.splits.parameters.ConfiguredPropertiesService;
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
    directory.add(new LayoutService());
    directory.add(new ConfiguredPropertiesService());
    TimeService.setCurrentDate(Dates.parseMonth("2008/06"));
  }

  public void testStandard() throws Exception {
    MonthChooserChecker chooser = createChooser(false);
    chooser.checkNoneHidden();
    chooser.checkVisibleYears(2007, 2008, 2009)
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

  public void testNone() throws Exception {
    MonthChooserChecker chooser = createChooser(true);
    chooser.checkNoneShown();
    chooser.checkVisibleYears(2007, 2008, 2009)
      .checkSelectedInCurrentMonth(5)
      .selectNone();
    assertEquals(-100, selectedMonth);
  }

  public void testMovePage() throws Exception {
    MonthChooserChecker month = createChooser(false);
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

  public void testEnableDisable() throws Exception {
    final MonthChooserDialog monthChooser = new MonthChooserDialog(new JFrame(), directory);
    MonthChooserChecker month = MonthChooserChecker.open(new Trigger() {
      public void run() throws Exception {
        monthChooser.show(200805, MonthRangeBound.LOWER, 200705, new MonthChooserDialog.Callback() {
          public void processSelection(int monthId) {
            selectedMonth = monthId;
          }
        });
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

  public void testEnableMultipleMonths() throws Exception {
    final MonthChooserDialog monthChooser = new MonthChooserDialog(new JFrame(), directory);
    MonthChooserChecker month = MonthChooserChecker.open(new Trigger() {
      public void run() throws Exception {
        monthChooser.show(200805, 200705, 200809, Arrays.asList(200707, 200804, 200805), new MonthChooserDialog.Callback() {
          public void processSelection(int monthId) {
            selectedMonth = monthId;
          }
        });
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
        monthChooser.show(200800, MonthRangeBound.NONE, 200801, new MonthChooserDialog.Callback() {
          public void processSelection(int monthId) {
            selectedMonth = monthId;
          }
        });
      }
    });
    month.checkNoneSelected();
  }

  public void testGotoCenter() throws Exception {
    MonthChooserChecker month = createChooser(false);
    month.nextPage().nextPage().nextPage()
      .gotoCenter()
      .checkVisibleYears(2007, 2008, 2009);
  }

  public void testCancel() throws Exception {
    MonthChooserChecker month = createChooser(false);
    month.cancel();
    checkMonthIs(-5);
  }

  private MonthChooserChecker createChooser(boolean showNone) {
    final MonthChooserDialog monthChooser = new MonthChooserDialog("Choose month", new JFrame(), directory);
    if (showNone) {
      monthChooser.setNoneOptionShown(true);
    }
    return MonthChooserChecker.open(new Trigger() {
      public void run() throws Exception {
        monthChooser.show(200805, MonthRangeBound.NONE, 200805, new MonthChooserDialog.Callback() {
          public void processSelection(int monthId) {
            selectedMonth = monthId;
          }

          public void processNoneSelected() {
            selectedMonth = -100;
          }

          public void processCancel() {
            selectedMonth = -5;
          }
        });
        synchronized (this) {
          ok = true;
          notify();
        }
      }
    });
  }
}
