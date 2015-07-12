package org.designup.picsou.functests.checkers.analysis;

import org.designup.picsou.functests.checkers.SeriesDeletionDialogChecker;
import org.designup.picsou.functests.checkers.SeriesEditionDialogChecker;
import org.designup.picsou.functests.checkers.ViewChecker;
import org.designup.picsou.functests.checkers.components.HistoChartChecker;
import org.designup.picsou.functests.checkers.components.PopupButton;
import org.designup.picsou.functests.checkers.components.PopupChecker;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.utils.Lang;
import org.globsframework.utils.TestUtils;
import org.uispec4j.*;
import org.uispec4j.Panel;
import org.uispec4j.Window;
import org.uispec4j.assertion.Assertion;
import org.uispec4j.assertion.UISpecAssert;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class EvolutionAnalysisChecker extends ViewChecker {
  private Panel panel;

  public EvolutionAnalysisChecker(Window mainWindow) {
    super(mainWindow);
  }


  protected Panel getPanel() {
    if (panel == null) {
      views.selectAnalysis();
      this.panel = mainWindow.getPanel("evolutionAnalysis");
    }
    return panel;
  }

  public EvolutionAnalysisChecker selectIncome() {
    return select(BudgetArea.INCOME);
  }

  public EvolutionAnalysisChecker selectRecurring() {
    return select(BudgetArea.RECURRING);
  }

  public EvolutionAnalysisChecker selectVariable() {
    return select(BudgetArea.VARIABLE);
  }

  private EvolutionAnalysisChecker select(BudgetArea budgetArea) {
    Mouse.click(getPanel().getPanel("budgetAreas").getTextBox(budgetArea.getLabel()).getContainer("selectionPanel"));
    return this;
  }

  public HistoChartChecker initBudgetAreaGraph() {
    return new HistoChartChecker(getPanel().getPanel("budgetAreasChart"));
  }

  public EvolutionAnalysisChecker checkSeriesList(final String... expectedNames) {
    UISpecAssert.assertThat(new Assertion() {
      public void check() {
        List<String> actualNames = new ArrayList<String>();
        for (Component seriesButton : getPanel().getSwingComponents(JButton.class, "seriesButton")) {
          actualNames.add(((JButton) seriesButton).getText());
        }
        TestUtils.assertEquals(actualNames, expectedNames);
      }
    });
    return this;
  }

  public HistoChartChecker initSeriesGraph(String series) {
    Panel seriesPanel = getSeriesButton(series).getContainer("seriesPanel");
    return new HistoChartChecker(seriesPanel.getPanel("seriesChart"));
  }

  private org.uispec4j.Button getSeriesButton(String series) {
    return getPanel().getPanel("seriesRepeat").getButton(series);
  }

  public SeriesEditionDialogChecker editSeries(String series) {
    return SeriesEditionDialogChecker.open(PopupButton.init(getSeriesButton(series)).triggerClick(Lang.get("series.edit")));
  }

  public EvolutionAnalysisChecker deleteSeriesWithConfirmation(String series) {
    Trigger deletionTrigger = PopupButton.init(getSeriesButton(series)).triggerClick(Lang.get("series.delete"));
    SeriesDeletionDialogChecker.init(deletionTrigger).uncategorize();
    return this;
  }
}
