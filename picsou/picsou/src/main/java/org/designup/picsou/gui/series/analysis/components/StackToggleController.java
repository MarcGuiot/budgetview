package org.designup.picsou.gui.series.analysis.components;

import org.designup.picsou.utils.Lang;
import org.globsframework.gui.splits.utils.GuiUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class StackToggleController {

  private JComponent budgetStack;
  private JComponent seriesStack;
  private JComponent subSeriesStack;

  private JButton backToBudgetButton;

  public StackToggleController(JComponent budgetStack, JComponent seriesStack, JComponent subSeriesStack) {
    this.budgetStack = budgetStack;
    this.seriesStack = seriesStack;
    this.subSeriesStack = subSeriesStack;
    this.backToBudgetButton = new JButton(new AbstractAction(Lang.get("seriesAnalysis.toggleController.backToBudget")) {
      public void actionPerformed(ActionEvent actionEvent) {
        showBudgetStack();
      }
    });
    showBudgetStack();
  }

  public JButton getBackToBudgetButton() {
    return backToBudgetButton;
  }

  public void showBudgetStack() {
    update(true);
  }

  public void showSubseriesStack() {
    update(false);
  }

  private void update(final boolean showBudget) {
    GuiUtils.runInSwingThread(new Runnable() {
      public void run() {
        budgetStack.setVisible(showBudget);
        subSeriesStack.setVisible(!showBudget);
        backToBudgetButton.setEnabled(!showBudget);
        GuiUtils.revalidate(backToBudgetButton);
      }
    });
  }
}
