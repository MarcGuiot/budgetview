package com.budgetview.gui.analysis.budget;

import com.budgetview.gui.analysis.utils.SeriesWrapperSelection;
import com.budgetview.utils.Lang;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class StackToggleController {

  private JComponent budgetStack;
  private JComponent rootSeriesStack;
  private JComponent groupSeriesStack;
  private JComponent subSeriesStack;
  private GlobRepository repository;

  private JButton gotoUpButton;
  private JButton gotoDownButton;

  private ViewMode currentMode;
  private SeriesWrapperSelection currentSelection = new SeriesWrapperSelection(new GlobList(), repository);

  public StackToggleController(JComponent budgetStack,
                               JComponent rootSeriesStack,
                               JComponent groupSeriesStack,
                               JComponent subSeriesStack,
                               GlobRepository repository) {
    this.budgetStack = budgetStack;
    this.rootSeriesStack = rootSeriesStack;
    this.groupSeriesStack = groupSeriesStack;
    this.subSeriesStack = subSeriesStack;
    this.repository = repository;
    this.gotoUpButton = new JButton(new AbstractAction() {
      public void actionPerformed(ActionEvent actionEvent) {
        currentMode.up();
      }
    });
    this.gotoDownButton = new JButton(new AbstractAction() {
      public void actionPerformed(ActionEvent actionEvent) {
        currentMode.down();
      }
    });
    this.currentMode = new ViewBudgetAndRoot();
  }

  public void updateFromSelection(GlobList wrappers) {
    currentSelection = new SeriesWrapperSelection(wrappers, repository);
    ViewMode newMode = currentMode.getNewMode(currentSelection);
    setMode(newMode);
  }

  public void showBudget(Glob wrapper) {
    currentSelection = new SeriesWrapperSelection(new GlobList(wrapper), repository);
    setMode(new ViewBudgetAndRoot());
  }

  private void setMode(ViewMode mode) {
    this.currentMode = mode;
    mode.apply(currentSelection);
  }

  public JButton getGotoUpButton() {
    return gotoUpButton;
  }

  public JButton getGotoDownButton() {
    return gotoDownButton;
  }

  private abstract class ViewMode {

    private final boolean showBudget;
    private final boolean showRoot;
    private final boolean showGroup;
    private final boolean showSubSeries;

    protected ViewMode(boolean showBudget, boolean showRoot, boolean showGroup, boolean showSubSeries) {
      this.showBudget = showBudget;
      this.showRoot = showRoot;
      this.showGroup = showGroup;
      this.showSubSeries = showSubSeries;
    }

    protected final void apply(SeriesWrapperSelection selection) {
      budgetStack.setVisible(showBudget);
      rootSeriesStack.setVisible(showRoot);
      groupSeriesStack.setVisible(showGroup);
      subSeriesStack.setVisible(showSubSeries);
      updateButtons(selection);
      GuiUtils.runInSwingThread(new Runnable() {
        public void run() {
          GuiUtils.revalidate(gotoUpButton);
          GuiUtils.revalidate(gotoDownButton);
        }
      });
    }

    abstract ViewMode getNewMode(SeriesWrapperSelection selection);

    abstract void updateButtons(SeriesWrapperSelection selection);

    abstract void up();

    abstract void down();
  }

  private class ViewBudgetAndRoot extends ViewMode {

    private ViewMode downMode;

    private ViewBudgetAndRoot() {
      super(true, true, false, false);
    }

    public ViewMode getNewMode(SeriesWrapperSelection selection) {
      if (selection.isSubSeriesListFromSameGroupSeries()) {
        return new ViewGroupAndSubSeries();
      }
      if (selection.isSubSeriesListFromSameRootSeries()) {
        return new ViewRootAndSubSeries();
      }
      if (selection.isSingleSeriesInGroup()) {
        return new ViewRootAndGroup();
      }
      return this;
    }

    void updateButtons(SeriesWrapperSelection selection) {
      gotoUpButton.setEnabled(false);
      if (selection.isSingleSeriesInGroup() || selection.isSingleGroup()) {
        gotoDownButton.setText(Lang.get("seriesAnalysis.toggleController.gotoGroupSeries"));
        gotoDownButton.setEnabled(true);
        downMode = new ViewRootAndGroup();
      }
      else if (selection.isSingleSeriesWithSubSeries()) {
        gotoDownButton.setText(Lang.get("seriesAnalysis.toggleController.gotoSubSeries"));
        gotoDownButton.setEnabled(true);
        downMode = new ViewRootAndSubSeries();
      }
      else {
        gotoDownButton.setEnabled(false);
      }
    }

    void up() {
      // noop
    }

    void down() {
      setMode(downMode);
    }
  }

  private class ViewRootAndGroup extends ViewMode {
    private ViewRootAndGroup() {
      super(false, true, true, false);
    }

    public ViewMode getNewMode(SeriesWrapperSelection selection) {
      if (selection.isSubSeriesListFromSameGroupSeries()) {
        return new ViewGroupAndSubSeries();
      }
      if (selection.isSubSeriesListFromSameRootSeries()) {
        return new ViewRootAndSubSeries();
      }
      if (selection.isRootSeriesOrGroup() || selection.isSingleSeriesInGroup()) {
        return this;
      }
      return new ViewBudgetAndRoot();
    }

    void updateButtons(SeriesWrapperSelection selection) {
      gotoUpButton.setText(Lang.get("seriesAnalysis.toggleController.gotoBudget"));
      gotoUpButton.setEnabled(true);
      if (selection.isSingleSeriesWithSubSeries()) {
        gotoDownButton.setText(Lang.get("seriesAnalysis.toggleController.gotoSubSeries"));
        gotoDownButton.setEnabled(true);
      }
      else {
        gotoDownButton.setEnabled(false);
      }
    }

    void up() {
      setMode(new ViewBudgetAndRoot());
    }

    void down() {
      setMode(new ViewGroupAndSubSeries());
    }
  }

  private class ViewRootAndSubSeries extends ViewMode {
    private ViewRootAndSubSeries() {
      super(false, true, false, true);
    }

    public ViewMode getNewMode(SeriesWrapperSelection selection) {
      if (selection.isSingleSeriesWithSubSeries()) {
        return this;
      }
      if (selection.isSubSeriesListFromSameRootSeries()) {
        return this;
      }
      if (selection.isSubSeriesListFromSameGroupSeries()) {
        return new ViewGroupAndSubSeries();
      }
      return new ViewBudgetAndRoot();
    }

    void updateButtons(SeriesWrapperSelection selection) {
      gotoUpButton.setText(Lang.get("seriesAnalysis.toggleController.gotoBudget"));
      gotoUpButton.setEnabled(true);
      gotoDownButton.setEnabled(false);
    }

    void up() {
      setMode(new ViewBudgetAndRoot());
    }

    void down() {
    }
  }

  private class ViewGroupAndSubSeries extends ViewMode {
    private ViewGroupAndSubSeries() {
      super(false, false, true, true);
    }

    public ViewMode getNewMode(SeriesWrapperSelection selection) {
      if (selection.isSubSeriesListFromSameGroupSeries()) {
        return this;
      }
      if (selection.isSubSeriesListFromSameRootSeries()) {
        return new ViewRootAndSubSeries();
      }
      return new ViewBudgetAndRoot();
    }

    void updateButtons(SeriesWrapperSelection selection) {
      gotoUpButton.setText(Lang.get("seriesAnalysis.toggleController.gotoGroupSeries"));
      gotoUpButton.setEnabled(true);
      gotoDownButton.setEnabled(false);
    }

    void up() {
      setMode(new ViewRootAndGroup());
    }

    void down() {

    }
  }

}
