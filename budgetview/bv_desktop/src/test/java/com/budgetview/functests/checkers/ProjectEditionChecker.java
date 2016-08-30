package com.budgetview.functests.checkers;

import com.budgetview.functests.checkers.components.GaugeChecker;
import com.budgetview.functests.checkers.components.MonthSliderChecker;
import com.budgetview.functests.checkers.components.PopupButton;
import com.budgetview.functests.checkers.components.PopupChecker;
import com.budgetview.desktop.description.Formatting;
import com.budgetview.shared.model.BudgetArea;
import com.budgetview.utils.Lang;
import junit.framework.Assert;
import org.globsframework.utils.TablePrinter;
import org.uispec4j.MenuItem;
import org.uispec4j.*;
import org.uispec4j.Panel;
import org.uispec4j.Window;
import org.uispec4j.interception.FileChooserHandler;
import org.uispec4j.interception.PopupMenuInterceptor;
import org.uispec4j.interception.WindowInterceptor;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

import static org.uispec4j.assertion.UISpecAssert.*;

public class ProjectEditionChecker extends ViewChecker {

  private Panel panel;

  public ProjectEditionChecker(Window mainWindow) {
    super(mainWindow);
  }

  public ProjectEditionChecker checkTitle(String text) {
    assertThat(getPanel().getTextBox("title").textEquals(text));
    return this;
  }

  public ProjectEditionChecker setDefaultAccount(String accountName) {
    getPanel().getComboBox("accountSelection").select(accountName);
    return this;
  }

  public ProjectEditionChecker checkDefaultAccountLabel(String accountName) {
    TextBox accountLabel = getPanel().getTextBox("accountLabel");
    assertThat(accountLabel.textEquals(accountName));
    assertThat(accountLabel.isVisible());
    checkComponentVisible(getPanel(), JComboBox.class, "accountSelection", false);
    return this;
  }

  public ProjectEditionChecker setNameAndValidate(String name) {
    getPanel().getTextBox("projectName").setText(name);
    return this;
  }

  public ProjectEditionChecker checkName(String text) {
    assertThat(getPanel().getButton("projectNameButton").textEquals(text));
    return this;
  }

  public ProjectEditionChecker edit() {
    getPopup().click(Lang.get("rename"));
    return this;
  }

  public ProjectEditionChecker clearName() {
    getPanel().getPanel("projectEditor").getInputTextBox("projectNameField").setText("", false);
    return this;
  }

  public ProjectEditionChecker checkNameEditionInProgress(String name) {
    Panel editor = getPanel().getPanel("projectEditor");
    assertThat(editor.isVisible());
    assertThat(editor.getInputTextBox("projectNameField").textEquals(name));
    return this;
  }

  public ProjectEditionChecker checkNoEditionInProgress() {
    checkComponentVisible(getPanel(), JTextField.class, "projectNameField", false);
    return this;
  }

  public ProjectEditionChecker checkProjectNameMessage(String expectedMessage) {
    Panel editor = getPanel().getPanel("projectEditor");
    TextBox nameField = editor.getTextBox("projectNameField");
    assertThat(nameField.isVisible());
    editor.getButton("Validate").click();
    checkTipVisible(getPanel(), nameField, expectedMessage);
    assertThat(editor.getButton("Validate").isVisible());
    return this;
  }

  public ProjectEditionChecker cancelEdition() {
    getPanel().getPanel("projectEditor").getButton("cancel").click();
    return this;
  }

  public ProjectEditionChecker setImage(String path) {
    views.selectProjects();
    PopupChecker checker = new PopupChecker() {
      protected MenuItem openMenu() {
        TextBox imageLabel = getPanel().getTextBox("imageLabel");
        return PopupMenuInterceptor.run(Mouse.triggerRightClick(imageLabel));
      }
    };
    WindowInterceptor
      .init(checker.triggerClick(Lang.get("imageLabel.actions.browse")))
      .process(FileChooserHandler.init()
                 .assertAcceptsFilesOnly()
                 .select(path))
      .run();
    return this;
  }

  public void setActive() {
    ToggleButton toggle = getActiveToggle();
    assertThat(toggle.isEnabled());
    toggle.click();
  }

  public void setInactive() {
    ToggleButton toggle = getActiveToggle();
    assertThat(toggle.isEnabled());
    toggle.click();
  }

  private ToggleButton getActiveToggle() {
    return getPanel().getPanel("projectPanel").getToggleButton("activeToggle");
  }

  public ProjectEditionChecker checkProjectGaugeHidden() {
    assertFalse(getPanel().getPanel("gaugePanel").isVisible());
    return this;
  }

  public ProjectEditionChecker checkProjectButtonsHidden() {
    assertFalse(getPanel().getToggleButton("activeToggle").isVisible());
    return this;
  }

