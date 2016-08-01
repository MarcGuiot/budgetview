package com.budgetview.functests.checkers;

import com.budgetview.functests.checkers.components.HistoChartChecker;
import com.budgetview.model.Month;
import com.budgetview.model.ProfileType;
import com.budgetview.utils.Lang;
import junit.framework.Assert;
import org.uispec4j.*;
import org.uispec4j.assertion.UISpecAssert;
import org.uispec4j.finder.ComponentMatchers;
import org.uispec4j.interception.WindowHandler;
import org.uispec4j.interception.WindowInterceptor;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.uispec4j.assertion.UISpecAssert.*;

public class SeriesEditionDialogChecker extends SeriesAmountEditionChecker<SeriesEditionDialogChecker> {

  public static final String JAN = "Jan";
  public static final String FEB = "Feb";
  public static final String MAR = "Mar";
  public static final String APR = "Apr";
  public static final String MAY = "May";
  public static final String JUN = "Jun";
  public static final String JUL = "Jul";
  public static final String AUG = "Aug";
  public static final String SEP = "Sep";
  public static final String OCT = "Oct";
  public static final String NOV = "Nov";
  public static final String DEC = "Dec";

  public static SeriesEditionDialogChecker open(Button button) {
    return open(button.triggerClick());
  }

  public static SeriesEditionDialogChecker open(Trigger trigger) {
    Window dialog = WindowInterceptor.getModalDialog(trigger);
    return new SeriesEditionDialogChecker(dialog);
  }

  public SeriesEditionDialogChecker(Window dialog) {
    super(dialog);
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
    dialog.getInputTextBox("nameField").setText(seriesName, false);
    return this;
  }

  public void setNameAndValidate(String seriesName) {
    dialog.getInputTextBox("nameField").setText(seriesName, true);
    assertFalse(dialog.isVisible());
  }

  public SeriesEditionDialogChecker showDescription() {
    dialog.getButton("showDescription").click();
    return this;
  }

  public SeriesEditionDialogChecker setDescription(String text) {
    dialog.getInputTextBox("descriptionField").setText(text);
    return this;
  }

  public SeriesEditionDialogChecker checkDescription(String text) {
    assertThat(dialog.getInputTextBox("descriptionField").textEquals(text));
    return this;
  }

  public SeriesEditionDialogChecker back() {
    dialog.getButton(Lang.get("seriesEdition.backToMain")).click();
    return this;
  }

  public SeriesEditionDialogChecker selectAllMonths() {
    setPropagationEnabled();
    selectFirstMonth();
    return this;
  }

  public SeriesEditionDialogChecker selectFirstMonth() {
    getChart().clickColumn(0);
    return this;
  }

  public SeriesEditionDialogChecker selectMonth(Integer monthId) {
    getChart().clickColumnId(monthId);
    return this;
  }

  public SeriesEditionDialogChecker checkChart(Object[][] content) {
    getChart().checkContents(content);
    return this;
  }

  public SeriesEditionDialogChecker checkChartColumn(int index, String label, String section, double reference, double actual) {
    getChart().checkDiffColumn(index, label, section, reference, actual);
    return this;
  }

  public SeriesEditionDialogChecker checkChartColumn(int index, String label, String section, double reference, double actual, boolean selected) {
    getChart().checkDiffColumn(index, label, section, reference, actual, selected);
    return this;
  }

  public SeriesEditionDialogChecker checkChartRange(int firstMonth, int lastMonth) {
    getChart().checkRange(firstMonth, lastMonth);
    return this;
  }

  public SeriesEditionDialogChecker scroll(int shift) {
    getChart().scroll(shift);
    return this;
  }

  public SeriesEditionDialogChecker checkMonthSelectorsVisible(boolean visible) {
    checkComponentVisible(dialog, JPanel.class, "monthSelectionPanel", visible);
    return this;
  }

  public SeriesEditionDialogChecker checkMonthSelected(int monthId) {
    getChart().checkSelectedIds(monthId);
    return this;
  }

  public SeriesEditionDialogChecker checkSelectedMonths(Integer... monthIds) {
    getChart().checkSelectedIds(monthIds);
    return this;
  }

  public SeriesEditionDialogChecker toggleMonth(int... months) {
    String labels[] = new String[months.length];
    for (int i = 0; i < months.length; i++) {
      labels[i] = getMonthLabel(months[i]);
    }
    return toggleMonth(labels);
  }

