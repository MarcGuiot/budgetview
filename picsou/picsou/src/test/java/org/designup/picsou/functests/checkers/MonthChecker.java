package org.designup.picsou.functests.checkers;

import junit.framework.Assert;
import org.designup.picsou.gui.time.TimeViewPanel;
import org.designup.picsou.gui.time.selectable.Selectable;
import org.designup.picsou.model.Month;
import org.globsframework.model.GlobList;
import org.globsframework.utils.Dates;
import org.globsframework.utils.TestUtils;
import org.uispec4j.Panel;
import org.uispec4j.assertion.Assertion;
import org.uispec4j.assertion.UISpecAssert;

import java.util.*;

public class MonthChecker extends DataChecker {
  protected TimeViewPanel timeViewPanel;

  public MonthChecker(Panel panel) {
    Panel table = panel.getPanel("month");
    timeViewPanel = (TimeViewPanel)table.getAwtComponent();
  }

  public void assertEmpty() {
    Assert.assertTrue(timeViewPanel.getCurrentlySelectedToUpdate().isEmpty());
  }

  public void assertDisplays(String... elements) {
    long end = System.currentTimeMillis() + 1000;
    GlobList list = new GlobList();
    timeViewPanel.getAllSelectableMonth(list);
    while (list.size() != elements.length && System.currentTimeMillis() < end) {
      try {
        Thread.sleep(50);
      }
      catch (InterruptedException e) {
      }
      list.clear();
      timeViewPanel.getAllSelectableMonth(list);
    }
    List<Integer> ids = new ArrayList<Integer>();
    for (String element : elements) {
      int index = element.indexOf(" (");
      if (index == -1) {
        index = element.length();
      }
      ids.add(Month.getMonthId(Dates.parseMonth(element.substring(0, index))));
    }
    Set<Integer> valueSet = list.getValueSet(Month.ID);
    TestUtils.assertSetEquals(ids, valueSet);
  }

  public void checkSelection(final String... dates) {
    UISpecAssert.assertThat(new Assertion() {
      public void check() throws Exception {
        Set<Selectable> list = timeViewPanel.getCurrentlySelectedToUpdate();
        GlobList selectedMonths = new GlobList();
        for (Selectable selectable : list) {
          selectable.getSelectedGlobs(selectedMonths);
        }

        if (dates.length != selectedMonths.size()) {
          Assert.assertEquals(Arrays.toString(dates), selectedMonths.getValueSet(Month.ID).toString());
        }

        for (int i = 0; i < dates.length; i++) {
          Assert.assertEquals(dates[i], Month.toString(selectedMonths.get(i).get(Month.ID)));
        }
      }
    });
  }

  /**
   * @deprecated
   */
  public void selectCell(int index) {
    timeViewPanel.selectMonth(index);
  }

  public void selectMonth(String yyyymm) {
    selectMonths(yyyymm);
  }

  public void selectMonths(String... yyyymm) {
    Set<Integer> monthIds = new HashSet<Integer>();
    for (String date : yyyymm) {
      monthIds.add(Month.getMonthId(Dates.parseMonth(date)));
    }
    timeViewPanel.selectMonth(monthIds);
  }

  public void selectLast() {
    timeViewPanel.selectLastMonth();
  }

  public void selectNone() {
    timeViewPanel.selectMonth(Collections.<Integer>emptySet());
  }

  public void selectAll() {
    timeViewPanel.selectAll();
  }

  public void assertSpanEquals(String fromYyyyMm, String toYyyyMm) {
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
}
