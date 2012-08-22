package org.designup.picsou.gui.series.analysis.components;

import org.designup.picsou.utils.Lang;
import org.globsframework.gui.splits.utils.GuiUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class StackToggleController {

  private JComponent budgetStack;
  private JComponent subSeriesStack;

  private JButton gotoBudgetButton;
  private JButton gotoSubSeriesButton;

  private boolean subSeriesPresent;
  private Mode currentMode;

  private enum Mode {
    BUDGET(true, false),
    SUB_SERIES(false, true);
    private final boolean budgetShown;
    private final boolean subSeriesShown;

    Mode(boolean budgetShown, boolean subSeriesShown) {
      this.budgetShown = budgetShown;
      this.subSeriesShown = subSeriesShown;
    }
  }

  public StackToggleController(JComponent budgetStack, JComponent subSeriesStack) {
    this.budgetStack = budgetStack;
    this.subSeriesStack = subSeriesStack;
    this.gotoBudgetButton = new JButton(new AbstractAction(Lang.get("seriesAnalysis.toggleController.gotoBudget")) {
      public void actionPerformed(ActionEvent actionEvent) {
        showBudgetStack();
      }
    });
    this.gotoSubSeriesButton = new JButton(new AbstractAction(Lang.get("seriesAnalysis.toggleController.gotoSubSeries")) {
      public void actionPerformed(ActionEvent actionEvent) {
        showSubSeriesStack();
      }
    });
    showBudgetStack();
  }

  public void setSubSeriesPresent(boolean present) {
    if (present != subSeriesPresent) {
      this.subSeriesPresent = present;
      update();
    }
  }

  public JButton getGotoBudgetButton() {
    return gotoBudgetButton;
  }

  public JButton getGotoSubSeriesButton() {
    return gotoSubSeriesButton;
  }

  public void showBudgetStack() {
    this.currentMode = Mode.BUDGET;
    update();
  }

  public void showSubSeriesStack() {
    this.currentMode = Mode.SUB_SERIES;
    this.subSeriesPresent = true;
    update();
  }

  private void update() {
    if (!subSeriesPresent) {
      currentMode = Mode.BUDGET;
    }
    budgetStack.setVisible(currentMode.budgetShown);
    subSeriesStack.setVisible(currentMode.subSeriesShown);
    gotoBudgetButton.setEnabled(currentMode.subSeriesShown);
    gotoSubSeriesButton.setEnabled(currentMode.budgetShown && subSeriesPresent);
    GuiUtils.runInSwingThread(new Runnable() {
      public void run() {
        GuiUtils.revalidate(gotoBudgetButton);
        GuiUtils.revalidate(gotoSubSeriesButton);
      }
    });
  }
}