  public SeriesEditionDialogChecker setPeriodMonths(Integer... months) {
    List selected = Arrays.asList(months);
    for (int month = 1; month < 13; month++) {
      CheckBox checkBox = getPeriodMonthCheckBox(getMonthLabel(month));
      if (selected.contains(month)) {
        checkBox.select();
      }
      else if (checkBox.isEnabled().isTrue()) {
        checkBox.unselect();
      }
    }
    return this;
  }

  private String getMonthLabel(int month) {
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
        return AUG;
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
        getPeriodMonthCheckBox(monthLabel).click();
      }
      catch (ItemNotFoundException e) {
        throw new RuntimeException("No component found for: " + monthLabel, e);
      }
    }
    return this;
  }

  public SeriesEditionDialogChecker checkMonthIsChecked(int... months) {
    for (int month : months) {
      assertThat(getMonthLabel(month) + " is not checked - " + getPeriodMonthStatuses(),
                 getPeriodMonthCheckBox(getMonthLabel(month)).isSelected());
    }
    return this;
  }

  public SeriesEditionDialogChecker checkMonthIsNotChecked(int... months) {
    for (int month : months) {
      assertFalse(getMonthLabel(month) + " is checked - " + getPeriodMonthStatuses(),
                  getPeriodMonthCheckBox(getMonthLabel(month)).isSelected());
    }
    return this;
  }

  public SeriesEditionDialogChecker checkMonthsEnabled(String... monthsLabel) {
    for (String monthLabel : monthsLabel) {
      assertThat(monthLabel + " is disabled - " + getPeriodMonthStatuses(),
                 getPeriodMonthCheckBox(monthLabel).isEnabled());
    }
    return this;
  }

  public SeriesEditionDialogChecker checkMonthsDisabled(String... monthsLabel) {
    for (String monthLabel : monthsLabel) {
      assertFalse(monthLabel + " is enabled - " + getPeriodMonthStatuses(),
                  getPeriodMonthCheckBox(monthLabel).isEnabled());
    }
    return this;
  }

  private CheckBox getPeriodMonthCheckBox(String monthLabel) {
    return dialog.getCheckBox(ComponentMatchers.componentLabelFor(monthLabel));
  }

  private String getPeriodMonthStatuses() {
    StringBuilder builder = new StringBuilder();
    builder.append("Selection: ");
    for (int month = 1; month < 13; month++) {
      String label = getMonthLabel(month);
      CheckBox checkBox = getPeriodMonthCheckBox(label);
      if (checkBox.isSelected().isTrue()) {
        builder.append(label).append(" ");
      }
    }
    return builder.toString();
  }

  public void validate() {
    dialog.getButton(Lang.get("ok")).click();
    checkClosed();
  }

  public Trigger triggerValidate() {
    return dialog.getButton(Lang.get("ok")).triggerClick();
  }

  public SeriesEditionDialogChecker validateAndCheckNameError(String message) {
    dialog.getButton(Lang.get("ok")).click();
    checkTipVisible(dialog, getNameBox(), message);
    checkVisible();
    return this;
  }

  public void cancel() {
    dialog.getButton(Lang.get("cancel")).click();
    checkClosed();
  }

  public void deleteCurrentSeries() {
    dialog.getButton(Lang.get("transaction.delete.action")).click();
    assertFalse(dialog.isVisible());
  }

  public SeriesDeletionDialogChecker openDelete() {
    return SeriesDeletionDialogChecker.init(dialog.getButton("Delete...").triggerClick());
  }

  public void deleteCurrentSeriesWithoutConfirmation() {
    dialog.getButton("Delete...").click();
    assertFalse(dialog.isVisible());
  }

  public void deleteCurrentSeriesWithConfirmation(String seriesName) {
    openDelete()
      .checkExistingTransactionsMessage(seriesName)
      .uncategorize();
    assertFalse(dialog.isVisible());
  }

  public void deleteSavingsSeriesWithConfirmation() {
    ConfirmationDialogChecker.open(dialog.getButton("Delete...").triggerClick())
      .checkMessageContains("Some operations have been associated")
      .validate();
    assertFalse(dialog.isVisible());
  }

  public SeriesEditionDialogChecker deleteSavingsSeriesWithConfirmationAndCancel() {
    ConfirmationDialogChecker.open(dialog.getButton("Delete...").triggerClick())
      .checkMessageContains("Some operations have been associated")
      .cancel();
    assertTrue(dialog.isVisible());
    return this;
  }

  public void deleteCurrentSeriesWithConfirmation() {
    openDelete()
      .checkExistingTransactionsMessage()
      .uncategorize();
    assertFalse(dialog.isVisible());
  }

  public SeriesEditionDialogChecker deleteCurrentSeriesWithConfirmationAndCancel() {
    openDelete()
      .checkExistingTransactionsMessage()
      .cancel();
    assertTrue(dialog.isVisible());
    return this;
  }

  public SeriesEditionDialogChecker setStartDate(int monthId) {
    setDate("seriesStartDateChooser", monthId);
    return this;
  }

  public SeriesEditionDialogChecker checkNoStartDate() {
    assertThat(dialog.getButton("seriesStartDateChooser").textEquals(Lang.get("seriesEdition.begin.none")));
    return this;
  }

  public SeriesEditionDialogChecker checkStartDate(String date) {
    checkDate("seriesStartDateChooser", date);
    return this;
  }

  public SeriesEditionDialogChecker setEndDate(int date) {
    setDate("seriesEndDateChooser", date);
    return this;
  }

  public SeriesEditionDialogChecker checkNoEndDate() {
    assertThat(dialog.getButton("seriesEndDateChooser").textEquals(Lang.get("seriesEdition.end.none")));
    return this;
  }

  public SeriesEditionDialogChecker checkEndDate(String monthId) {
    checkDate("seriesEndDateChooser", monthId);
    return this;
  }

  public SeriesEditionDialogChecker setSingleMonthDate(int monthId) {
    setDate("singleMonthChooser", monthId);
    return this;
  }

  public SeriesEditionDialogChecker checkSingleMonth(String date) {
    checkDate("singleMonthChooser", date);
    checkComponentVisible(dialog, JButton.class, "seriesStartDateChooser", false);
    checkComponentVisible(dialog, JButton.class, "seriesEndDateChooser", false);
    return this;
  }

  private void setDate(String componentName, int monthId) {
    MonthChooserChecker month = getMonthChooser(componentName);
    month.centerOn(monthId)
      .selectMonthInCurrent(Month.toMonth(monthId));
  }

  private void clearDate(String componentName) {
    MonthChooserChecker month = getMonthChooser(componentName);
    month.selectNone();
  }

  public MonthChooserChecker editStartDate() {
    return getMonthChooser("seriesStartDateChooser");
  }

  public MonthChooserChecker editEndDate() {
    return getMonthChooser("seriesEndDateChooser");
  }

  public SeriesEditionDialogChecker clearStartDate() {
    clearDate("seriesStartDateChooser");
    return this;
  }

  public SeriesEditionDialogChecker clearEndDate() {
    clearDate("seriesEndDateChooser");
    return this;
  }

  private MonthChooserChecker getMonthChooser(String labelName) {
    return MonthChooserChecker.open(dialog.getButton(labelName).triggerClick());
  }

  private void checkDate(String labelName, String text) {
    assertThat(dialog.getButton(labelName).textEquals(text));
  }

  public SeriesEditionDialogChecker checkVisible() {
    assertTrue(dialog.isVisible());
    return this;
  }

  public void checkClosed() {
    assertFalse(dialog.isVisible());
  }

  public SeriesEditionDialogChecker checkOkEnabled(boolean isEnabled) {
    UISpecAssert.assertEquals(isEnabled ? "ok is disabled" : "ok is enabled",
                              isEnabled, dialog.getButton("ok").isEnabled());
    return this;

  }

  public SeriesEditionDialogChecker checkNameIsSelected() {
    JTextField textEditor = (JTextField) dialog.getInputTextBox("nameField").getAwtComponent();
    Assert.assertEquals(textEditor.getText(), textEditor.getSelectedText());
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

  private ComboBox getProfileCombo() {
    return dialog.getComboBox("profileCombo");
  }

  public SeriesEditionDialogChecker checkMonthsAreHidden() {
    assertFalse(dialog.getPanel("monthSelectionPanel").isVisible());
    return this;
  }

  public SeriesEditionDialogChecker checkMonthsAreVisible() {
    assertTrue(dialog.getPanel("monthSelectionPanel").isVisible());
    return this;
  }

  public SeriesEditionDialogChecker setRepeatIrregular() {
    getProfileCombo().select(ProfileType.IRREGULAR.getLabel());
    return this;
  }

  public SeriesEditionDialogChecker setRepeatEverySixMonths() {
    getProfileCombo().select(ProfileType.SIX_MONTHS.getLabel());
    return this;
  }

  public SeriesEditionDialogChecker setRepeatEveryTwoMonths() {
    getProfileCombo().select(ProfileType.TWO_MONTHS.getLabel());
    return this;
  }

  public SeriesEditionDialogChecker setRepeatCustom() {
    getProfileCombo().select(ProfileType.CUSTOM.getLabel());
    return this;
  }

  public SeriesEditionDialogChecker setRepeatEveryMonth() {
    getProfileCombo().select(ProfileType.EVERY_MONTH.getLabel());
    return this;
  }

  public SeriesEditionDialogChecker setRepeatOnceAYear() {
    getProfileCombo().select(ProfileType.ONCE_A_YEAR.getLabel());
    return this;
  }

  public SeriesEditionDialogChecker setRepeatSingleMonth() {
    getProfileCombo().select(ProfileType.SINGLE_MONTH.getLabel());
    return this;
  }

  public SeriesEditionDialogChecker checkRepeatsEveryMonth() {
    assertThat(getProfileCombo().selectionEquals(ProfileType.EVERY_MONTH.getLabel()));
    return this;
  }

  public SeriesEditionDialogChecker checkRepeatsEveryTwoMonths() {
    assertThat(getProfileCombo().selectionEquals(ProfileType.TWO_MONTHS.getLabel()));
    return this;
  }

  public SeriesEditionDialogChecker checkRepeatsASingleMonth() {
    assertThat(getProfileCombo().selectionEquals(ProfileType.SINGLE_MONTH.getLabel()));
    return this;
  }

  public SeriesEditionDialogChecker checkRepeatsIrregularly() {
    assertThat(getProfileCombo().selectionEquals(ProfileType.IRREGULAR.getLabel()));
    return this;
  }

  public SeriesEditionDialogChecker checkRepeatsWithCustomPattern() {
    assertThat(getProfileCombo().selectionEquals(ProfileType.CUSTOM.getLabel()));
    return this;
  }

  public SeriesEditionDialogChecker checkAmountLabel(final String text) {
    assertThat(dialog.getPanel("seriesAmountEditionPanel").getTextBox("dateLabel").textEquals(text));
    return this;
  }

  public AccountEditionChecker createAccount() {
    Button button = dialog.getButton("createAccount");
    assertThat(button.isVisible());
    return AccountEditionChecker.open(button.triggerClick());
  }

  public SeriesEditionDialogChecker checkFromAccount(String account) {
    assertThat(dialog.getComboBox("fromAccount").selectionEquals(account));
    return this;
  }

  public SeriesEditionDialogChecker checkAccountsComboAreHidden() {
    assertFalse(dialog.getComboBox("fromAccount").isVisible());
    assertFalse(dialog.getComboBox("toAccount").isVisible());
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

  public SeriesEditionDialogChecker checkToContentEquals(String... name) {
    assertTrue(dialog.getComboBox("toAccount").contentEquals(name));
    return this;
  }

  public SeriesEditionDialogChecker setFromAccount(String account) {
    dialog.getComboBox("fromAccount").select(account);
    return this;
  }

  public SeriesEditionDialogChecker checkFromContentEquals(String... name) {
    assertTrue(dialog.getComboBox("fromAccount").contentEquals(name));
    return this;
  }

  public SeriesEditionDialogChecker checkDateChooserIsHidden() {
    assertFalse(dialog.getComboBox("dayChooser").isVisible());
    return this;
  }

  public SeriesEditionDialogChecker setDay(String day) {
    dialog.getComboBox("dayChooser").select(day);
    return this;
  }

  public SeriesEditionDialogChecker checkSavingsMessageVisibility(boolean isVisible) {
    if (isVisible) {
      assertTrue(dialog.getTextBox("savingsMessage").textEquals("At least one of the accounts must be an existing account"));
      assertTrue(dialog.getTextBox("savingsMessage").isVisible());
    }
    else {
      assertFalse(dialog.getTextBox("savingsMessage").isVisible());
    }
    return this;
  }

  public SeriesEditionDialogChecker editSubSeries() {
    dialog.getButton("showSubSeries").click();
    return this;
  }

  public SeriesEditionDialogChecker checkAddSubSeriesEnabled(boolean enabled) {
    assertEquals(enabled, dialog.getButton("Add").isEnabled());
    return this;
  }

  public SeriesEditionDialogChecker checkAddSubSeriesTextIsEmpty() {
    assertThat(dialog.getInputTextBox("subSeriesNameField").textIsEmpty());
    return this;
  }

  public SeriesEditionDialogChecker checkSubSeriesMessage(String message) {
    TextBox messageBox = dialog.getTextBox("subSeriesErrorMessage");
    assertThat(messageBox.textEquals(message));
    assertThat(messageBox.isVisible());
    return this;
  }

  public SeriesEditionDialogChecker checkNoSubSeriesMessage() {
    TextBox messageBox = dialog.getTextBox("subSeriesErrorMessage");
    assertFalse(messageBox.isVisible());
    return this;
  }

  public SeriesEditionDialogChecker enterSubSeriesName(String name) {
    dialog.getInputTextBox("subSeriesNameField").setText(name, false);
    return this;
  }

  public SeriesEditionDialogChecker selectSubSeries(String name) {
    dialog.getListBox().select(name);
    return this;
  }

  public SeriesEditionDialogChecker addSubSeries(String name) {
    dialog.getInputTextBox("subSeriesNameField").setText(name, false);
    dialog.getButton("Add").click();
    assertFalse(dialog.getTextBox("subSeriesErrorMessage").isVisible());
    assertThat(dialog.getListBox().contains(name));
    return this;
  }

  public SeriesEditionDialogChecker addSubSeries() {
    dialog.getButton("Add").click();
    return this;
  }

  public SeriesEditionDialogChecker checkSubSeriesList(String... names) {
    assertThat(dialog.getListBox().contentEquals(names));
    return this;
  }

  public SeriesEditionDialogChecker checkSubSeriesListIsEmpty() {
    assertThat(dialog.getListBox().isEmpty());
    return this;
  }

  public SeriesEditionDialogChecker renameSubSeries(String previousName, final String newName) {
    dialog.getListBox().select(previousName);
    WindowInterceptor.init(dialog.getButton("renameSubSeries"))
      .process(new WindowHandler() {
        public Trigger process(Window window) throws Exception {
          window.getInputTextBox().setText(newName);
          return window.getButton("OK").triggerClick();
        }
      })
      .run();
    return this;
  }

  public SeriesEditionDialogChecker checkRenameSubSeriesMessage(String subSeriesName,
                                                                final String newName,
                                                                final String errorMessage) {
    dialog.getListBox().select(subSeriesName);
    WindowInterceptor.init(dialog.getButton("renameSubSeries"))
      .process(new WindowHandler() {
        public Trigger process(Window window) throws Exception {
          window.getInputTextBox().setText(newName, false);
          window.getButton("OK").click();
          assertThat(window.isVisible());
          TextBox messageLabel = window.getTextBox("messageLabel");
          assertThat(messageLabel.isVisible());
          assertThat(messageLabel.textEquals(errorMessage));
          return window.getButton("Cancel").triggerClick();
        }
      })
      .run();
    return this;
  }

  public SeriesEditionDialogChecker deleteSubSeries(String... names) {
    dialog.getListBox().select(names);
    dialog.getButton("deleteSubSeries").click();
    return this;
  }

  public SeriesEditionDialogChecker deleteSubSeriesAndConfirm(String... names) {
    dialog.getListBox().select(names);
    DeleteSubSeriesDialogChecker.open(dialog.getButton("deleteSubSeries").triggerClick()).validate();
    return this;
  }

  public DeleteSubSeriesDialogChecker deleteSubSeriesWithConfirmation(String... names) {
    dialog.getListBox().select(names);
    return DeleteSubSeriesDialogChecker.open(dialog.getButton("deleteSubSeries").triggerClick());
  }

  public SeriesEditionDialogChecker checkBudgetArea(String budgetAreaName) {
    assertThat(dialog.getComboBox("budgetAreaChooser").selectionEquals(budgetAreaName));
    return this;
  }

  public SeriesEditionDialogChecker changeBudgetArea(String budgetArea) {
    dialog.getComboBox("budgetAreaChooser").select(budgetArea);
    return this;
  }

  public SeriesEditionDialogChecker checkBudgetAreaContent() {
    assertThat(dialog.getComboBox("budgetAreaChooser").contentEquals("Recurring", "Variable", "Extras"));
    return this;
  }

  public SeriesEditionDialogChecker checkBudgetAreaIsHidden() {
    assertFalse(dialog.getComboBox("budgetAreaChooser").isVisible());
    return this;
  }

  public SeriesEditionDialogChecker checkMainPanelShown() {
    assertThat(dialog.containsSwingComponent(JPanel.class, "chart"));
    return this;
  }

  public SeriesEditionDialogChecker setAutomaticForecast() {
    getForceSingleOperationCombo().select(Lang.get("seriesEdition.forecast.mode.auto"));
    return this;
  }

  public SeriesEditionDialogChecker setSingleOperationForecast() {
    getForceSingleOperationCombo().select(Lang.get("seriesEdition.forecast.mode.single"));
    return this;
  }

  public SeriesEditionDialogChecker checkAutomaticForecastSelected() {
    assertThat(getForceSingleOperationCombo().selectionEquals(Lang.get("seriesEdition.forecast.mode.auto")));
    return this;
  }

  public SeriesEditionDialogChecker checkSingleOperationForecastSelected() {
    assertThat(getForceSingleOperationCombo().selectionEquals(Lang.get("seriesEdition.forecast.mode.single")));
    return this;
  }

  public SeriesEditionDialogChecker checkSingleOperationForecastDayShown(boolean enabled) {
    assertEquals(enabled, getForceSingleOperationDayCombo().isVisible());
    return this;
  }

  public SeriesEditionDialogChecker checkForceSingleOperationDay(int day) {
    assertThat(getForceSingleOperationDayCombo().selectionEquals(Integer.toString(day)));
    return this;
  }

  public SeriesEditionDialogChecker setForceSingleOperationDay(int day) {
    getForceSingleOperationDayCombo().select(Integer.toString(day));
    return this;
  }

  private ComboBox getForceSingleOperationCombo() {
    return dialog.getComboBox("forecastModeCombo");
  }

  public SeriesEditionDialogChecker checkForceSingleOperationDayList(Integer[] values) {
    List<String> content = new ArrayList<String>();
    for (Integer value : values) {
      content.add(Integer.toString(value));
    }
    assertThat(getForceSingleOperationDayCombo().contentEquals(content.toArray(new String[content.size()])));
    return this;
  }

  private ComboBox getForceSingleOperationDayCombo() {
    return dialog.getComboBox("forecastDayCombo");
  }

  private HistoChartChecker getChart() {
    return new HistoChartChecker(dialog, "seriesAmountEditionPanel", "chart");
  }

  public SeriesEditionDialogChecker checkNoTipShown() {
    checkNoTipVisible(dialog);
    return this;
  }

  public SeriesEditionDialogChecker setTargetAccount(String account) {
    dialog.getComboBox("targetAccountCombo").select(account);
    assertFalse(dialog.getTextBox("targetAccountLabel").isVisible());
    return this;
  }

  public SeriesEditionDialogChecker checkEditableTargetAccount(String account) {
    ComboBox combo = dialog.getComboBox("targetAccountCombo");
    assertThat(combo.selectionEquals(account));
    assertThat(combo.isVisible());
    return this;
  }

  public SeriesEditionDialogChecker checkAvailableTargetAccounts(String... accounts) {
    ComboBox combo = dialog.getComboBox("targetAccountCombo");
    assertThat(combo.contentEquals(accounts));
    assertThat(combo.isVisible());
    return this;
  }

  public SeriesEditionDialogChecker checkReadOnlyTargetAccount(String accountName) {
    checkComponentVisible(dialog, JComboBox.class, "targetAccountCombo", false);
    TextBox label = dialog.getTextBox("targetAccountLabel");
    assertTrue(label.textEquals(accountName));
    assertTrue(label.isVisible());
    return this;
  }

  public SeriesEditionDialogChecker checkTargetAccountNotShown() {
    checkComponentVisible(dialog, JComboBox.class, "targetAccountCombo", false);
    checkComponentVisible(dialog, JLabel.class, "targetAccountLabel", false);
    return this;
  }
}
