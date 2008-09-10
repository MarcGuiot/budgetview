package org.designup.picsou.functests.checkers;

import junit.framework.Assert;
import org.designup.picsou.model.MasterCategory;
import org.designup.picsou.model.Month;
import org.uispec4j.Button;
import org.uispec4j.*;
import org.uispec4j.Window;
import org.uispec4j.assertion.UISpecAssert;
import static org.uispec4j.assertion.UISpecAssert.assertFalse;
import static org.uispec4j.assertion.UISpecAssert.assertThat;
import org.uispec4j.finder.ComponentMatchers;
import org.uispec4j.interception.WindowInterceptor;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.InvocationTargetException;

public class SeriesEditionDialogChecker extends DataChecker {
  private Window dialog;
  private boolean singleSelection;
  private Table table;

  public SeriesEditionDialogChecker(Window dialog, boolean singleSelection) {
    this.dialog = dialog;
    this.singleSelection = singleSelection;
    this.table = dialog.getTable();
  }

  public SeriesEditionDialogChecker checkName(String seriesName) {
    assertThat(dialog.getInputTextBox("nameField").textEquals(seriesName));
    return this;
  }

  public SeriesEditionDialogChecker setName(String seriesName) {
    dialog.getInputTextBox("nameField").setText(seriesName);
    return this;
  }

  public SeriesEditionDialogChecker unselect() {
    dialog.getListBox("seriesList").clearSelection();
    return this;
  }

  public SeriesEditionDialogChecker checkAmount(String displayedValue) {
    assertThat(dialog.getInputTextBox("amountEditor").textEquals(displayedValue));
    return this;
  }

  public SeriesEditionDialogChecker setAmount(String value) {
    dialog.getInputTextBox("amountEditor").setText(value);
    return this;
  }

  public SeriesEditionDialogChecker unselectCategory(MasterCategory... category) {
    Window chooser = WindowInterceptor.getModalDialog(dialog.getButton("Select").triggerClick());
    CategoryChooserChecker categoryChooser = new CategoryChooserChecker(chooser);
    for (MasterCategory masterCategory : category) {
      categoryChooser.selectCategory(getCategoryName(masterCategory));
    }
    categoryChooser.checkUnSelected(category);
    categoryChooser.validate();
    return this;
  }

  public SeriesEditionDialogChecker setCategory(MasterCategory... masterCategories) {
    String categories[] = new String[masterCategories.length];
    int i = 0;
    for (MasterCategory category : masterCategories) {
      categories[i] = getCategoryName(category);
      i++;
    }
    return setCategory(categories);
  }

  public SeriesEditionDialogChecker setCategory(String... categories) {
    Window chooser = WindowInterceptor.getModalDialog(dialog.getButton("Select").triggerClick());
    CategoryChooserChecker categoryChooser = new CategoryChooserChecker(chooser);
    if (singleSelection) {
      Assert.assertEquals(1, categories.length);
      categoryChooser.checkTitle("Select a category");
      categoryChooser.selectCategory(categories[0], singleSelection);
    }
    else {
      categoryChooser.checkTitle("Select categories");
      for (String category : categories) {
        categoryChooser.selectCategory(category);
      }
      categoryChooser.validate();
    }
    return checkCategory(categories);
  }

  public SeriesEditionDialogChecker checkCategory(MasterCategory... masterCategories) {
    String categories[] = new String[masterCategories.length];
    int i = 0;
    for (MasterCategory category : masterCategories) {
      categories[i] = getCategoryName(category);
      i++;
    }
    return checkCategory(categories);
  }

  public SeriesEditionDialogChecker checkCategory() {
    return checkCategory(new String[0]);
  }

  public SeriesEditionDialogChecker checkCategory(String... categories) {
    if (singleSelection) {
      if (categories.length == 0) {
        assertThat(dialog.getTextBox("singleCategoryLabel").textContains("Select a category"));
      }
      else {
        assertThat(dialog.getTextBox("singleCategoryLabel").textEquals("Category:" + categories[0]));
      }
    }
    else {
      if (categories.length == 0) {
        assertThat(dialog.getTextBox("singleCategoryLabel").textContains("Select a category"));
        UISpecAssert.assertThat(dialog.getListBox("multipleCategoryList").isEmpty());
      }
      else {
        String[] categoryName = new String[categories.length];
        int i = 0;
        for (String category : categories) {
          categoryName[i] = category;
          i++;
        }
        UISpecAssert.assertThat(dialog.getListBox("multipleCategoryList").contentEquals(categoryName));
      }
    }
    return this;
  }


  public CategoryChooserChecker openCategory() {
    Window chooser = WindowInterceptor.getModalDialog(dialog.getButton("Select").triggerClick());
    return new CategoryChooserChecker(chooser);
  }

  public SeriesEditionDialogChecker selectAllMonths() {
    table.selectRowSpan(0, table.getRowCount() - 1);
    return this;
  }

