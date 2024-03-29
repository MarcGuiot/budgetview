package com.budgetview.functests.checkers;

import com.budgetview.functests.checkers.components.HistoDailyChecker;
import com.budgetview.functests.checkers.components.PopupButton;
import com.budgetview.desktop.accounts.components.AccountWeatherButton;
import com.budgetview.desktop.components.charts.histo.HistoChart;
import com.budgetview.desktop.components.charts.histo.HistoSelectionManager;
import com.budgetview.desktop.description.Formatting;
import com.budgetview.model.Day;
import com.budgetview.utils.Lang;
import org.globsframework.utils.Dates;
import org.globsframework.utils.TablePrinter;
import org.globsframework.utils.TestUtils;
import org.junit.Assert;
import org.uispec4j.Button;
import org.uispec4j.Panel;
import org.uispec4j.*;
import org.uispec4j.Window;
import org.uispec4j.assertion.Assertion;
import org.uispec4j.assertion.UISpecAssert;
import org.uispec4j.interception.WindowInterceptor;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

import static org.uispec4j.assertion.UISpecAssert.assertThat;

public abstract class AccountViewPanelChecker<T extends AccountViewPanelChecker> extends ViewChecker {
  protected Window mainWindow;
  protected String panelName;
  protected Panel accountsPanel;

  protected AccountViewPanelChecker(Window mainWindow, String panelName) {
    super(mainWindow);
    this.mainWindow = mainWindow;
    this.panelName = panelName;
  }

  public T checkAccounts(final String... expectedNames) {
    assertThat(new Assertion() {
      public void check() {
        org.globsframework.utils.TestUtils.assertSetEquals(getDisplayedAccounts(), expectedNames);
      }
    });
    return (T) this;
  }

  public T select(String accountName) {
    ToggleButton selectAccount = getAccountPanel(accountName).getToggleButton("selectAccount");
    UISpecAssert.assertFalse("Already selected", selectAccount.isSelected());
    selectAccount.click();
    return (T) this;
  }

  public T unselect(String accountName) {
    ToggleButton selectAccount = getAccountPanel(accountName).getToggleButton("selectAccount");
    assertThat("Not selected", selectAccount.isSelected());
    selectAccount.click();
    return (T) this;
  }

  public void checkNoAccountsSelected() {
    TestUtils.assertEmpty(getSelectedAccounts());
  }

  public void checkSelectedAccounts(String... accountNames) {
    List<String> actual = getSelectedAccounts();
    TestUtils.assertSetEquals(actual, accountNames);
  }

  private List<String> getSelectedAccounts() {
    Panel accountsPanel = getPanel();
    UIComponent[] toggles = accountsPanel.getUIComponents(ToggleButton.class, "selectAccount");
    List<String> actual = new ArrayList<String>();
    for (UIComponent toggle : toggles) {
      if (((ToggleButton) toggle).isSelected().isTrue()) {
        actual.add(toggle.getContainer("accountPanel").getButton("editAccount").getLabel());
      }
    }
    return actual;
  }

  private Set<String> getDisplayedAccounts() {
    UIComponent[] uiComponents = getPanel().getUIComponents(Button.class, "editAccount");
    Set<String> existingNames = new HashSet<String>();
    for (UIComponent uiComponent : uiComponents) {
      Button button = (Button) uiComponent;
      existingNames.add(button.getLabel());
    }
    return existingNames;
  }

  public T checkAccountOrder(String... accounts) {
    UIComponent[] uiComponents = getPanel().getUIComponents(Button.class, "editAccount");
    List<String> existingNames = new ArrayList<String>();
    for (UIComponent uiComponent : uiComponents) {
      Button button = (Button) uiComponent;
      existingNames.add(button.getLabel());
    }
    TestUtils.assertEquals(existingNames, accounts);
    return (T) this;
  }

  public T checkNoAccountsDisplayed() {
    TestUtils.assertEmpty(getDisplayedAccounts());
    return (T) this;
  }

  public void checkNotPresent(String accountName) {
    TestUtils.assertNotContains(getDisplayedAccounts(), accountName);
  }

