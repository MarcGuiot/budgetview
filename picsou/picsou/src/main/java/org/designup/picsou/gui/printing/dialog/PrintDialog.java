package org.designup.picsou.gui.printing.dialog;

import org.designup.picsou.gui.components.CancelAction;
import org.designup.picsou.gui.components.dialogs.PicsouDialog;
import org.designup.picsou.gui.printing.PrinterService;
import org.designup.picsou.gui.printing.report.BudgetReport;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.util.MonthRange;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.OperationFailed;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.SortedSet;

public class PrintDialog {

  private PicsouDialog dialog;
  private GlobRepository repository;
  private Directory directory;
  private Period currentPeriod;
  private PrintDialog.CurrentMonthAction currentMonthAction;
  private PrintDialog.CurrentYearAction currentYearAction;
  private Integer currentMonth;

  public PrintDialog(GlobRepository repository, Directory directory) {
    this.repository = repository;
    this.directory = directory;
    initDialog();
  }

  public void show(Integer currentMonth) {
    this.currentMonth = currentMonth;
    currentMonthAction.update(currentMonth);
    currentYearAction.update(currentMonth);
    dialog.showCentered();
  }

  private void initDialog() {
    dialog = PicsouDialog.create(directory.get(JFrame.class), true, directory);

    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/printing/printDialog.splits",
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
        BudgetReport report = new BudgetReport(currentMonth, currentPeriod.getRange(), repository, directory);
        dialog.setVisible(false);
        directory.get(PrinterService.class).print(Lang.get("application"), report);
      }
      catch (OperationFailed e) {
        throw new RuntimeException(e);
      }
    }
  }

  private interface Period {
    MonthRange getRange();
  }

  private abstract class PeriodSelectionAction extends AbstractAction implements Period {

    private MonthRange range;

    public void update(Integer currentMonthId) {
      MonthRange target = getTargetRange(currentMonthId);
      MonthRange available = getExistingRange(repository);
      this.range = target.intersection(available);
      putValue(Action.NAME, getLabel(currentMonthId));
    }

    protected abstract String getLabel(Integer currentMonthId);

    protected abstract MonthRange getTargetRange(int currentMonthId);

    protected MonthRange getExistingRange(GlobRepository repository) {
      SortedSet<Integer> months = repository.getAll(Month.TYPE).getSortedSet(Month.ID);
      return new MonthRange(months.first(), months.last());
    }

    public MonthRange getRange() {
      return range;
    }

    public void actionPerformed(ActionEvent actionEvent) {
      PrintDialog.this.currentPeriod = this;
    }
  }

  private class CurrentMonthAction extends PeriodSelectionAction {

    protected String getLabel(Integer currentMonthId) {
      return Lang.get("print.dialog.option.month", Month.getFullLabel(currentMonthId));
    }

    protected MonthRange getTargetRange(int currentMonthId) {
      return new MonthRange(Month.previous(currentMonthId),
                            Month.next(currentMonthId, 10));
    }
  }

  private class CurrentYearAction extends PeriodSelectionAction {

    protected String getLabel(Integer currentMonthId) {
      return Lang.get("print.dialog.option.year", Integer.toString(Month.toYear(currentMonthId)));
    }

    protected MonthRange getTargetRange(int currentMonthId) {
      int year = Month.toYear(currentMonthId) * 100;
      return new MonthRange(year + 1, year + 12);
    }
  }
}