  public ProjectEditionChecker checkProjectButtonsShown() {
    assertTrue(getPanel().getToggleButton("activeToggle").isVisible());
    return this;
  }

  public ProjectEditionChecker checkProjectGauge(double actual, double planned) {

    assertThat(getPanel().getTextBox("totalActual").textEquals(Formatting.toString(actual, BudgetArea.EXTRAS)));
    assertThat(getPanel().getTextBox("totalPlanned").textEquals(Formatting.toString(planned, BudgetArea.EXTRAS)));

    GaugeChecker gauge = new GaugeChecker(getPanel(), "gauge");
    gauge.checkActualValue(actual);
    gauge.checkTargetValue(planned);
    return this;
  }

  public ProjectEditionChecker checkItem(int index, String label, String month, double actual, double planned) {
    ProjectItemViewChecker itemPanel = view(index);
    itemPanel.checkValues(label, month, actual, planned);
    return this;
  }

  public ProjectEditionChecker checkItemCount(int count) {
    Component[] panels = getPanel().getSwingComponents(JPanel.class, "projectItemPanel");
    if (panels.length != count) {
      Assert.fail("Unexpected item count: expected " + count + " but was " + panels.length +
                  " - labels: " + Arrays.asList(getItemLabels()));
    }
    return this;
  }

  public ProjectEditionChecker checkItems(String expected) {
    Assert.assertEquals(expected.trim(), getContent());
    return this;
  }

  public ProjectEditionChecker deleteItem(int index) {
    views.selectProjects();
    view(index).delete();
    return this;
  }

  public ProjectEditionChecker addExpenseItem() {
    getPanel().getButton("addExpenseItem").click();
    return this;
  }

  public ProjectEditionChecker addExpenseItem(int index, String label, int firstMonth, double amount) {
    return addExpenseItem(index, label, firstMonth, amount, 1, null);
  }

  public ProjectEditionChecker addExpenseItem(int index, String label, int firstMonth, double amount, String account) {
    return addExpenseItem(index, label, firstMonth, amount, 1, account);
  }

  public ProjectEditionChecker addExpenseItem(int index, String label, int firstMonth, double amount, int numberOfMonths) {
    return addExpenseItem(index, label, firstMonth, amount, numberOfMonths, null);
  }

  public ProjectEditionChecker addExpenseItem(int index, String label, int firstMonth, double amount, int numberOfMonths, String account) {
    addExpenseItem();
    ProjectItemExpenseEditionChecker projectItemEditionChecker = editExpense(index)
      .setLabel(label)
      .setMonth(firstMonth)
      .setAmount(amount);
    if (account != null) {
      projectItemEditionChecker.setTargetAccount(account);
    }
    if (numberOfMonths != 1) {
      projectItemEditionChecker
        .switchToSeveralMonths()
        .setMonthCount(numberOfMonths);
    }
    projectItemEditionChecker
      .validate();
    return this;
  }

  public ProjectEditionChecker addTransferItem() {
    getPanel().getButton("addTransferItem").click();
    return this;
  }

  public ProjectEditionChecker addTransferItem(int index, String label, int monthId, double amount, String fromAccount, String toAccount) {
    addTransferItem();
    editTransfer(index)
      .setLabel(label)
      .setAmount(amount)
      .setMonth(monthId)
      .setFromAccount(fromAccount)
      .setToAccount(toAccount)
      .validate();
    return this;
  }

  public ProjectEditionChecker addTransferItem(int index, String label, double amount, String fromAccount, String toAccount) {
    addTransferItem();
    editTransfer(index)
      .setLabel(label)
      .setAmount(amount)
      .setFromAccount(fromAccount)
      .setToAccount(toAccount)
      .validate();
    return this;
  }

  public ProjectEditionChecker checkItemGauge(int index, double actual, double planned) {
    view(index).checkGauge(actual, planned);
    return this;
  }

  public ProjectEditionChecker cancelExpenseEdition(int i) {
    editExpense(i).cancel();
    return this;
  }

  private String getContent() {
    checkNoEditedItems();
    TablePrinter printer = new TablePrinter();
    Component[] panels = getPanel().getSwingComponents(JPanel.class, "projectItemPanel");
    for (int i = 0; i < panels.length; i++) {
      view(i).write(printer);
    }
    return printer.toString().trim();
  }

  private void checkNoEditedItems() {
    java.util.List<String> labels = new ArrayList<String>();
    Component[] editorPanels = getPanel().getSwingComponents(JPanel.class, "projectItemEditionPanel");
    for (Component editorPanel : editorPanels) {
      Panel panel1 = new Panel((JPanel) editorPanel);
      labels.add(panel1.getTextBox("nameField").getText());
    }
    if (labels.size() > 0) {
      Assert.fail("Unexpected edited items: " + labels);
    }
  }