  public void checkAccount(String accountName, double balance, String updateDate) {
    Panel accountPanel = getAccountPanel(accountName);

    final StringBuilder expected = new StringBuilder()
      .append(accountName)
      .append(" - ").append(toString(balance));

    final StringBuilder actual = new StringBuilder()
      .append(accountName)
      .append(" - ").append(accountPanel.getButton("accountPosition").getLabel());

    if (updateDate != null) {
      expected.append(" on ").append(updateDate);
      actual.append(" on ").append(accountPanel.getTextBox("accountUpdateDate").getText());
    }

    UISpecAssert.assertThat(new Assertion() {
      public void check() {
        Assert.assertEquals(expected.toString(), actual.toString());
      }
    });
  }

  public void checkAccountUpdateDate(String accountName, String dateText) {
    Panel parentPanel = getAccountPanel(accountName);
    UISpecAssert.assertTrue(parentPanel.getTextBox("accountUpdateDate").textEquals(dateText));
  }

  public void checkAccountWithoutPosition(String accountName, String yyyymmdd) {
    Panel parentPanel = getAccountPanel(accountName);
    assertThat(parentPanel.getButton("accountPosition").textEquals("0.00"));
    Date date = Dates.parse(yyyymmdd);
    UISpecAssert.assertTrue(parentPanel.getTextBox("accountUpdateDate").textEquals(Formatting.toString(date)));
  }

  public void checkDisplayIsEmpty(String accountName) {
    Panel parentPanel = getAccountPanel(accountName);
    UISpecAssert.assertTrue(parentPanel.getButton("accountPosition").textEquals("0.00"));
    UISpecAssert.assertTrue(parentPanel.getTextBox("accountUpdateDate").textEquals("2006/02/01"));
  }

  public T checkReferencePosition(final double amount, final String updateDate) {
    final Date date = Dates.parse(updateDate);
    assertThat(new Assertion() {
      public void check() {
        String actual = getPanel().getTextBox("referencePosition").getText() + " " + getPanel().getTextBox("referencePositionDate").getText();
        String expected = AccountViewPanelChecker.this.toString(amount) + " on " + Formatting.toString(date);
        Assert.assertEquals(expected, actual);
      }
    });
    return (T) this;
  }

  public T checkReferencePositionDateContains(String text) {
    assertThat(getPanel().getTextBox("referencePositionDate").textContains(text));
    return (T) this;
  }

  public void checkPosition(String accountName, double position) {
    Panel parentPanel = getAccountPanel(accountName);
    UISpecAssert.assertTrue(parentPanel.getButton("accountPosition").textEquals(toString(position)));
  }

  public T checkEndOfMonthPosition(String accountName, double amount) {
    getChart(accountName).checkEndOfMonthValue(amount);
    return (T) this;
  }

  public void showChart(String accountName) {
    openPopup(accountName).click(Lang.get("account.chart.show"));
  }

  public void hideChart(String accountName) {
    openPopup(accountName).click(Lang.get("account.chart.hide"));
  }

  public void toggleChart(String accountName) {
    getAccountPanel(accountName).getToggleButton("toggleGraph").click();
  }

  public T checkChartShown(String accountName) {
    checkComponentVisible(getAccountPanel(accountName), HistoChart.class, "accountPositionsChart", true);
    return (T) this;
  }

  public T checkChartHidden(String accountName) {
    checkComponentVisible(getAccountPanel(accountName), HistoChart.class, "accountPositionsChart", false);
    return (T) this;
  }

  public AccountPositionEditionChecker editPosition(String accountName) {
    Panel parentPanel = getAccountPanel(accountName);
    Window window = WindowInterceptor.getModalDialog(parentPanel.getButton("accountPosition").triggerClick());
    return new AccountPositionEditionChecker(window);
  }

  public T changePosition(String accountName, final double balance, final String operationLabel) {
    editPosition(accountName)
      .setAmountAndEnter(balance);
    return (T) this;
  }

