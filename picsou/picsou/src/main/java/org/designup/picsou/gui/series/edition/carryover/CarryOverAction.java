package org.designup.picsou.gui.series.edition.carryover;

import org.designup.picsou.gui.components.dialogs.ConfirmationDialog;
import org.designup.picsou.gui.components.dialogs.MessageDialog;
import org.designup.picsou.gui.components.dialogs.MessageType;
import org.designup.picsou.gui.description.Formatting;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.Project;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.SeriesBudget;
import com.budgetview.shared.utils.Amounts;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Set;

import static org.globsframework.model.FieldValue.value;

public class CarryOverAction
  extends AbstractAction
  implements ChangeSetListener, GlobSelectionListener, Disposable {

  private final Key seriesKey;
  private final Integer seriesId;
  private Integer currentMonth;
  private GlobRepository repository;
  private Directory directory;

  public CarryOverAction(Key seriesKey, GlobRepository repository, Directory directory) {
    this.seriesKey = seriesKey;
    this.seriesId = seriesKey.get(Series.ID);
    this.repository = repository;
    this.directory = directory;
    repository.addChangeListener(this);
    directory.get(SelectionService.class).addListener(this, Month.TYPE);
    update();
  }

  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    if (changeSet.containsChanges(seriesKey)) {
      update();
    }
    else if (changeSet.containsChanges(SeriesBudget.TYPE)) {
      Set<Key> updatedKeys = changeSet.getUpdated(SeriesBudget.TYPE);
      for (Key key : updatedKeys) {
        if (seriesId.equals(key.get(SeriesBudget.ID))) {
          update();
          return;
        }
      }
    }
  }

  public void selectionUpdated(GlobSelection selection) {
    GlobList months = selection.getAll(Month.TYPE);
    currentMonth = months.size() == 1 ? months.getFirst().get(Month.ID) : null;
    update();
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    if (changedTypes.contains(SeriesBudget.TYPE)) {
      update();
    }
  }

  private void update() {
    if ((currentMonth == null) ||
        (repository == null) ||
        !repository.contains(Key.create(Month.TYPE, Month.next(currentMonth)))) {
      setDisabled();
      return;
    }

    Glob series = repository.find(seriesKey);
    if ((series == null) || Project.findProject(series, repository) != null) {
      setDisabled();
      return;
    }

    Glob seriesBudget = SeriesBudget.find(seriesId, currentMonth, repository);
    if ((seriesBudget == null) || (!seriesBudget.isTrue(SeriesBudget.ACTIVE))) {
      setDisabled();
      return;
    }

    double actual = seriesBudget.get(SeriesBudget.ACTUAL_AMOUNT, 0.00);
    double planned = seriesBudget.get(SeriesBudget.PLANNED_AMOUNT, 0.00);
    if (Amounts.equal(planned, actual)) {
      setDisabled();
      return;
    }

    if (Amounts.isNearZero(planned) || planned < 0) {
      if (actual > planned) {
        setNegativeRemainderText();
      }
      else {
        setNegativeOverdrawText();
      }
    }
    else {
      if (actual < planned) {
        setPositiveRemainderText();
      }
      else {
        setPositiveOverdrawText();
      }
    }
    setEnabled(true);
  }

  private void setDisabled() {
    putValue(Action.NAME, Lang.get("series.carryOver.disabled"));
    setEnabled(false);
  }

  private void setPositiveRemainderText() {
    putValue(Action.NAME, Lang.get("series.carryOver.positive.remainder"));
  }

  private void setPositiveOverdrawText() {
    putValue(Action.NAME, Lang.get("series.carryOver.positive.overdraw"));
  }

  private void setNegativeRemainderText() {
    putValue(Action.NAME, Lang.get("series.carryOver.negative.remainder"));
  }

  private void setNegativeOverdrawText() {
    putValue(Action.NAME, Lang.get("series.carryOver.negative.overdraw"));
  }

  public void actionPerformed(ActionEvent actionEvent) {

    Glob series = repository.find(seriesKey);
    Glob seriesBudget = SeriesBudget.find(seriesId, currentMonth, repository);
    double actual = seriesBudget.get(SeriesBudget.ACTUAL_AMOUNT, 0.00);
    double planned = seriesBudget.get(SeriesBudget.PLANNED_AMOUNT, 0.00);

    Glob nextMonthBudget = SeriesBudget.findOrCreate(seriesId, Month.next(currentMonth), repository);
    double nextMonthPlanned = nextMonthBudget == null ? 0.00 : nextMonthBudget.get(SeriesBudget.PLANNED_AMOUNT, 0.00);
    double nextMonthActual = nextMonthBudget == null ? 0.00 : nextMonthBudget.get(SeriesBudget.ACTUAL_AMOUNT, 0.00);

    CarryOverComputer computer = new CarryOverComputer(currentMonth, actual, planned);
    int month = Month.next(currentMonth);
    while (computer.hasNext() && repository.contains(Key.create(Month.TYPE, month))) {
      Glob nextSeriesBudget = SeriesBudget.find(seriesId, month, repository);
      double nextPlanned = nextSeriesBudget == null ? 0.00 : nextSeriesBudget.get(SeriesBudget.PLANNED_AMOUNT, 0.00);
      double nextActual = nextSeriesBudget == null ? 0.00 : nextSeriesBudget.get(SeriesBudget.ACTUAL_AMOUNT, 0.00);
      computer.next(nextActual, nextPlanned);
      month = Month.next(month);
    }

    List<CarryOver> result = computer.getResult();

    CarryOverOption option = queryCarryOverOption(computer, result);

    if (option == CarryOverOption.NONE) {
      return;
    }

    if (option == CarryOverOption.FORCE_SINGLE_MONTH) {
      result = computer.forceSingleMonth(nextMonthActual, nextMonthPlanned);
    }

    repository.startChangeSet();
    try {
      int lastMonth = currentMonth;
      int index = 0;
      for (CarryOver carryOver : result) {
        lastMonth = carryOver.getMonth();
        Glob budget = SeriesBudget.findOrCreate(seriesId, lastMonth, repository);
        double newPlanned = carryOver.getNewPlanned();
        repository.update(budget.getKey(),
                          value(SeriesBudget.ACTIVE,
                                budget.isTrue(SeriesBudget.ACTIVE) || Amounts.isNotZero(newPlanned)),
                          value(SeriesBudget.PLANNED_AMOUNT, newPlanned));
        if ((index++ > 1) && (option != CarryOverOption.SEVERAL_MONTHS)) {
          break;
        }
      }
      repository.update(seriesKey, value(Series.IS_AUTOMATIC, false));
      if (currentMonth.equals(series.get(Series.LAST_MONTH))) {
        repository.update(seriesKey, Series.LAST_MONTH, lastMonth);
      }
    }
    finally {
      repository.completeChangeSet();
    }
  }

  private CarryOverOption queryCarryOverOption(CarryOverComputer computer, List<CarryOver> result) {
    CarryOverOption option = CarryOverOption.DEFAULT;
    if (!computer.canCarryOver()) {
      MessageDialog.show("series.carryOver.dialog.title",
                         MessageType.INFO, directory, "series.carryOver.confirm.noCarryOverPossible");
      option = CarryOverOption.NONE;
    }
    else if ((result.size() > 1) && (result.get(1).getCarriedOver() == 0.00)) {
      if (ConfirmationDialog.confirmed("series.carryOver.dialog.title",
                                       Lang.get("series.carryOver.confirm.nothingPlanned",
                                                Formatting.toAbsString(result.get(0).getRemainder())),
                                       directory.get(JFrame.class), directory)) {
        option = CarryOverOption.SEVERAL_MONTHS;
      }
      else {
        option = CarryOverOption.NONE;
      }
    }
    else if (result.size() > 2) {
      CarryOverDialog dialog =
        new CarryOverDialog(result.get(1).getAvailable(),
                            result.get(0).getRemainder(),
                            result.get(1).getRemainder(),
                            repository, directory);
      option = dialog.show();
    }
    return option;
  }

  public void dispose() {
    repository.removeChangeListener(this);
    repository = null;
    directory.get(SelectionService.class).removeListener(this);
    directory = null;
  }
}
