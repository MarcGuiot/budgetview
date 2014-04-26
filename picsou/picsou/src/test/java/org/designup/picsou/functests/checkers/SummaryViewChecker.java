package org.designup.picsou.functests.checkers;

import junit.framework.Assert;
import org.designup.picsou.functests.checkers.components.HistoDailyChecker;
import org.designup.picsou.functests.checkers.components.PopupButton;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.splits.utils.HtmlUtils;
import org.globsframework.utils.TestUtils;
import org.uispec4j.Button;
import org.uispec4j.Panel;
import org.uispec4j.UIComponent;
import org.uispec4j.Window;

import java.util.ArrayList;
import java.util.List;

import static org.uispec4j.assertion.UISpecAssert.assertThat;

public class SummaryViewChecker extends ViewChecker {
  private Panel panel;

  public SummaryViewChecker(Window mainWindow) {
    super(mainWindow);
  }

  public HistoDailyChecker getAccountChart(String name) {
    return new HistoDailyChecker(getAccountPanel(name), "accountHistoChart");
  }

  public Panel getAccountPanel(String name) {
    views.selectHome();
    Button button = getPanel().getButton(name);
    return button.getContainer("accountPanel");
  }

  private Panel getPanel() {
    if (panel == null) {
      views.selectHome();
      panel = mainWindow.getPanel("summaryView");
    }
    return panel;
  }

  public void checkAccounts(String... accountNames) {
    List<String> result = new ArrayList<String>();
    for (UIComponent component : getPanel().getUIComponents(Button.class, "accountChartButton")) {
      result.add(((Button)component).getLabel());
    }
    TestUtils.assertEquals(result, accountNames);
  }

  public void moveAccountUp(String accountName) {
    PopupButton button = PopupButton.init(getPanel(), accountName);
    button.click(Lang.get("account.move.up"));
  }

  public void moveAccountDown(String accountName) {
    PopupButton button = PopupButton.init(getPanel(), accountName);
    button.click(Lang.get("account.move.down"));
  }

  public void hideGraph(String accountName) {
    PopupButton button = PopupButton.init(getPanel(), accountName);
    button.click(Lang.get("account.chart.show"));
  }

  public void showGraph(String accountName) {
    PopupButton button = PopupButton.init(getPanel(), accountName);
    button.click(Lang.get("account.chart.show"));
  }

  public void checkGraphShown(String accountName) {
    Assert.assertTrue(getAccountChart(accountName).getChart().isVisible());
  }

  public void checkGraphHidden(String accountName) {
    Assert.assertFalse(getAccountChart(accountName).getChart().isVisible());
  }

  public void checkAccountPosition(String accountName, String expected) {
    Assert.assertEquals(expected,
                        HtmlUtils.cleanup(getAccountPanel(accountName).getTextBox("accountPositionLabel").getText()));
  }
}
