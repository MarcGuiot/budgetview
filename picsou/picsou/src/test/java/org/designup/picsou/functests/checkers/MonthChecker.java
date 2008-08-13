package org.designup.picsou.functests.checkers;

import junit.framework.Assert;
import org.designup.picsou.gui.time.TimeViewPanel;
import org.designup.picsou.gui.time.selectable.Selectable;
import org.designup.picsou.model.Month;
import org.globsframework.model.GlobList;
import org.globsframework.utils.Dates;
import org.globsframework.utils.TestUtils;
import org.uispec4j.Panel;

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

  public void assertEquals(String... elements) {
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

  public void assertCellSelected(int index) {
    Set<Selectable> list = timeViewPanel.getCurrentlySelectedToUpdate();
    Assert.assertEquals(1, list.size());
    Selectable selectable = list.iterator().next();
    GlobList globList = new GlobList();
    selectable.getSelectedGlobs(globList);
    Assert.assertEquals(1, globList.size());
    Integer currentyymm = globList.get(0).get(Month.ID);
    GlobList currentMonths = timeViewPanel.getRepository().getAll(Month.TYPE).sort(Month.ID);
    if (!currentyymm.equals(currentMonths.get(index).get(Month.ID))) {
      Assert.fail(Month.toString(currentMonths.get(index).get(Month.ID)) + " not selected\n" +
                  "Selection: " + currentMonths);
    }
  }

  public void assertCellSelected(String... dates) {
    Set<Selectable> list = timeViewPanel.getCurrentlySelectedToUpdate();
    GlobList selectedMonth = new GlobList();
    for (Selectable selectable : list) {
      selectable.getSelectedGlobs(selectedMonth);
    }

    for (int i = 0; i < dates.length; i++) {
      Assert.assertEquals(dates[i], Month.toString(selectedMonth.get(i).get(Month.ID)));
    }
  }

  public void selectCell(int index) {
    timeViewPanel.selectMonth(index);
  }

  public void selectCell(String date) {
    selectCells(date);
  }

  public void selectCells(String... dates) {
    Set<Integer> monthIds = new HashSet<Integer>();
    for (String date : dates) {
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

  public void assertSpanEquals(String fromMmYyyy, String toMmYyyy) {
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
    Assert.assertEquals(fromMmYyyy, Month.toString(list.get(0).get(Month.ID)));
    Assert.assertEquals(toMmYyyy, Month.toString(list.get(list.size() - 1).get(Month.ID)));
  }
}