  private String[] getItemLabels() {
    java.util.List<String> labels = new ArrayList<String>();
    for (Component jPanel : getPanel().getSwingComponents(JPanel.class, "projectItemViewPanel")) {
      Panel viewPanel = new Panel((JPanel) jPanel);
      labels.add(viewPanel.getButton("itemButton").getLabel());
    }
    for (Component jPanel : getPanel().getSwingComponents(JPanel.class, "projectItemEditionPanel")) {
      Panel editorPanel = new Panel((JPanel) jPanel);
      labels.add(editorPanel.getTextBox("nameField").getText());
    }
    return labels.toArray(new String[labels.size()]);
  }

  public ProjectEditionChecker checkNoErrorTipDisplayed() {
    super.checkNoTipVisible(getPanel());
    return this;
  }

  public void delete() throws Exception {
    getDeleteTrigger().run();
  }

  public void deleteWithConfirmation(String title, String message) {
    ConfirmationDialogChecker.open(getDeleteTrigger())
      .checkTitle(title)
      .checkMessageContains(message)
      .validate("Delete project");
  }

  public void openDeleteAndNavigate() {
    ConfirmationDialogChecker.open(getDeleteTrigger())
      .clickOnHyperlink("see these operations")
      .checkHidden();
  }

  private Trigger getDeleteTrigger() {
    views.selectProjects();
    PopupButton button = getPopup();
    return button.triggerClick("Delete");
  }

  public ProjectEditionChecker setFirstMonth(int monthId) {
    MonthSliderChecker.init(getProjectPanel(), "monthSlider").setMonth(monthId);
    return this;
  }

  public ProjectEditionChecker slideToNextMonth() {
    MonthSliderChecker.init(getProjectPanel(), "monthSlider").next();
    return this;
  }

  public ProjectEditionChecker checkPeriod(String period) {
    MonthSliderChecker.init(getProjectPanel(), "monthSlider").checkText(period);
    return this;
  }

  public ProjectEditionChecker checkPeriodHidden() {
    MonthSliderChecker.init(getProjectPanel(), "monthSlider").checkHidden();
    return this;
  }

  public ProjectItemExpenseEditionChecker editExpense(int index) {
    return new ProjectItemExpenseEditionChecker(getItemPanel(index));
  }

  public ProjectItemExpenseEditionChecker toggleAndEditExpense(int index) {
    view(index).modify();
    return editExpense(index);
  }

  public ProjectItemTransferEditionChecker editTransfer(int index) {
    return new ProjectItemTransferEditionChecker(getItemPanel(index));
  }

  public ProjectItemTransferEditionChecker toggleAndEditTransfer(int index) {
    view(index).modify();
    return editTransfer(index);
  }

  public ProjectItemViewChecker view(int index) {
    return new ProjectItemViewChecker(getItemPanel(index));
  }

  private Panel getPanel() {
    if (panel == null) {
      views.selectProjects();
      panel = mainWindow.getPanel("projectEditionView");
    }
    return panel;
  }

  private Panel getProjectPanel() {
    return getPanel().getPanel("projectPanel");
  }

  private Panel getItemPanel(int index) {
    Component[] panels = getPanel().getSwingComponents(JPanel.class, "projectItemPanel");
    return new Panel((JPanel) panels[index]);
  }

  public void backToList() {
    getPanel().getButton("backToList").click();
  }

  public void sortItems() {
    getPopup().click(Lang.get("projectEdition.sortItems"));
  }

  public ProjectDuplicationDialogChecker openDuplicate() {
    return ProjectDuplicationDialogChecker.open(getPopup().triggerClick(Lang.get("projectEdition.duplicate.menu")));
  }

  public PopupButton getPopup() {
    views.selectProjects();
    final org.uispec4j.Button itemButton = getPanel().getButton("nameButton");
    return new PopupButton(itemButton);
  }

  public void checkDuplicateDisabled() {
    getPopup().checkItemDisabled(Lang.get("projectEdition.duplicate.menu"));
  }

  public AccountEditionChecker checkAddExpenseWithNoAccount() {
    return AccountEditionChecker.open(ConfirmationDialogChecker.open(getPanel().getButton("addExpenseItem").triggerClick())
                                        .checkMessageContains("In order to prepare projects, you must first create a main bank account")
                                        .getOkTrigger("Create an account"));
  }

  public AccountEditionChecker checkAddTransferItemWithNoAccount() {
    return AccountEditionChecker.open(ConfirmationDialogChecker.open(getPanel().getButton("addTransferItem").triggerClick())
                                        .checkMessageContains("In order to prepare projects, you must first create a main bank account")
                                        .getOkTrigger("Create an account"));
  }
}
