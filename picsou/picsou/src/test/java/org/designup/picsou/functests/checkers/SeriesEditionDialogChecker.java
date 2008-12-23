package org.designup.picsou.functests.checkers;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;
import org.designup.picsou.model.MasterCategory;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.ProfileType;
import org.uispec4j.*;
import org.uispec4j.assertion.UISpecAssert;
import static org.uispec4j.assertion.UISpecAssert.*;
import org.uispec4j.finder.ComponentMatchers;
import org.uispec4j.interception.WindowHandler;
import org.uispec4j.interception.WindowInterceptor;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;

public class SeriesEditionDialogChecker extends DataChecker {
  private Window dialog;
  private boolean singleCategorySeries;
  private Table table;
  public static final String JAN = "Jan";
  public static final String FEB = "Feb";
  public static final String MAR = "Mar";
  public static final String APR = "Apr";
  public static final String MAY = "May";
  public static final String JUN = "Jun";
  public static final String JUL = "Jul";
  public static final String AOU = "Aug";
  public static final String SEP = "Sep";
  public static final String OCT = "Oct";
  public static final String NOV = "Nov";
  public static final String DEC = "Dec";

  public SeriesEditionDialogChecker(Window dialog, boolean singleCategorySeries) {
    this.dialog = dialog;
    this.singleCategorySeries = singleCategorySeries;
  }

  public SeriesEditionDialogChecker checkTitle(String text) {
    assertThat(dialog.getTextBox("title").textEquals(text));
    return this;
  }

  public SeriesEditionDialogChecker checkName(String seriesName) {
    TextBox getNameBox = getNameBox();
    assertThat(getNameBox.textEquals(seriesName));
    return this;
  }

  public TextBox getNameBox() {
    return dialog.getInputTextBox("nameField");
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
    TextBox getAmount = getAmountTextBox();
    assertThat(getAmount.textEquals(displayedValue));
    return this;
  }

  public SeriesEditionDialogChecker checkAmountDisabled() {
    assertFalse(getAmountTextBox().isEnabled());
    assertFalse(dialog.getRadioButton("positiveAmounts").isEnabled());
    assertFalse(dialog.getRadioButton("negativeAmounts").isEnabled());
    return this;
  }

  public TextBox getAmountTextBox() {
    try {
      return dialog.getInputTextBox("amountEditor");
    }
    catch (ItemNotFoundException e) {
      throw new AssertionFailedError("Amount editor not found - make sure the series is in manual mode");
    }
  }

  public SeriesEditionDialogChecker setAmount(double value) {
    return setAmount(Double.toString(value));
  }

  public SeriesEditionDialogChecker setAmount(String value) {
    getAmountTextBox().setText(value);
    return this;
  }

  public SeriesEditionDialogChecker unselectCategory(MasterCategory... category) {
    Window chooser = WindowInterceptor.getModalDialog(dialog.getButton("Select").triggerClick());
    CategoryChooserChecker categoryChooser = new CategoryChooserChecker(chooser);
    for (MasterCategory masterCategory : category) {
      categoryChooser.unselectCategory(getCategoryName(masterCategory));
    }
    categoryChooser.checkUnselected(category);
    categoryChooser.validate();
    return this;
  }

  public SeriesEditionDialogChecker setCategory(MasterCategory master) {
    return setCategories(master);
  }

  public SeriesEditionDialogChecker setCategories(MasterCategory... masters) {
    String categories[] = new String[masters.length];
    int i = 0;
    for (MasterCategory category : masters) {
      categories[i] = getCategoryName(category);
      i++;
    }
    return setCategories(categories);
  }

  public SeriesEditionDialogChecker addCategory(String... categories) {
    Window chooser = WindowInterceptor.getModalDialog(dialog.getButton("Select").triggerClick());
    CategoryChooserChecker categoryChooser = new CategoryChooserChecker(chooser);
    if (singleCategorySeries) {
      Assert.fail("only one category can be selected");
    }
    else {
      categoryChooser.checkTitle("Select categories");
      for (String category : categories) {
        categoryChooser.selectCategory(category);
      }
      categoryChooser.validate();
    }
    return this;
  }

