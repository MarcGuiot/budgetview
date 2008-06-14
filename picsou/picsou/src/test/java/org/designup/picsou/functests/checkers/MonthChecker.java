package org.designup.picsou.functests.checkers;

import junit.framework.Assert;
import org.designup.picsou.gui.time.TimeViewPanel;
import org.designup.picsou.gui.time.selectable.Selectable;
import org.designup.picsou.model.Month;
import org.globsframework.model.GlobList;
import org.globsframework.utils.Dates;
import org.globsframework.utils.TestUtils;
import org.uispec4j.Panel;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MonthChecker extends DataChecker {
  protected TimeViewPanel timeViewPanel;

  public MonthChecker(Panel panel) {
    Panel table = panel.getPanel("month");
    timeViewPanel = (TimeViewPanel)table.getAwtComponent();
  }

  public void assertEmpty() {
    Assert.assertTrue(timeViewPanel.getCurrentlySelectedToUpdate().isEmpty());
  }

  public ContentChecker initContent() {
    return new ContentChecker();
  }

  public void assertContains(String... elements) {
    List<Integer> ids = new ArrayList<Integer>();
    for (String element : elements) {
      int index = element.indexOf(" (");
      if (index == -1) {
        index = element.length();
      }
      ids.add(Month.getMonthId(Dates.parseMonth(element.substring(0, index))));
    }
    GlobList list = new GlobList();
    timeViewPanel.getAllSelectableMonth(list);
    Set<Integer> valueSet = list.getValueSet(Month.ID);
    TestUtils.assertSetEquals(valueSet, ids);
  }

  public void assertCellSelected(int index) {
    Set<Selectable> list = timeViewPanel.getCurrentlySelectedToUpdate();
    Assert.assertEquals(1, list.size());
    Selectable selectable = list.iterator().next();
    GlobList globList = new GlobList();
    selectable.getObject(globList);
    Assert.assertEquals(1, globList.size());
    Integer currentyymm = globList.get(0).get(Month.ID);
    GlobList currentMonths = timeViewPanel.getRepository().getAll(Month.TYPE).sort(Month.ID);
    if (!currentyymm.equals(currentMonths.get(index).get(Month.ID))) {
      Assert.fail(Month.toString(currentMonths.get(index).get(Month.ID)) + " not selected\n" +
                  "Selection: " + currentMonths);
    }
  }

  public void assertCellSelected(boolean... cells) {
    Set<Selectable> list = timeViewPanel.getCurrentlySelectedToUpdate();
    GlobList selectedMonth = new GlobList();
    for (Selectable selectable : list) {
      selectable.getObject(selectedMonth);
    }
    GlobList currentMonths = timeViewPanel.getRepository().getAll(Month.TYPE).sort(Month.ID);

    for (int i = 0; i < cells.length; i++) {
      if (cells[i]) {
        Assert.assertTrue(Month.toString(currentMonths.get(i).get(Month.ID)) + "not selected",
                          selectedMonth.contains(currentMonths.get(i)));
      }
    }

    // TODO: finir le test
    System.out.println("MonthChecker.assertCellSelected: TBD");
//    assertTrue(table.selectionEquals(new boolean[][]{cells}));
  }

  public void selectCell(int index) {
    timeViewPanel.selectMonth(index);
  }

  public void selectCells(int... indexes) {
    timeViewPanel.selectMonth(indexes);
  }

  public void selectLast() {
    timeViewPanel.selectLastMonth();
  }

  public void selectNone() {
    selectCells();
  }

  public class ContentChecker {
    private ContentChecker() {
    }

    public ContentChecker add(String month, int year, double income, double incomePart, double expenses, double expensesPart) {
      return this;
    }

    public void check() {
    }
  }
}
