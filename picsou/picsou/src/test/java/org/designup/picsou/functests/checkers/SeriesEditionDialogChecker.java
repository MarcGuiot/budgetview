package org.designup.picsou.functests.checkers;

import junit.framework.Assert;
import org.designup.picsou.functests.checkers.components.HistoChartChecker;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.ProfileType;
import org.designup.picsou.utils.Lang;
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

  public SeriesEditionDialogChecker toggleAutoReport() {
    dialog.getCheckBox("autoReport").select();
    return this;
  }

  public SeriesEditionDialogChecker unToggleAutoReport() {
    dialog.getCheckBox("autoReport").unselect();
    return this;
  }

  public SeriesEditionDialogChecker setDescription(String text) {
    dialog.getTabGroup().selectTab("Description");
    dialog.getInputTextBox("descriptionField").setText(text);
    return this;
  }

  public SeriesEditionDialogChecker checkDescription(String text) {
    assertThat(dialog.getInputTextBox("descriptionField").textEquals(text));
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

  public SeriesEditionDialogChecker checkMonthSelectorsVisible(boolean visible) {
    checkComponentVisible(dialog, JPanel.class, "monthSelectionPanel", visible);
    return this;
  }

  public SeriesEditionDialogChecker checkMonthSelected(int monthId) {
    getChart().checkSelectedIds(monthId);
    return this;
  }

  public SeriesEditionDialogChecker checkMonthsSelected(Integer... monthIds) {
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
    StringBuffer buffer = new StringBuffer();
    buffer.append("Selection: ");
    for (int month = 1; month < 13; month++) {
      String label = getMonthLabel(month);
      CheckBox checkBox = getPeriodMonthCheckBox(label);
      if (checkBox.isSelected().isTrue()) {
        buffer.append(label).append(" ");
      }
    }
    return buffer.toString();
  }

  public void validate() {
    dialog.getButton(Lang.get("ok")).click();
    checkClosed();
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

  public SeriesDeletionDialogChecker openDeleteDialog() {
    return SeriesDeletionDialogChecker.init(dialog.getButton("Delete...").triggerClick());
  }

  public void deleteCurrentSeriesWithConfirmation(String seriesName) {
    openDeleteDialog()
      .checkMessage(seriesName)
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
    openDeleteDialog()
      .checkMessage()
      .uncategorize();
    assertFalse(dialog.isVisible());
  }

  public SeriesEditionDialogChecker deleteCurrentSeriesWithConfirmationAndCancel() {
    openDeleteDialog()
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

  public SeriesEditionDialogChecker clearStartDate() {
    dialog.getButton("deleteSeriesStartDate").click();
    return this;
  }

  public SeriesEditionDialogChecker clearEndDate() {
    dialog.getButton("deleteSeriesEndDate").click();
    return this;
  }

  private MonthChooserChecker getMonthChooser(String labelName) {
    return MonthChooserChecker.open(dialog.getButton(labelName).triggerClick());
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
    JTextField textEditor = (JTextField)dialog.getInputTextBox("nameField").getAwtComponent();
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

  public SeriesEditionDialogChecker setRepeatEveryThreeMonths() {
    getProfileCombo().select(ProfileType.THREE_MONTHS.getLabel());
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

  public SeriesEditionDialogChecker gotoSubSeriesTab() {
    dialog.getTabGroup().selectTab("Sub-series");
    return this;
  }

  public SeriesEditionDialogChecker checkAddSubSeriesEnabled(boolean enabled) {
    assertEquals(enabled, getSelectedTab().getButton("Add").isEnabled());
    return this;
  }

  public SeriesEditionDialogChecker checkAddSubSeriesTextIsEmpty() {
    assertThat(getSelectedTab().getInputTextBox("subSeriesNameField").textIsEmpty());
    return this;
  }

  public SeriesEditionDialogChecker checkSubSeriesMessage(String message) {
    TextBox messageBox = getSelectedTab().getTextBox("subSeriesErrorMessage");
    assertThat(messageBox.textEquals(message));
    assertThat(messageBox.isVisible());
    return this;
  }

  public SeriesEditionDialogChecker checkNoSubSeriesMessage() {
    TextBox messageBox = getSelectedTab().getTextBox("subSeriesErrorMessage");
    assertFalse(messageBox.isVisible());
    return this;
  }

  public SeriesEditionDialogChecker enterSubSeriesName(String name) {
    getSelectedTab().getInputTextBox("subSeriesNameField").setText(name, false);
    return this;
  }

  public SeriesEditionDialogChecker selectSubSeries(String name) {
    getSelectedTab().getListBox().select(name);
    return this;
  }

  public SeriesEditionDialogChecker addSubSeries(String name) {
    Panel tab = getSelectedTab();
    tab.getInputTextBox("subSeriesNameField").setText(name, false);
    tab.getButton("Add").click();
    assertFalse(tab.getTextBox("subSeriesErrorMessage").isVisible());
    assertThat(tab.getListBox().contains(name));
    return this;
  }

  public SeriesEditionDialogChecker addSubSeries() {
    getSelectedTab().getButton("Add").click();
    return this;
  }

  public SeriesEditionDialogChecker checkSubSeriesList(String... names) {
    assertThat(getSelectedTab().getListBox().contentEquals(names));
    return this;
  }

  public SeriesEditionDialogChecker checkSubSeriesListIsEmpty() {
    assertThat(getSelectedTab().getListBox().isEmpty());
    return this;
  }

  private Panel getSelectedTab() {
    return dialog.getTabGroup().getSelectedTab();
  }

  public SeriesEditionDialogChecker renameSubSeries(String previousName, final String newName) {
    Panel tab = getSelectedTab();
    tab.getListBox().select(previousName);
    WindowInterceptor.init(tab.getButton("renameSubSeries"))
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
    Panel tab = getSelectedTab();
    tab.getListBox().select(subSeriesName);
    WindowInterceptor.init(tab.getButton("renameSubSeries"))
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
    Panel tab = getSelectedTab();
    tab.getListBox().select(names);
    tab.getButton("deleteSubSeries").click();
    return this;
  }

  public SeriesEditionDialogChecker deleteSubSeriesAndConfirm(String... names) {
    Panel tab = getSelectedTab();
    tab.getListBox().select(names);
    DeleteSubSeriesDialogChecker.open(tab.getButton("deleteSubSeries").triggerClick()).validate();
    return this;
  }

  public DeleteSubSeriesDialogChecker deleteSubSeriesWithConfirmation(String... names) {
    Panel tab = getSelectedTab();
    tab.getListBox().select(names);
    return DeleteSubSeriesDialogChecker.open(tab.getButton("deleteSubSeries").triggerClick());
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

  public SeriesEditionDialogChecker checkMainTabIsSelected() {
    assertThat(dialog.getTabGroup().selectedTabEquals("Terms"));
    return this;
  }

  public SeriesEditionDialogChecker forceSingleOperationForecast() {
    getForceSingleOperationCheckBox().select();
    return this;
  }

  public SeriesEditionDialogChecker unselectedForceSingleOperationForecast() {
    getForceSingleOperationCheckBox().unselect();
    return this;
  }

  public SeriesEditionDialogChecker checkForceSingleOperationSelected(boolean selected) {
    assertEquals(selected, getForceSingleOperationCheckBox().isSelected());
    return this;
  }

  public SeriesEditionDialogChecker checkForceSingleOperationDayEnabled(boolean enabled) {
    assertEquals(enabled, getForceSingleOperationDayCombo().isEnabled());
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

  private CheckBox getForceSingleOperationCheckBox() {
    dialog.getTabGroup().selectTab("Forecast");
    return dialog.getCheckBox("forceSingleOperationCheckbox");
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
    dialog.getTabGroup().selectTab("Forecast");
    return dialog.getComboBox("forceSingleOperationDayCombo");
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

  public SeriesEditionDialogChecker checkTargetAccounts(String... accounts) {
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