  public SeriesEditionDialogChecker checkTable(Object[][] content) {
    assertThat(table.contentEquals(content));
    return this;
  }

  public SeriesEditionDialogChecker checkMonthSelected(int index) {
    assertThat(table.rowIsSelected(index));
    return this;
  }

  public SeriesEditionDialogChecker checkMonthsSelected(int... rows) {
    assertThat(table.rowsAreSelected(rows));
    return this;
  }

  public SeriesEditionDialogChecker toggleMonth(String... monthsLabel) {
    for (String monthLabel : monthsLabel) {
      try {
        getMonthCheckBox(monthLabel).click();
      }
      catch (ItemNotFoundException e) {
        throw new RuntimeException("No component found for: " + monthLabel, e);
      }
    }
    return this;
  }

  public SeriesEditionDialogChecker checkMonthIsChecked(String... monthsLabel) {
    for (String monthLabel : monthsLabel) {
      assertThat(monthLabel + " is not checked", getMonthCheckBox(monthLabel).isSelected());
    }
    return this;
  }

  public SeriesEditionDialogChecker checkMonthIsNotChecked(String... monthsLabel) {
    for (String monthLabel : monthsLabel) {
      assertFalse(monthLabel + " is checked", getMonthCheckBox(monthLabel).isSelected());
    }
    return this;
  }

  public SeriesEditionDialogChecker checkMonthIsEnabled(String... monthsLabel) {
    for (String monthLabel : monthsLabel) {
      assertThat(monthLabel + " is disabled", getMonthCheckBox(monthLabel).isEnabled());
    }
    return this;
  }

  public SeriesEditionDialogChecker checkMonthIsDisabled(String... monthsLabel) {
    for (String monthLabel : monthsLabel) {
      assertFalse(monthLabel + " is enabled", getMonthCheckBox(monthLabel).isEnabled());
    }
    return this;
  }

  private CheckBox getMonthCheckBox(String monthLabel) {
    return dialog.getCheckBox(ComponentMatchers.componentLabelFor(monthLabel));
  }

  public void validate() {
    dialog.getButton("OK").click();
    checkClosed();
  }

  public Trigger triggerValidate() {
    return dialog.getButton("OK").triggerClick();
  }

  public void cancel() {
    dialog.getButton("Cancel").click();
    checkClosed();
  }

  public SeriesEditionDialogChecker checkNoSeries() {
    assertThat(dialog.getListBox().isEmpty());
    return this;
  }

  public SeriesEditionDialogChecker checkSeriesList(String... names) {
    assertThat(dialog.getListBox("seriesList").contentEquals(names));
    return this;
  }

  public SeriesEditionDialogChecker selectSeries(final int index) {
    try {
      SwingUtilities.invokeAndWait(new Runnable() {
        public void run() {
          dialog.getListBox("seriesList").selectIndex(index);
        }
      });
    }
    catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
    catch (InvocationTargetException e) {
      throw new RuntimeException(e);
    }
    return this;
  }

  public SeriesEditionDialogChecker selectSeries(String name) {
    dialog.getListBox("seriesList").select(name);
    return this;
  }

  public SeriesEditionDialogChecker checkSeriesSelected(String name) {
    assertThat(dialog.getListBox("seriesList").selectionEquals(name));
    return this;
  }

  public SeriesEditionDialogChecker createSeries() {
    dialog.getButton("create").click();
    return this;
  }

  public SeriesEditionDialogChecker deleteSeries() {
    dialog.getButton("delete").click();
    return this;
  }

  public SeriesEditionDialogChecker setStartDate(int monthId) {
    setDate("beginSeriesCalendar", monthId);
    return this;
  }

  public SeriesEditionDialogChecker checkNoStartDate() {
    assertFalse(dialog.getTextBox("beginSeriesDate").isVisible());
    assertFalse(dialog.getButton("deleteBeginSeriesDate").isVisible());
    return this;
  }

  public SeriesEditionDialogChecker checkStartDate(String monthId) {
    checkDate("beginSeriesDate", monthId);
    return this;
  }

  public SeriesEditionDialogChecker setEndDate(int monthId) {
    setDate("endSeriesCalendar", monthId);
    return this;
  }

  public SeriesEditionDialogChecker checkNoEndDate() {
    assertFalse(dialog.getTextBox("endSeriesDate").isVisible());
    assertFalse(dialog.getButton("deleteEndSeriesDate").isVisible());
    return this;
  }

  public SeriesEditionDialogChecker checkEndDate(String monthId) {
    checkDate("endSeriesDate", monthId);
    return this;
  }

  private void setDate(String labelName, int monthId) {
    MonthChooserChecker month = getMonthChooser(labelName);
    month.centerTo(monthId)
      .selectMonthInCurrent(Month.toMonth(monthId));
  }


  public MonthChooserChecker getStartCalendar() {
    return getMonthChooser("beginSeriesCalendar");
  }

