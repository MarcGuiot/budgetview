package org.designup.picsou.gui.series.evolution;

import org.designup.picsou.gui.description.Formatting;
import org.designup.picsou.gui.model.BudgetStat;
import org.designup.picsou.gui.model.SavingsBudgetStat;
import org.designup.picsou.gui.model.SeriesStat;
import org.designup.picsou.gui.series.SeriesEditor;
import org.designup.picsou.gui.series.view.SeriesWrapper;
import org.designup.picsou.gui.series.view.SeriesWrapperType;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.util.Amounts;
import org.globsframework.gui.views.GlobTableView;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.KeyBuilder;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.model.format.GlobStringifier;
import org.globsframework.model.format.utils.AbstractGlobStringifier;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.InvalidParameter;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Collections;

public class SeriesEvolutionMonthEditor extends SeriesEvolutionEditor {

  protected SeriesEvolutionMonthEditor(int offset, GlobTableView view,
                                       GlobRepository repository, Directory directory,
                                       SeriesEvolutionColors colors) {
    super(offset, view, directory.get(DescriptionService.class), repository, directory, colors);

    complete(new OpenSeriesAmountEditionDialogAction(directory));
  }

  protected String getText(Glob seriesWrapper) {

    Integer itemId = seriesWrapper.get(SeriesWrapper.ITEM_ID);

    switch (SeriesWrapperType.get(seriesWrapper)) {
      case BUDGET_AREA:
        return getBudgetAreaLabelText(BudgetArea.get(itemId));

      case SERIES:
        return getSeriesButtonText(itemId);

      case SUMMARY:
        return getSummaryLabelText(seriesWrapper);

      default:
        throw new InvalidParameter("Unexpected type: " + SeriesWrapperType.get(seriesWrapper));
    }
  }

  protected String getDescription(Glob seriesWrapper) {
    return null;
  }

  private String getBudgetAreaLabelText(BudgetArea budgetArea) {
    Glob budgetStat = repository.find(Key.create(BudgetStat.TYPE, referenceMonthId));
    if (budgetArea.equals(BudgetArea.UNCATEGORIZED)) {
      return format(budgetStat, BudgetStat.UNCATEGORIZED_ABS, budgetArea);
    }
    else if (budgetStat != null) {
      return format(budgetStat, BudgetStat.getSummary(budgetArea), budgetArea);
    }
    return "";
  }

  private String getSeriesButtonText(Integer itemId) {
    Glob seriesStat = repository.find(KeyBuilder.init(SeriesStat.TYPE)
      .set(SeriesStat.MONTH, referenceMonthId)
      .set(SeriesStat.SERIES, itemId)
      .get());
    if (seriesStat == null) {
      return "";
    }

    Glob series = repository.find(Key.create(Series.TYPE, itemId));
    BudgetArea budgetArea = BudgetArea.get(series.get(Series.BUDGET_AREA));
    Double value = seriesStat.get(SeriesStat.SUMMARY_AMOUNT);
    return format(value, budgetArea);
  }

  private String getSummaryLabelText(Glob seriesWrapper) {
    Integer id = seriesWrapper.get(SeriesWrapper.ID);
    if (id.equals(SeriesWrapper.BALANCE_SUMMARY_ID)) {
      Glob budgetStat = repository.find(Key.create(BudgetStat.TYPE, referenceMonthId));
      return format(budgetStat, BudgetStat.MONTH_BALANCE, null);
    }

    if (id.equals(SeriesWrapper.MAIN_POSITION_SUMMARY_ID)) {
      Glob budgetStat = repository.find(Key.create(BudgetStat.TYPE, referenceMonthId));
      return format(budgetStat, BudgetStat.END_OF_MONTH_ACCOUNT_POSITION, null);
    }

    if (id.equals(SeriesWrapper.SAVINGS_POSITION_SUMMARY_ID)) {
      Glob budgetStat = SavingsBudgetStat.findSummary(referenceMonthId, repository);
      return format(budgetStat, SavingsBudgetStat.END_OF_MONTH_POSITION, null);
    }

    throw new InvalidParameter("Unexpected ID: " + id);
  }

  private String format(Glob glob, DoubleField field, BudgetArea budgetArea) {
    if (glob == null) {
      return "";
    }
    return format(glob.get(field), budgetArea);
  }

  private String format(Double value, BudgetArea budgetArea) {
    if (Amounts.isNullOrZero(value)) {
      return "";
    }
    if (budgetArea != null) {
      return Formatting.toString(value, budgetArea);
    }
    return Formatting.toString(value);
  }

  public GlobStringifier getStringifier() {
    return new AbstractGlobStringifier() {
      public String toString(Glob seriesWrapper, GlobRepository repository) {
        if (seriesWrapper == null) {
          return null;
        }
        return getText(seriesWrapper);
      }
    };
  }

  private class OpenSeriesAmountEditionDialogAction extends AbstractAction {
    private Directory directory;

    public OpenSeriesAmountEditionDialogAction(Directory directory) {
      this.directory = directory;
    }

    public void actionPerformed(ActionEvent e) {
      SeriesEditor.get(directory).showAmount(currentSeries, Collections.singleton(referenceMonthId));
    }
  }
}
