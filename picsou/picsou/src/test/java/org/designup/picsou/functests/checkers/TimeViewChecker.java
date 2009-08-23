package org.designup.picsou.functests.checkers;

import junit.framework.Assert;
import org.designup.picsou.gui.time.TimeViewPanel;
import org.designup.picsou.gui.time.selectable.Selectable;
import org.designup.picsou.model.Month;
import org.globsframework.model.GlobList;
import org.globsframework.utils.TestUtils;
import org.uispec4j.Panel;
import org.uispec4j.assertion.Assertion;
import org.uispec4j.assertion.UISpecAssert;

import java.util.*;

public class TimeViewChecker extends GuiChecker {
  protected TimeViewPanel timeViewPanel;

  public TimeViewChecker(Panel panel) {
    Panel table = panel.getPanel("timeView");
    timeViewPanel = (TimeViewPanel)table.getAwtComponent();
  }

  public void assertEmpty() {
    Set<Selectable> selectables = timeViewPanel.getCurrentlySelectedToUpdate();
    Assert.assertTrue("Contains: " + selectables, selectables.isEmpty());
  }

  public void checkDisplays(String... months) {
    long end = System.currentTimeMillis() + 1000;
    GlobList list = new GlobList();
    timeViewPanel.getAllSelectableMonth(list);
    while (list.size() != months.length && System.currentTimeMillis() < end) {
      try {
        Thread.sleep(50);
      }
      catch (InterruptedException e) {
      }
      list.clear();
      timeViewPanel.getAllSelectableMonth(list);
    }
    List<Integer> ids = new ArrayList<Integer>();
    for (String month : months) {
      ids.add(parseMonthId(month));
    }
    Set<Integer> valueSet = list.getValueSet(Month.ID);
    TestUtils.assertSetEquals(ids, valueSet);
  }

  public void checkSelection(final String... yyyymm) {
    UISpecAssert.assertThat(new Assertion() {
      public void check() throws Exception {
        Set<Selectable> list = timeViewPanel.getCurrentlySelectedToUpdate();
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
    timeViewPanel.selectMonthByIndex(index);
  }

  public void selectMonth(String yyyymm) {
    selectMonths(yyyymm);
    checkSelection(yyyymm);
  }

  public void selectMonths(String... yyyymm) {
    Set<Integer> monthIds = new HashSet<Integer>();
    for (String date : yyyymm) {
      monthIds.add(parseMonthId(date));
    }
    timeViewPanel.selectMonths(monthIds);
  }

  public void selectLast() {
    timeViewPanel.selectLastMonth();
  }

  public void selectNone() {
    timeViewPanel.selectMonths(Collections.<Integer>emptySet());
  }

  public void selectAll() {
    timeViewPanel.selectAll();
  }

  public void checkSpanEquals(String fromYyyyMm, String toYyyyMm) {
    long end = System.currentTimeMillis() + 1000;
    GlobList list = new GlobList();
    timeViewPanel.getAllSelectableMonth(list);
    while (list.size() < 2 && System.currentTimeMillis() < end) {
      try {
        Thread.sleep(50);
      }
      catch (InterruptedException e) {
      }
      list.clear();
      timeViewPanel.getAllSelectableMonth(list);
    }
    Assert.assertTrue(list.size() >= 2);
    Assert.assertEquals(fromYyyyMm, Month.toString(list.get(0).get(Month.ID)));
    Assert.assertEquals(toYyyyMm, Month.toString(list.get(list.size() - 1).get(Month.ID)));
  }

  public TimeViewChecker checkMonthTooltip(String monthId, double balance, double position) {
    timeViewPanel.getMouseOverHandler().enterMonth(parseMonthId(monthId));
    String tooltip = timeViewPanel.getToolTipText();
    Assert.assertTrue("Expected balance: " + balance + " - " + tooltip,
                      tooltip.contains("Balance: " + toString(balance, true)));
    Assert.assertTrue("Expected position: " + position + " - " + tooltip,
                      tooltip.contains("Position: " + toString(position)));
    return this;
  }

  public TimeViewChecker checkMonthTooltip(String monthId, String expectedTooltip) {
    timeViewPanel.getMouseOverHandler().enterMonth(parseMonthId(monthId));
    Assert.assertEquals(expectedTooltip, timeViewPanel.getToolTipText());
    return this;
  }

  public TimeViewChecker checkYearTooltip(int year, String expectedTooltip) {
    timeViewPanel.getMouseOverHandler().enterYear(year);
    Assert.assertEquals(expectedTooltip, timeViewPanel.getToolTipText());
    return this;
  }
}