  public MonthChooserChecker getEndCalendar() {
    return getMonthChooser("endSeriesCalendar");
  }

  private MonthChooserChecker getMonthChooser(String labelName) {
    return new MonthChooserChecker(
      WindowInterceptor.getModalDialog(dialog.getButton(labelName).triggerClick()));
  }

  private void checkDate(String labelName, String text) {
    assertThat(dialog.getTextBox(labelName).textEquals(text));
  }

  public SeriesEditionDialogChecker checkAllMonthsDisabled() {
    for (UIComponent checkBox : dialog.getUIComponents(CheckBox.class)) {
      assertFalse(checkBox.isEnabled());
    }
    return this;
  }

  public SeriesEditionDialogChecker checkAllFieldsDisabled() {
    for (Component component : dialog.getSwingComponents(JTextField.class)) {
      TextBox textBox = new TextBox((JTextField)component);
      assertFalse(textBox.isEnabled());
    }
    return this;
  }

  public void checkClosed() {
    assertFalse(dialog.isVisible());
  }

  private TextBox getSingleCategoryLabel() {
    return dialog.getTextBox("singleCategoryLabel");
  }

  public SeriesEditionDialogChecker checkCategorizeEnable(boolean enable) {
    Button categoryzeButton = getCategoryzeButton();
    if (enable) {
      assertThat(categoryzeButton.isEnabled());
    }
    else {
      assertFalse(categoryzeButton.isEnabled());
    }
    return this;
  }

  public SeriesEditionDialogChecker checkSingleCategorizeIsVisible(boolean visible) {
    TextBox label = getSingleCategoryLabel();
    UISpecAssert.assertEquals(visible, label.isVisible());
    return this;
  }

  public SeriesEditionDialogChecker checkCategorizeLabel() {
    TextBox label = getSingleCategoryLabel();
    UISpecAssert.assertThat(label.textEquals("Category:"));
    return this;
  }


  private Button getCategoryzeButton() {
    return dialog.getButton("assignCategory");
  }

  public SeriesEditionDialogChecker checkCategoryListEnable(boolean enable) {
    checkMultiCategorizeIsVisible(true);
    ListBox multiCategoryList = getMultiCategoryList();
    TextBox label = getMultiCategotyLabel();
    UISpecAssert.assertEquals(enable, label.isEnabled());
    UISpecAssert.assertEquals(enable, multiCategoryList.isEnabled());
    return this;
  }

  public SeriesEditionDialogChecker checkMultiCategorizeIsVisible(boolean visible) {
    ListBox multiCategoryList = getMultiCategoryList();
    TextBox label = getMultiCategotyLabel();
    UISpecAssert.assertEquals(visible, label.isVisible());
    UISpecAssert.assertEquals(visible, multiCategoryList.isVisible());
    return this;
  }

  private ListBox getMultiCategoryList() {
    return dialog.getListBox("multipleCategoryList");
  }

  private TextBox getMultiCategotyLabel() {
    return dialog.getTextBox("multiCategoryLabel");
  }

  public SeriesEditionDialogChecker checkOkEnabled(boolean isEnabled) {
    UISpecAssert.assertEquals(isEnabled, dialog.getButton("ok").isEnabled());
    return this;

  }

  public SeriesEditionDialogChecker removeBeginDate() {
    dialog.getButton("deleteBeginSeriesDate").click();
    return this;
  }

  public SeriesEditionDialogChecker removeEndDate() {
    dialog.getButton("deleteEndSeriesDate").click();
    return this;
  }

  public SeriesEditionDialogChecker checkCalendarsAreDisable() {
    assertFalse(dialog.getButton("beginSeriesCalendar").isEnabled());
    assertFalse(dialog.getButton("endSeriesCalendar").isEnabled());
    return this;
  }

  public SeriesEditionDialogChecker checkNameIsSelected() {
    JTextField textEditor = (JTextField)dialog.getInputTextBox("nameField").getAwtComponent();
    Assert.assertEquals(textEditor.getText(), textEditor.getSelectedText());
    return this;
  }

  public SeriesEditionDialogChecker checkAmountIsSelected() {
    JTextField textEditor = (JTextField)dialog.getInputTextBox("amountEditor").getAwtComponent();
    Assert.assertEquals(textEditor.getText(), textEditor.getSelectedText());
    return this;
  }

  public SeriesEditionDialogChecker checkLabelExpenseAmount() {
    assertThat(dialog.getTextBox("seriesEditionAmountLabel").textEquals("Planned expense"));
    return this;
  }

  public SeriesEditionDialogChecker checkLabelIncomeAmount() {
    assertThat(dialog.getTextBox("seriesEditionAmountLabel").textEquals("Planned income"));
    return this;
  }

  public SeriesDeleteDialogChecker deleteSeriesWithAsk() {
    return new SeriesDeleteDialogChecker(WindowInterceptor.getModalDialog(dialog.getButton("delete").triggerClick()));
  }
}