  public AccountEditionChecker edit(String accountName) {
    return AccountEditionChecker.open(triggerEditAccount(accountName, Lang.get("accountView.edit")));
  }

  private Trigger triggerEditAccount(String accountName, String menuOption) {
    PopupButton button = openPopup(accountName);
    return button.triggerClick(menuOption);
  }

  private PopupButton openPopup(String accountName) {
    return PopupButton.init(getAccountPanel(accountName), "editAccount");
  }

  public ConfirmationDialogChecker openDelete(String accountName) {
    return ConfirmationDialogChecker.open(openPopup(accountName).triggerClick("Delete..."));
  }

  public T rollover(String accountName, int monthId, int day) {
    HistoDailyChecker chart = getChart(accountName);
    HistoSelectionManager selectionManager = chart.getChart().getSelectionManager();
    int columnIndex = chart.getDataset().getIndex(monthId);
    Set<org.globsframework.model.Key> keys = new HashSet<org.globsframework.model.Key>();
    keys.add(org.globsframework.model.Key.create(Day.MONTH, monthId, Day.DAY, day));
    selectionManager.updateRollover(columnIndex >= 0 ? columnIndex : null, keys, false, false, new Point(0, 0));
    return (T) this;
  }

  private Panel getAccountPanel(final String accountName) {
    Button account = null;
    try {
      account = getPanel().getButton(accountName);
    }
    catch (Throwable e) {
      Assert.fail("Account '" + accountName + "' not found - available accounts: " + getDisplayedAccounts());
    }
    return account.getContainer("accountPanel");
  }

  public void checkAccountWebsite(String accountName, String linkText, String expectedUrl) {
    BrowsingChecker.checkDisplay(triggerEditAccount(accountName, linkText), expectedUrl);
  }

  public void checkAccountWebsiteLinkDisabled(String accountName) {
    PopupButton button = openPopup(accountName);
    button.checkItemDisabled(Lang.get("accountView.goto.website.disabled"));
  }

  private Panel getPanel() {
    if (accountsPanel == null) {
      views.selectData();
      accountsPanel = mainWindow.getPanel(panelName);
    }
    return accountsPanel;
  }

  public HistoDailyChecker getChart(String accountName) {
    return new HistoDailyChecker(getAccountPanel(accountName), "accountPositionsChart");
  }

  public T checkContent(final String expectedContent) {
    UISpecAssert.assertThat(new Assertion() {
      public void check() {
        Assert.assertEquals(expectedContent.trim(), AccountViewPanelChecker.getActualContent(getPanel()).trim());
      }
    });
    return (T) this;
  }

  static String getActualContent(Panel panel) {
    TablePrinter printer = new TablePrinter();
    Component[] selects = panel.getSwingComponents(JToggleButton.class, "selectAccount");
    Component[] buttons = panel.getSwingComponents(JButton.class, "editAccount");
    Component[] weathers = panel.getSwingComponents(JButton.class, "accountWeather");
    Component[] positions = panel.getSwingComponents(JButton.class, "accountPosition");
    Component[] dates = panel.getSwingComponents(JLabel.class, "accountUpdateDate");
    for (int i = 0; i < weathers.length; i++) {
      String weather = toWeatherString(weathers[i]);
      JToggleButton select = (JToggleButton) selects[i];
      JButton editButton = (JButton) buttons[i];
      String accountName = editButton.getText();
      if (select.isSelected()) {
        accountName += "*";
      }
      printer.addRow(accountName,
                     ((JButton) positions[i]).getText() + " on " + ((JLabel) dates[i]).getText(),
                     weather);
    }
    return printer.toString();
  }

  private static String toWeatherString(Component weather) {
    JButton status = (JButton) weather;
    Icon icon = (status.getIcon());
    if (icon == AccountWeatherButton.SUNNY_ICON) {
      return "sunny";
    }
    else if (icon == AccountWeatherButton.CLOUDY_ICON) {
      return "cloudy";
    }
    else if (icon == AccountWeatherButton.RAINY_ICON) {
      return "rainy";
    }
    return "-";
  }
}
