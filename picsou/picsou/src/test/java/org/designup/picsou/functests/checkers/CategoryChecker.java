package org.designup.picsou.functests.checkers;

import junit.framework.Assert;
import org.designup.picsou.gui.categories.CategoryView;
import org.designup.picsou.gui.categories.columns.CategoryExpansionColumn;
import org.designup.picsou.gui.description.PicsouDescriptionService;
import org.designup.picsou.model.Category;
import org.designup.picsou.model.MasterCategory;
import org.globsframework.model.Glob;
import org.globsframework.utils.ArrayTestUtils;
import org.globsframework.utils.exceptions.ItemNotFound;
import org.uispec4j.Button;
import org.uispec4j.*;
import org.uispec4j.Window;
import static org.uispec4j.assertion.UISpecAssert.assertTrue;
import org.uispec4j.interception.WindowInterceptor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;

public class CategoryChecker extends ViewChecker {
  private static final int NAME_COLUMN_INDEX = 1;

  private Table table;

  public CategoryChecker(Window window) {
    super(window);
  }

  public Table getTable() {
    if (table == null) {
      table = window.getTable("category");
      getTable().setCellValueConverter(0, new TableCellValueConverter() {
        public Object getValue(int row, int column, Component renderedComponent, Object modelObject) {
          return "";
        }
      });
      getTable().setCellValueConverter(CategoryView.CATEGORY_COLUMN_INDEX, new TableCellValueConverter() {
        public Object getValue(int row, int column, Component renderedComponent, Object modelObject) {
          return ((JLabel)renderedComponent).getText();
        }
      });
    }
    return table;
  }

  protected UIComponent findMainComponent(Window window) {
    return window.getTable("category");
  }

  public void selectNone() {
    select(new MasterCategory[0]);
  }

  public void select(String... categoryNames) {
    getTable().selectRows(getIndexes(categoryNames));
  }

  public void select(MasterCategory... categories) {
    getTable().selectRows(getIndexes(categories));
  }

  public void assertCategoryNamesEqual(String... expected) {
    int rowCount = getTable().getRowCount();
    String[] actual = new String[rowCount];
    for (int row = 0; row < rowCount; row++) {
      actual[row] = (String)getTable().getContentAt(row, NAME_COLUMN_INDEX);
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
    assertTrue(getTable().rowsAreSelected(getIndexes(categories)));
  }

  public void assertSelectionEquals(String categoryName) {
    assertTrue(getTable().rowsAreSelected(getIndexes(categoryName)));
  }

  public int[] getIndexes(MasterCategory... categories) {
    int[] indexes = new int[categories.length];
    for (int i = 0; i < categories.length; i++) {
      indexes[i] = -1;
      MasterCategory targetCategory = categories[i];
      for (int tableRow = 0; tableRow < getTable().getRowCount(); tableRow++) {
        Glob glob = (Glob)getTable().getContentAt(tableRow, NAME_COLUMN_INDEX, ModelTableCellValueConverter.INSTANCE);
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
    for (int tableRow = 0; tableRow < getTable().getRowCount(); tableRow++) {
      String name = (String)getTable().getContentAt(tableRow, NAME_COLUMN_INDEX);
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
    CategoryEditionChecker edition = openEditionDialog();
    edition.createSubCategory(name);
    edition.validate();
  }

  public Trigger triggerRename() {
    return triggerButton("Rename");
  }

  public void assertCreationNotAvailable() {
    Assert.assertNull(rolloverOnCategoryLabel().findUIComponent(Button.class, "Add"));
  }

  public void assertCategoryExists(String categoryName) {
    Assert.assertTrue(getIndex(categoryName) >= 0);
  }

  public void assertCategoryNotFound(String categoryName) {
    Assert.assertTrue(getIndex(categoryName) < 0);
  }

  public void deleteSelected(MasterCategory replaceByCategory) {
    CategoryEditionChecker edition = openEditionDialog();
    edition.deleteSubCategory(getCategoryName(replaceByCategory));
    edition.validate();
  }

  public void deleteSelected() {
    CategoryEditionChecker edition = openEditionDialog();
    edition.deleteSubCategory();
    edition.validate();
  }

  private int getIndex(MasterCategory master) {
    return getIndexes(master)[0];
  }

  public void assertExpansionEnabled(MasterCategory master, boolean enabled) {
    JButton button = (JButton)getTable().getSwingRendererComponentAt(getIndex(master), 0);
    Assert.assertEquals(enabled,
                        (button.getIcon() != null) &&
                        (button.getIcon() != CategoryExpansionColumn.DISABLED_ICON));
  }

  public void assertExpanded(MasterCategory master, boolean expanded) {
    JButton button = (JButton)getTable().getSwingRendererComponentAt(getIndex(master), 0);
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
    JButton button = (JButton)getTable().getSwingEditorComponentAt(getIndex(master), 0);
    new Button(button).click();
  }

  public void doubleClick(MasterCategory master) {
    getTable().doubleClick(getIndex(master), 1);
  }

  public void doubleClick(String category) {
    getTable().doubleClick(getIndex(category), 1);
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
    Table.Cell cell = getTable().editCell(getTable().getJTable().getSelectedRow(), CategoryView.CATEGORY_COLUMN_INDEX);
    mouseEnterInComponent(cell.getTextBox().getAwtComponent());
    return cell;
  }

  private void mouseEnterInComponent(Component component) {
    MouseEvent event = new MouseEvent(component, MouseEvent.MOUSE_ENTERED, 1, 0, 0, 0, 0, false);
    component.dispatchEvent(event);
  }

  public CategoryEditionChecker openEditionDialog() {
    Window dialog = WindowInterceptor.getModalDialog(window.getButton("edit").triggerClick());
    return new CategoryEditionChecker(dialog);
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
      assertTrue(getTable().contentEquals(expectedArray));
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
