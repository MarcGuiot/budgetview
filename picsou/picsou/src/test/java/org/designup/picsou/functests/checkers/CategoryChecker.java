package org.designup.picsou.functests.checkers;

import junit.framework.Assert;
import org.designup.picsou.gui.categories.CategoryExpansionColumn;
import org.designup.picsou.gui.categories.CategoryView;
import org.designup.picsou.gui.utils.PicsouDescriptionService;
import org.designup.picsou.model.Category;
import org.designup.picsou.model.MasterCategory;
import org.globsframework.model.Glob;
import org.globsframework.utils.ArrayTestUtils;
import org.globsframework.utils.exceptions.ItemNotFound;
import org.uispec4j.Button;
import org.uispec4j.*;
import org.uispec4j.Panel;
import org.uispec4j.Window;
import static org.uispec4j.assertion.UISpecAssert.assertTrue;
import org.uispec4j.interception.WindowHandler;
import org.uispec4j.interception.WindowInterceptor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;

public class CategoryChecker extends DataChecker {
  private static final int NAME_COLUMN_INDEX = 1;

  private Table table;

  public CategoryChecker(Panel panel) {
    table = panel.getTable("category");
    table.setCellValueConverter(0, new TableCellValueConverter() {
      public Object getValue(int row, int column, Component renderedComponent, Object modelObject) {
        return "";
      }
    });
    table.setCellValueConverter(CategoryView.CATEGORY_COLUMN_INDEX, new TableCellValueConverter() {
      public Object getValue(int row, int column, Component renderedComponent, Object modelObject) {
        Panel panel = new Panel((Container)renderedComponent);
        return panel.getTextBox().getText();
      }
    });
  }

  public Table getTable() {
    return table;
  }

  public void selectNone() {
    select(new MasterCategory[0]);
  }

  public void select(String... categoryNames) {
    table.selectRows(getIndexes(categoryNames));
  }

  public void select(MasterCategory... categories) {
    table.selectRows(getIndexes(categories));
  }

  public void assertCategoryNamesEqual(String... expected) {
    int rowCount = table.getRowCount();
    String[] actual = new String[rowCount];
    for (int row = 0; row < rowCount; row++) {
      actual[row] = (String)table.getContentAt(row, NAME_COLUMN_INDEX);
    }
    ArrayTestUtils.assertEquals(expected, actual);
  }

  public String[] getSortedCategoryNames(List<MasterCategory> categories) {
    return getSortedCategoryNames(categories.toArray(new MasterCategory[categories.size()]));
  }

  public String[] getSortedCategoryNames(MasterCategory... categories) {
    List<String> names = new ArrayList<String>();
    for (MasterCategory category : categories) {
      names.add(getCategoryName(category));
    }
    Collections.sort(names);
    return names.toArray(new String[names.size()]);
  }

  public void assertSelectionEquals(MasterCategory... categories) {
    assertTrue(table.rowsAreSelected(getIndexes(categories)));
  }

  public void assertSelectionEquals(String categoryName) {
    assertTrue(table.rowsAreSelected(getIndexes(categoryName)));
  }

  public int[] getIndexes(MasterCategory... categories) {
    int[] indexes = new int[categories.length];
    for (int i = 0; i < categories.length; i++) {
      indexes[i] = -1;
      MasterCategory targetCategory = categories[i];
      for (int tableRow = 0; tableRow < table.getRowCount(); tableRow++) {
        Glob glob = (Glob)table.getContentAt(tableRow, NAME_COLUMN_INDEX, ModelTableCellValueConverter.INSTANCE);
        if (targetCategory.getId().equals(glob.get(Category.ID))) {
          indexes[i] = tableRow;
          continue;
        }
      }
      if (indexes[i] < 0) {
        throw new RuntimeException("Category " + categories[i] + " not found");
      }
    }
    return indexes;
  }

  public int[] getIndexes(String... categoryNames) {
    int[] indexes = new int[categoryNames.length];
    for (int i = 0; i < categoryNames.length; i++) {
      String categoryName = categoryNames[i];
      indexes[i] = getIndex(categoryName);
      if (indexes[i] < 0) {
        throw new ItemNotFound("Category '" + categoryNames[i] + "' not found");
      }
    }
    return indexes;
  }

  private int getIndex(String categoryName) {
    for (int tableRow = 0; tableRow < table.getRowCount(); tableRow++) {
      String name = (String)table.getContentAt(tableRow, NAME_COLUMN_INDEX);
      if (categoryName.equals(name)) {
        return tableRow;
      }
    }
    return -1;
  }

  public ContentChecker initContent() {
    return new ContentChecker();
  }

  public void createSubCategory(final MasterCategory master, final String name) {
    select(master);
    WindowInterceptor.init(triggerCreate())
      .process(new WindowHandler() {
        public Trigger process(Window window) throws Exception {
          window.getInputTextBox().setText(name);
          return window.getButton("OK").triggerClick();
        }
      })
      .run();
  }

  public Trigger triggerCreate() {
    return triggerButton("Add");
  }

  public Trigger triggerRename() {
    return triggerButton("Rename");
  }

  public void assertCreationNotAvailable() {
    Assert.assertNull(rolloverOnCategoryLabel().findUIComponent(Button.class, "Add"));
  }

