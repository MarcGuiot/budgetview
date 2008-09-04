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
  private boolean oneSelection;
  private Table table;

  public SeriesEditionDialogChecker(Window dialog, boolean oneSelection) {
    this.dialog = dialog;
    this.oneSelection = oneSelection;
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

  public void checkType(String expectedType) {
    // TODO
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


  public SeriesEditionDialogChecker setCategory(MasterCategory... category) {
    Window chooser = WindowInterceptor.getModalDialog(dialog.getButton("Select").triggerClick());
    CategoryChooserChecker categoryChooser = new CategoryChooserChecker(chooser);
    if (oneSelection) {
      Assert.assertEquals(1, category.length);
      categoryChooser.selectCategory(getCategoryName(category[0]), oneSelection);
    }
    else {
      for (MasterCategory masterCategory : category) {
        categoryChooser.selectCategory(getCategoryName(masterCategory));
      }
      categoryChooser.validate();
    }
    checkCategory(category);
    return this;
  }

  public SeriesEditionDialogChecker checkCategory(MasterCategory... category) {
    if (oneSelection) {
      if (category.length == 0) {
        assertThat(dialog.getTextBox("singleCategoryLabel").textContains("Select a category"));
      }
      else {
        assertThat(dialog.getTextBox("singleCategoryLabel").textEquals("Category:" + getCategoryName(category[0])));
      }
    }
    else {
      if (category.length == 0) {
        assertThat(dialog.getTextBox("singleCategoryLabel").textContains("Select a category"));
        UISpecAssert.assertThat(dialog.getListBox("multipleCategoryList").isEmpty());
      }
      else {
        String[] categoryName = new String[category.length];
        int i = 0;
        for (MasterCategory masterCategory : category) {
          categoryName[i] = getCategoryName(masterCategory);
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

  public SeriesEditionDialogChecker toggleMonth(String monthLabel) {
    try {
      getMonthCheckBox(monthLabel).click();
    }
    catch (ItemNotFoundException e) {
      throw new RuntimeException("No component found for: " + monthLabel, e);
    }
    return this;
  }

  public SeriesEditionDialogChecker checkMonthIsActive(String monthLabel) {
    assertThat(monthLabel + " is not enabled", getMonthCheckBox(monthLabel).isSelected());
    return this;
  }

  public SeriesEditionDialogChecker checkMonthIsInactive(String monthLabel) {
    assertFalse(monthLabel + " is not disabled", getMonthCheckBox(monthLabel).isSelected());
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
    MonthChooserChecker month = new MonthChooserChecker(
      WindowInterceptor.getModalDialog(dialog.getButton(labelName).triggerClick()));
    int currentYear = Integer.parseInt(month.getCurrentYear().getText());
    int year = Month.toYear(monthId);
    for (; year < currentYear; year++) {
      month.previousYear();
    }
    for (; currentYear < year; currentYear++) {
      month.nextYear();
    }
    month.selectMonthInCurrent(Month.toMonth(monthId));
  }

  private void checkDate(String labelName, int monthId) {
    String text = Month.toMonth(monthId) + "/" + Month.toYear(monthId);
    checkDate(labelName, text);
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

  public SeriesEditionDialogChecker checkOk(boolean isEnable) {
    UISpecAssert.assertEquals(isEnable, dialog.getButton("ok").isEnabled());
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
}
