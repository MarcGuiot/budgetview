package com.budgetview.desktop.printing.dialog;

import com.budgetview.desktop.components.dialogs.CancelAction;
import com.budgetview.desktop.components.dialogs.MessageDialog;
import com.budgetview.desktop.components.dialogs.MessageType;
import com.budgetview.desktop.components.dialogs.PicsouDialog;
import com.budgetview.desktop.printing.PrinterService;
import com.budgetview.desktop.printing.budget.BudgetReport;
import com.budgetview.model.Month;
import com.budgetview.model.util.ClosedMonthRange;
import com.budgetview.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.OperationFailed;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.SortedSet;

public class PrintBudgetDialog {

  private PicsouDialog dialog;
  private GlobRepository repository;
  private Directory directory;
  private Period currentPeriod;
  private PrintBudgetDialog.CurrentMonthAction currentMonthAction;
  private PrintBudgetDialog.CurrentYearAction currentYearAction;
  private SortedSet<Integer> selectedMonths;
  private Integer currentMonth;
  private GlobsPanelBuilder builder;

  public PrintBudgetDialog(GlobRepository repository, Directory directory) {
    this.repository = repository;
    this.directory = directory;
    initDialog();
  }

  public void show(SortedSet<Integer> selectedMonths) {
    this.selectedMonths = selectedMonths;

    this.currentMonth = selectedMonths.last();
    this.currentMonthAction.update(currentMonth);
    this.currentYearAction.update(currentMonth);

    this.dialog.showCentered();
    builder.dispose();
  }

  private void initDialog() {
    dialog = PicsouDialog.create(this, directory.get(JFrame.class), true, directory);

    builder = new GlobsPanelBuilder(getClass(), "/layout/printing/printBudgetDialog.splits",
                                                      repository, directory);

    currentMonthAction = new CurrentMonthAction();
    currentYearAction = new CurrentYearAction();

    JRadioButton currentMonthRadio = new JRadioButton(currentMonthAction);
    JRadioButton currentYearRadio = new JRadioButton(currentYearAction);

    builder.add("currentMonth", currentMonthRadio);
    builder.add("currentYear", currentYearRadio);

    ButtonGroup group = new ButtonGroup();
    group.add(currentMonthRadio);
    group.add(currentYearRadio);

    currentMonthRadio.doClick();

    dialog.addPanelWithButtons(builder.<JPanel>load(),
                               new OkAction(), new CancelAction(dialog));
    dialog.pack();
  }

  private class OkAction extends AbstractAction {

    private OkAction() {
      super(Lang.get("print.dialog.ok"));
    }

    public void actionPerformed(ActionEvent actionEvent) {
      try {
        // Build report before closing the window
        BudgetReport report = new BudgetReport(selectedMonths, currentMonth, currentPeriod.getRange(),
                                               repository, directory);
        dialog.setVisible(false);
        directory.get(PrinterService.class).print(Lang.get("application"), report);
      }
      catch (OperationFailed e) {
        MessageDialog.show("print.completion.failed.title", MessageType.ERROR, directory,
                           "print.completion.failed.message", e.getMessage());
      }
    }
  }

  private interface Period {
    ClosedMonthRange getRange();
  }

  private abstract class PeriodSelectionAction extends AbstractAction implements Period {

    private ClosedMonthRange range;

    public void update(Integer currentMonthId) {
      ClosedMonthRange target = getTargetRange(currentMonthId);
      ClosedMonthRange available = getExistingRange(repository);
      this.range = target.intersection(available);
      putValue(Action.NAME, getLabel(currentMonthId));
    }

    protected abstract String getLabel(Integer currentMonthId);

    protected abstract ClosedMonthRange getTargetRange(int currentMonthId);

    protected ClosedMonthRange getExistingRange(GlobRepository repository) {
      SortedSet<Integer> months = repository.getAll(Month.TYPE).getSortedSet(Month.ID);
      return new ClosedMonthRange(months.first(), months.last());
    }

    public ClosedMonthRange getRange() {
      return range;
    }

    public void actionPerformed(ActionEvent actionEvent) {
      PrintBudgetDialog.this.currentPeriod = this;
    }
  }

  private class CurrentMonthAction extends PeriodSelectionAction {

    protected String getLabel(Integer currentMonthId) {
      return Lang.get("print.dialog.option.month", Month.getFullLabel(currentMonthId, true));
    }

    protected ClosedMonthRange getTargetRange(int currentMonthId) {
      return new ClosedMonthRange(Month.previous(currentMonthId),
                            Month.next(currentMonthId, 10));
    }
  }

  private class CurrentYearAction extends PeriodSelectionAction {

    protected String getLabel(Integer currentMonthId) {
      return Lang.get("print.dialog.option.year", Integer.toString(Month.toYear(currentMonthId)));
    }

    protected ClosedMonthRange getTargetRange(int currentMonthId) {
      int year = Month.toYear(currentMonthId) * 100;
      return new ClosedMonthRange(year + 1, year + 12);
    }
  }
}
