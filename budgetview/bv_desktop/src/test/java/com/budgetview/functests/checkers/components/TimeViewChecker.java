package com.budgetview.functests.checkers.components;

import com.budgetview.functests.checkers.GuiChecker;
import com.budgetview.desktop.time.TimeViewPanel;
import com.budgetview.desktop.time.selectable.Selectable;
import com.budgetview.model.Month;
import com.budgetview.shared.utils.AmountFormat;
import junit.framework.Assert;
import org.globsframework.model.GlobList;
import org.globsframework.utils.TestUtils;
import org.uispec4j.Panel;
import org.uispec4j.assertion.Assertion;
import org.uispec4j.assertion.UISpecAssert;

import javax.swing.*;
import java.util.*;

public class TimeViewChecker extends GuiChecker {
  private TimeViewPanel timeViewPanel;
  private Panel mainWindow;

  public TimeViewChecker(Panel mainWindow) {
    this.mainWindow = mainWindow;
  }

  public void assertEmpty() {
    Set<Selectable> selectables = getTimeViewPanel().getCurrentlySelectedToUpdate();
    Assert.assertTrue("Contains: " + selectables, selectables.isEmpty());
  }

  public void checkDisplays(String... months) {
    long end = System.currentTimeMillis() + 1000;
    GlobList list = new GlobList();
    getTimeViewPanel().getAllSelectableMonth(list);
    while (list.size() != months.length && System.currentTimeMillis() < end) {
      try {
        Thread.sleep(50);
      }
      catch (InterruptedException e) {
      }
      list.clear();
      getTimeViewPanel().getAllSelectableMonth(list);
    }
    List<Integer> expectedIds = new ArrayList<Integer>();
    for (String month : months) {
      expectedIds.add(parseMonthId(month));
    }
    Set<Integer> actualIds = list.getValueSet(Month.ID);
    TestUtils.assertSetEquals(actualIds, expectedIds);
  }

  public void checkSelection(final String... yyyymm) {
    UISpecAssert.assertThat(new Assertion() {
      public void check() {
        Set<Selectable> list = getTimeViewPanel().getCurrentlySelectedToUpdate();
        GlobList selectedMonths = new GlobList();
        for (Selectable selectable : list) {
          selectable.getSelectedGlobs(selectedMonths);
        }

        if (yyyymm.length != selectedMonths.size()) {
          Assert.assertEquals(Arrays.toString(yyyymm), selectedMonths.getValueSet(Month.ID).toString());
        }

        for (int i = 0; i < yyyymm.length; i++) {
          Assert.assertEquals(yyyymm[i], Month.toString(selectedMonths.get(i).get(Month.ID)));
        }
      }
    });
  }

  /**
   * @deprecated
   */
  public void selectCell(int index) {
    getTimeViewPanel().selectMonthByIndex(index);
  }

  public void selectMonth(final int monthId) throws Exception {
    SwingUtilities.invokeAndWait(new Runnable() {
      public void run() {
        getTimeViewPanel().selectMonths(Collections.singleton(monthId));
      }
    });
  }

  public void selectMonth(String yyyymm) throws Exception {
    selectMonths(yyyymm);
    checkSelection(yyyymm);
  }

  public void selectMonths(final int... monthIds) throws Exception {
    final Set<Integer> monthSet = new HashSet<Integer>();
    for (int monthId : monthIds) {
      monthSet.add(monthId);
    }
    SwingUtilities.invokeAndWait(new Runnable() {
      public void run() {
        getTimeViewPanel().selectMonths(monthSet);
      }
    });
  }

  public void selectMonths(String... yyyymm) throws Exception {
    final Set<Integer> monthIds = new HashSet<Integer>();
    for (String date : yyyymm) {
      monthIds.add(parseMonthId(date));
    }
    SwingUtilities.invokeAndWait(new Runnable() {
      public void run() {
        getTimeViewPanel().selectMonths(monthIds);
      }
    });
  }

  public void selectLast() throws Exception {
    SwingUtilities.invokeAndWait(new Runnable() {
      public void run() {
        getTimeViewPanel().selectLastMonth();
      }
    });
  }

  public void selectNone() throws Exception {
    SwingUtilities.invokeAndWait(new Runnable() {
      public void run() {
        getTimeViewPanel().selectMonths(Collections.<Integer>emptySet());
      }
    });
  }

  public void selectAll() throws Exception {
    SwingUtilities.invokeAndWait(new Runnable() {
      public void run() {
        getTimeViewPanel().selectAll();
      }
    });
  }

  public void checkSpanEquals(String fromYyyyMm, String toYyyyMm) {
    long end = System.currentTimeMillis() + 1000;
    GlobList list = new GlobList();
    getTimeViewPanel().getAllSelectableMonth(list);
    while (list.size() < 2 && System.currentTimeMillis() < end) {
      try {
        Thread.sleep(50);
      }
      catch (InterruptedException e) {
      }
      list.clear();
      getTimeViewPanel().getAllSelectableMonth(list);
    }
    Assert.assertTrue(list.size() >= 2);
    Assert.assertEquals(fromYyyyMm, Month.toString(list.get(0).get(Month.ID)));
    Assert.assertEquals(toYyyyMm, Month.toString(list.get(list.size() - 1).get(Month.ID)));
  }

  public TimeViewChecker checkMonthTooltip(String monthId, double position) {
    getTimeViewPanel().getMouseOverHandler().enterMonth(parseMonthId(monthId));
    String tooltip = getTimeViewPanel().getToolTipText();
    if (!tooltip.contains("Min position: " + AmountFormat.toStandardValueString(position))) {
      Assert.fail("Expected position: " + position + " - " + tooltip +
                  "\nbut was: " + tooltip);
    }
    return this;
  }

  public TimeViewChecker checkMonthTooltip(String monthId, String expectedTooltip) {
    getTimeViewPanel().getMouseOverHandler().enterMonth(parseMonthId(monthId));
    Assert.assertEquals(expectedTooltip, getTimeViewPanel().getToolTipText());
    return this;
  }

  public TimeViewChecker checkYearTooltip(int year, String expectedTooltip) {
    getTimeViewPanel().getMouseOverHandler().enterYear(year);
    Assert.assertEquals(expectedTooltip, getTimeViewPanel().getToolTipText());
    return this;
  }

  public TimeViewPanel getTimeViewPanel() {
    if (timeViewPanel == null) {
      Panel table = mainWindow.getPanel("timeView");
      setTimeViewPanel((TimeViewPanel) table.getAwtComponent());
    }
    return timeViewPanel;
  }

  public void setTimeViewPanel(TimeViewPanel timeViewPanel) {
    this.timeViewPanel = timeViewPanel;
  }
}