  public SeriesEditionDialogChecker setCategories(String... categories) {
    Window chooser = WindowInterceptor.getModalDialog(dialog.getButton("Select").triggerClick());
    CategoryChooserChecker categoryChooser = new CategoryChooserChecker(chooser);
    if (singleCategorySeries) {
      Assert.assertEquals(1, categories.length);
      categoryChooser.checkTitle("Select a category");
      categoryChooser.selectCategory(categories[0], singleCategorySeries);
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

  public SeriesEditionDialogChecker checkCategory(MasterCategory master) {
    return checkCategories(master);
  }

  public SeriesEditionDialogChecker checkCategories(MasterCategory... masters) {
    String categories[] = new String[masters.length];
    int i = 0;
    for (MasterCategory category : masters) {
      categories[i] = getCategoryName(category);
      i++;
    }
    return checkCategory(categories);
  }

  public SeriesEditionDialogChecker checkNoCategory() {
    if (singleCategorySeries) {
      assertThat(dialog.getTextBox("singleCategoryField").textIsEmpty());
      assertThat(dialog.getTextBox("missingCategoryLabel").textContains("You must select a category"));
    }
    else {
      UISpecAssert.assertThat(dialog.getListBox("multipleCategoryList").isEmpty());
      assertThat(dialog.getTextBox("missingCategoryLabel").textContains("You must select at least one category"));
    }
    return this;
  }

  public SeriesEditionDialogChecker checkCategory(String... categories) {
    if (categories.length == 0) {
      return checkNoCategory();
    }

    if (singleCategorySeries) {
      if (categories.length > 1) {
        fail("Only one category should be expected for single category series");
      }
      assertThat(dialog.getTextBox("singleCategoryField").textEquals(categories[0]));
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
    return this;
  }

  public CategoryChooserChecker openCategory() {
    Window chooser = WindowInterceptor.getModalDialog(dialog.getButton("Select").triggerClick());
    return new CategoryChooserChecker(chooser);
  }

  public SeriesEditionDialogChecker selectAllMonths() {
    getTable().selectRowSpan(0, getTable().getRowCount() - 1);
    return this;
  }

  public SeriesEditionDialogChecker selectMonth(Integer monthId) {
    int[] indices = getTable().getRowIndices(0, Integer.toString(Month.toYear(monthId)));
    for (int indice : indices) {
      if (getTable().getContentAt(indice, 1).equals(Month.getFullMonthLabel(Month.toMonth(monthId)))) {
        getTable().selectRow(indice);
        return this;
      }
    }
    assertThat(fail(monthId + " not found "));
    return null;
  }

  public SeriesEditionDialogChecker selectNoMonth() {
    getTable().clearSelection();
    return this;
  }

  public SeriesEditionDialogChecker checkTable(Object[][] content) {
    assertThat(getTable().contentEquals(content));
    return this;
  }

  private Table getTable() {
    if (table == null) {
      this.table = dialog.getTable();
    }
    return table;
  }

  public SeriesEditionDialogChecker switchToManual() {
    dialog.getButton("manual").click();
    return this;
  }

  public SeriesEditionDialogChecker switchToAutomatic() {
    WindowInterceptor.init(dialog.getButton("automatic").triggerClick())
      .process(new WindowHandler() {
        public Trigger process(Window window) throws Exception {
          return window.getButton("ok").triggerClick();
        }
      }).run();
    table = null;
    return this;
  }

  public SeriesEditionDialogChecker checkMonthSelectorsVisible(boolean visible) {
    checkComponentVisible(dialog, JPanel.class, "monthSelectionPanel", visible);
    return this;
  }

  public SeriesEditionDialogChecker checkMonthSelected(int index) {
    assertThat(getTable().rowIsSelected(index));
    return this;
  }

  public SeriesEditionDialogChecker checkMonthsSelected(int... rows) {
    assertThat(getTable().rowsAreSelected(rows));
    return this;
  }

  public SeriesEditionDialogChecker toggleMonth(int... months) {
    String labels[] = new String[months.length];
    for (int i = 0; i < months.length; i++) {
      labels[i] = transposeMonthId(months[i]);
    }
    return toggleMonth(labels);
  }

  private String transposeMonthId(int month) {
    switch (month) {
      case 1:
        return JAN;
      case 2:
        return FEB;
      case 3:
        return MAR;
      case 4:
        return APR;
      case 5:
        return MAY;
      case 6:
        return JUN;
      case 7:
        return JUL;
      case 8:
        return AOU;
      case 9:
        return SEP;
      case 10:
        return OCT;
      case 11:
        return NOV;
      case 12:
        return DEC;
    }
    Assert.fail(month + " unknown");
    return "";
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

  public SeriesEditionDialogChecker checkMonthIsChecked(int... months) {
    for (int month : months) {
      assertThat(transposeMonthId(month) + " is not checked", getMonthCheckBox(transposeMonthId(month)).isSelected());
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

  public SeriesEditionDialogChecker checkMonthIsNotChecked(int... months) {
    for (int month : months) {
      assertFalse(transposeMonthId(month) + " is checked", getMonthCheckBox(transposeMonthId(month)).isSelected());
    }
    return this;
  }

  public SeriesEditionDialogChecker checkMonthsEnabled(String... monthsLabel) {
    for (String monthLabel : monthsLabel) {
      assertThat(monthLabel + " is disabled", getMonthCheckBox(monthLabel).isEnabled());
    }
    return this;
  }

  public SeriesEditionDialogChecker checkMonthsDisabled(String... monthsLabel) {
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


  public SeriesEditionDialogChecker checkSeriesListIsEmpty() {
    return checkSeriesListEquals();
  }

  public SeriesEditionDialogChecker checkSeriesListEquals(String... names) {
    assertThat(dialog.getListBox("seriesList").contentEquals(names));
    return this;
  }

  public SeriesEditionDialogChecker checkSeriesListContains(String... names) {
    assertThat(dialog.getListBox("seriesList").contains(names));
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

  public SeriesEditionDialogChecker deleteSelectedSeries() {
    dialog.getButton("deleteSelected").click();
    return this;
  }

  public SeriesDeleteDialogChecker deleteSelectedSeriesWithConfirmation() {
    return SeriesDeleteDialogChecker.init(dialog.getButton("deleteSelected").triggerClick());
  }

  public void deleteCurrentSeries() {
    dialog.getButton("Delete...").click();
    assertFalse(dialog.isVisible());
  }

  public void deleteCurrentSeriesWithConfirmation() {
    SeriesDeleteDialogChecker.init(dialog.getButton("Delete...").triggerClick())
      .checkMessage()
      .validate();
    assertFalse(dialog.isVisible());
  }

  public SeriesEditionDialogChecker deleteCurrentSeriesWithConfirmationAndCancel() {
    SeriesDeleteDialogChecker.init(dialog.getButton("Delete...").triggerClick())
      .checkMessage()
      .cancel();
    assertTrue(dialog.isVisible());
    return this;
  }

  public SeriesEditionDialogChecker setStartDate(int monthId) {
    setDate("seriesStartDateChooser", monthId);
    return this;
  }

  public SeriesEditionDialogChecker checkNoStartDate() {
    assertTrue(dialog.getTextBox("seriesStartDate").isVisible());
    assertFalse(dialog.getButton("deleteSeriesStartDate").isVisible());
    return this;
  }

  public SeriesEditionDialogChecker checkStartDate(String date) {
    checkDate("seriesStartDate", date);
    return this;
  }

  public SeriesEditionDialogChecker setEndDate(int date) {
    setDate("seriesEndDateChooser", date);
    return this;
  }

  public SeriesEditionDialogChecker checkNoEndDate() {
    assertTrue(dialog.getTextBox("seriesEndDate").isVisible());
    assertFalse(dialog.getButton("deleteSeriesEndDate").isVisible());
    return this;
  }

  public SeriesEditionDialogChecker checkEndDate(String monthId) {
    checkDate("seriesEndDate", monthId);
    return this;
  }

  public SeriesEditionDialogChecker setSingleMonthDate(int monthId) {
    setDate("singleMonthChooser", monthId);
    return this;
  }

  public SeriesEditionDialogChecker checkSingleMonthDate(String date) {
    checkDate("singleMonthDate", date);
    checkComponentVisible(dialog, JTextField.class, "seriesStartDate", false);
    checkComponentVisible(dialog, JTextField.class, "seriesEndDate", false);
    return this;
  }

  private void setDate(String labelName, int monthId) {
    MonthChooserChecker month = getMonthChooser(labelName);
    month.centerOn(monthId)
      .selectMonthInCurrent(Month.toMonth(monthId));
  }

  public MonthChooserChecker editStartDate() {
    return getMonthChooser("seriesStartDateChooser");
  }

  public MonthChooserChecker editEndDate() {
    return getMonthChooser("seriesEndDateChooser");
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

  public void checkClosed() {
    assertFalse(dialog.isVisible());
  }

  public SeriesEditionDialogChecker checkCategorizeEnabled(boolean enable) {
    Button categorizeButton = getCategorizeButton();
    if (enable) {
      assertThat(categorizeButton.isEnabled());
    }
    else {
      assertFalse(categorizeButton.isEnabled());
    }
    return this;
  }

  public SeriesEditionDialogChecker checkSingleCategorizeIsVisible(boolean visible) {
    TextBox textField = dialog.getTextBox("singleCategoryField");
    UISpecAssert.assertEquals(visible, textField.isVisible());
    return this;
  }

  public SeriesEditionDialogChecker checkCategorizeLabelIsEmpty() {
    TextBox label = dialog.getTextBox("singleCategoryField");
    UISpecAssert.assertThat(label.textIsEmpty());
    return this;
  }

  private Button getCategorizeButton() {
    return dialog.getButton("assignCategory");
  }

  public SeriesEditionDialogChecker checkCategoryListEnable(boolean enable) {
    checkMultiCategorizeIsVisible(true);
    ListBox multiCategoryList = getMultiCategoryList();
    UISpecAssert.assertEquals(enable, multiCategoryList.isEnabled());
    return this;
  }

  public SeriesEditionDialogChecker checkMultiCategorizeIsVisible(boolean visible) {
    ListBox multiCategoryList = getMultiCategoryList();
    UISpecAssert.assertEquals(visible, multiCategoryList.isVisible());
    return this;
  }

  private ListBox getMultiCategoryList() {
    return dialog.getListBox("multipleCategoryList");
  }

  public SeriesEditionDialogChecker checkOkEnabled(boolean isEnabled) {
    UISpecAssert.assertEquals(isEnabled ? "ok is disable" : "ok is enable",
                              isEnabled, dialog.getButton("ok").isEnabled());
    return this;

  }

  public SeriesEditionDialogChecker removeBeginDate() {
    dialog.getButton("deleteSeriesStartDate").click();
    return this;
  }

  public SeriesEditionDialogChecker removeEndDate() {
    dialog.getButton("deleteSeriesEndDate").click();
    return this;
  }

  public SeriesEditionDialogChecker checkCalendarsAreDisabled() {
    assertFalse(dialog.getButton("seriesStartDateChooser").isEnabled());
    assertFalse(dialog.getButton("seriesEndDateChooser").isEnabled());
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

  public SeriesEditionDialogChecker checkAmountLabel(final String text) {
    assertThat(dialog.getTextBox("seriesBudgetEditionAmountLabel").textEquals(text));
    return this;
  }

  public SeriesEditionDialogChecker selectPositiveAmounts() {
    dialog.getRadioButton("positiveAmounts").click();
    return this;
  }

  public SeriesEditionDialogChecker checkPositiveAmountsSelected() {
    assertThat(dialog.getRadioButton("positiveAmounts").isSelected());
    return this;
  }

  public SeriesEditionDialogChecker selectNegativeAmounts() {
    dialog.getRadioButton("negativeAmounts").click();
    return this;
  }

  public SeriesEditionDialogChecker checkNegativeAmountsSelected() {
    assertThat(dialog.getRadioButton("negativeAmounts").isSelected());
    return this;
  }

  public SeriesEditionDialogChecker checkPositiveAmountsNotSelected() {
    assertFalse(dialog.getRadioButton("positiveAmounts").isSelected());
    return this;
  }

  public SeriesEditionDialogChecker checkAmountsRadioAreNotVisible() {
    assertFalse(dialog.getRadioButton("negativeAmounts").isVisible());
    assertFalse(dialog.getRadioButton("positiveAmounts").isVisible());
    return this;
  }

  public SeriesEditionDialogChecker checkProfiles(String... profiles) {
    UISpecAssert.assertThat(getProfileCombo().contentEquals(profiles));
    return this;
  }

  public SeriesEditionDialogChecker checkSelectedProfile(String profile) {
    UISpecAssert.assertThat(getProfileCombo().selectionEquals(profile));
    return this;
  }

  public SeriesEditionDialogChecker checkInManual() {
    UISpecAssert.assertTrue(dialog.getButton("automatic").isVisible());
    return this;
  }

  public SeriesEditionDialogChecker checkInAutomatic() {
    UISpecAssert.assertTrue(dialog.getButton("manual").isVisible());
    return this;
  }

  private ComboBox getProfileCombo() {
    return dialog.getComboBox("profileCombo");
  }

  public SeriesEditionDialogChecker monthsAreHidden() {
    assertFalse(dialog.getPanel("monthSelectionPanel").isVisible());
    return this;
  }

  public SeriesEditionDialogChecker monthsAreVisible() {
    assertTrue(dialog.getPanel("monthSelectionPanel").isVisible());
    return this;
  }

  public SeriesEditionDialogChecker setUnknown() {
    getProfileCombo().select(ProfileType.IRREGULAR.getLabel());
    return this;
  }

  public SeriesEditionDialogChecker setSixMonths() {
    getProfileCombo().select(ProfileType.SIX_MONTHS.getLabel());
    return this;
  }

  public SeriesEditionDialogChecker setTwoMonths() {
    getProfileCombo().select(ProfileType.TWO_MONTHS.getLabel());
    return this;
  }

  public SeriesEditionDialogChecker setFourMonths() {
    getProfileCombo().select(ProfileType.FOUR_MONTHS.getLabel());
    return this;
  }

  public SeriesEditionDialogChecker setCustom() {
    getProfileCombo().select(ProfileType.CUSTOM.getLabel());
    return this;
  }

  public SeriesEditionDialogChecker setEveryMonth() {
    getProfileCombo().select(ProfileType.EVERY_MONTH.getLabel());
    return this;
  }

  public SeriesEditionDialogChecker checkEveryMonthSelected() {
    assertThat(getProfileCombo().selectionEquals(ProfileType.EVERY_MONTH.getLabel()));
    return this;
  }

  public SeriesEditionDialogChecker setSingleMonth() {
    getProfileCombo().select(ProfileType.SINGLE_MONTH.getLabel());
    return this;
  }

  public SeriesEditionDialogChecker checkSingleMonthSelected() {
    assertThat(getProfileCombo().selectionEquals(ProfileType.SINGLE_MONTH.getLabel()));
    return this;
  }

  public SeriesEditionDialogChecker checkSeriesListIsHidden() {
    checkSeriesListVisible(false);
    return this;
  }

  public SeriesEditionDialogChecker checkSeriesListIsVisible() {
    checkSeriesListVisible(true);
    return this;
  }

  private void checkSeriesListVisible(boolean visible) {
    checkComponentVisible(dialog, JPanel.class, "seriesListButtonPanel", visible);
    checkComponentVisible(dialog, JPanel.class, "seriesPanel", visible);
    checkComponentVisible(dialog, JButton.class, "deleteSingleSeries", !visible);
  }

  public SeriesEditionDialogChecker checkAmountIsDisabled() {
    assertFalse(getAmountTextBox().isEnabled());
    return this;
  }

  public SeriesEditionDialogChecker checkFromAccount(String account) {
    assertThat(dialog.getComboBox("fromAccount").selectionEquals(account));
    return this;
  }

  public SeriesEditionDialogChecker checkToAccount(String account) {
    assertThat(dialog.getComboBox("toAccount").selectionEquals(account));
    return this;
  }

  public SeriesEditionDialogChecker setToAccount(String account) {
    dialog.getComboBox("toAccount").select(account);
    return this;
  }

  public SeriesEditionDialogChecker setFromAccount(String account) {
    dialog.getComboBox("fromAccount").select(account);
    return this;
  }

  public SeriesEditionDialogChecker checkDateChooserIsHidden() {
    assertFalse(dialog.getComboBox("dateChooser").isVisible());
    return this;
  }

  public SeriesEditionDialogChecker setDate(String date) {
    dialog.getComboBox("dateChooser").select(date);
    return this;
  }
}