  public void assertRenameNotAvailable() {
    Assert.assertNull(rolloverOnCategoryLabel().findUIComponent(Button.class, "Rename"));
  }

  public void assertDeletionNotAvailable() {
    Assert.assertNull(rolloverOnCategoryLabel().findUIComponent(Button.class, "Delete"));
  }

  public void assertCategoryExists(String categoryName) {
    Assert.assertTrue(getIndex(categoryName) >= 0);
  }

  public void assertCategoryNotFound(String categoryName) {
    Assert.assertTrue(getIndex(categoryName) < 0);
  }

  public void deleteSelected() {
    WindowInterceptor.init(triggerButton("Delete"))
      .processWithButtonClick("Yes")
      .run();
  }

  public Trigger triggerPopup(String name) {
    return table.triggerRightClick(getIndex(name), NAME_COLUMN_INDEX);
  }

  public Trigger triggerPopup(MasterCategory master) {
    return table.triggerRightClick(getIndex(master), NAME_COLUMN_INDEX);
  }

  private int getIndex(MasterCategory master) {
    return getIndexes(master)[0];
  }

  public void assertExpansionEnabled(MasterCategory master, boolean enabled) {
    JButton button = (JButton)table.getSwingRendererComponentAt(getIndex(master), 0);
    Assert.assertEquals(enabled,
                        (button.getIcon() != null) &&
                        (button.getIcon() != CategoryExpansionColumn.DISABLED_ICON));
  }

  public void assertExpanded(MasterCategory master, boolean expanded) {
    JButton button = (JButton)table.getSwingRendererComponentAt(getIndex(master), 0);
    if (expanded) {
      Assert.assertSame(CategoryExpansionColumn.EXPANDED_ICON, button.getIcon());
    }
    else {
      Assert.assertSame(CategoryExpansionColumn.COLLAPSED_ICON, button.getIcon());
    }
  }

  public void assertVisible(String categoryName, boolean visible) {
    Assert.assertEquals(visible, getIndex(categoryName) > -1);
  }

  public void toggleExpanded(MasterCategory master) {
    select(master);
    JButton button = (JButton)table.getSwingEditorComponentAt(getIndex(master), 0);
    new Button(button).click();
  }

  public void doubleClick(MasterCategory master) {
    table.doubleClick(getIndex(master), 1);
  }

  public void doubleClick(String category) {
    table.doubleClick(getIndex(category), 1);
  }

  private Trigger triggerButton(final String name) {
    return new Trigger() {
      public void run() throws Exception {
        Table.Cell cell = rolloverOnCategoryLabel();
        cell.getButton(name).click();
      }
    };
  }

  private Table.Cell rolloverOnCategoryLabel() {
    Table.Cell cell = table.editCell(table.getJTable().getSelectedRow(), CategoryView.CATEGORY_COLUMN_INDEX);
    mouseEnterInComponent(cell.getTextBox().getAwtComponent());
    return cell;
  }

  private void mouseEnterInComponent(Component component) {
    MouseEvent event = new MouseEvent(component, MouseEvent.MOUSE_ENTERED, 1, 0, 0, 0, 0, false);
    component.dispatchEvent(event);
  }

  public class ContentChecker {
    private Map<MasterCategory, Values> expectedValues = new EnumMap<MasterCategory, Values>(MasterCategory.class);

    private ContentChecker() {
    }

    public ContentChecker add(MasterCategory category,
                              double income, double incomePart,
                              double expense, double expensePart) {
      expectedValues.put(category, new Values(income, expense));
      return this;
    }

    public void check() {
      String[][] expectedArray = computeExpectedArray();
      assertTrue(table.contentEquals(expectedArray));
    }

    private String[][] computeExpectedArray() {
      List<MasterCategory> categories = getSortedCategories();
      String[][] expectedArray = new String[MasterCategory.values().length][3];
      int index = 0;
      for (MasterCategory category : categories) {
        Values values = expectedValues.get(category);
        if (values == null) {
          values = Values.NULL;
        }
        expectedArray[index][0] = "";
        expectedArray[index][1] = getCategoryName(category);
        expectedArray[index][2] = toString(values);
        index++;
      }
      return expectedArray;
    }

    private String toString(Values values) {
      double value = values.expense + values.income;
      if (value == 0) {
        return "";
      }
      return PicsouDescriptionService.INTEGER_FORMAT.format(value);
    }

    private List<MasterCategory> getSortedCategories() {
      List<MasterCategory> categories = new ArrayList<MasterCategory>(Arrays.asList(MasterCategory.values()));
      Collections.sort(categories, new Comparator<MasterCategory>() {
        public int compare(MasterCategory category1, MasterCategory category2) {
          if (MasterCategory.ALL.equals(category1)) {
            return -1;
          }
          if (MasterCategory.ALL.equals(category2)) {
            return 1;
          }
          if (MasterCategory.NONE.equals(category1)) {
            return -1;
          }
          if (MasterCategory.NONE.equals(category2)) {
            return 1;
          }
          String name1 = getCategoryName(category1);
          String name2 = getCategoryName(category2);
          return name1.compareTo(name2);
        }
      });
      return categories;
    }
  }

  private static class Values {
    double income;
    double expense;

    public Values(double income, double expense) {
      this.income = income;
      this.expense = expense;
    }

    static final Values NULL = new Values(0, 0);
  }
}
