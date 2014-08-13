package org.designup.picsou.gui.analysis.evolution;

import org.designup.picsou.gui.description.Formatting;
import org.designup.picsou.gui.model.*;
import org.designup.picsou.gui.series.SeriesEditor;
import org.designup.picsou.gui.analysis.SeriesChartsColors;
import org.designup.picsou.gui.series.view.SeriesWrapper;
import org.designup.picsou.gui.series.view.SeriesWrapperType;
import org.designup.picsou.model.*;
import com.budgetview.shared.utils.Amounts;
import org.globsframework.gui.views.GlobTableView;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.metamodel.fields.LinkField;
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
import javax.swing.border.Border;
import java.awt.event.ActionEvent;
import java.util.Collections;

public class SeriesEvolutionMonthEditor extends SeriesEvolutionEditor {

  protected SeriesEvolutionMonthEditor(int offset, GlobTableView view,
                                       GlobRepository repository, Directory directory,
                                       SeriesChartsColors colors) {
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

      case SERIES_GROUP:
        return getSeriesGroupButtonText(itemId);

      case SUMMARY:
        return getSummaryLabelText(seriesWrapper);

      case SUB_SERIES:
        return getSubSeriesButtonText(itemId);

      default:
        throw new InvalidParameter("Unexpected type: " + SeriesWrapperType.get(seriesWrapper));
    }
  }

  protected Border getBorderForSeries(Glob wrapper) {
    return null;
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
    return getButtonText(itemId, SeriesType.SERIES, Series.TYPE, Series.BUDGET_AREA);
  }

  private String getSeriesGroupButtonText(Integer itemId) {
    return getButtonText(itemId, SeriesType.SERIES_GROUP, SeriesGroup.TYPE, SeriesGroup.BUDGET_AREA);
  }

  private String getButtonText(Integer itemId, SeriesType seriesType, GlobType type, LinkField budgetAreaField) {
    Glob seriesStat = repository.find(KeyBuilder.init(SeriesStat.TYPE)
                                        .set(SeriesStat.MONTH, referenceMonthId)
                                        .set(SeriesStat.TARGET_TYPE, seriesType.getId())
                                        .set(SeriesStat.TARGET, itemId)
                                        .get());
    if (seriesStat == null) {
      return "";
    }

    Glob target = repository.get(Key.create(type, itemId));
    BudgetArea budgetArea = BudgetArea.get(target.get(budgetAreaField));
    Double value = seriesStat.get(SeriesStat.SUMMARY_AMOUNT);
    return format(value, budgetArea);
  }

  private String getSubSeriesButtonText(Integer itemId) {
    Glob subSeriesStat = repository.find(KeyBuilder.init(SubSeriesStat.TYPE)
                                        .set(SubSeriesStat.MONTH, referenceMonthId)
                                        .set(SubSeriesStat.SUB_SERIES, itemId)
                                        .get());
    if (subSeriesStat == null) {
      return "";
    }

    Glob subSeries = repository.get(Key.create(SubSeries.TYPE, itemId));
    Glob series = repository.get(Key.create(Series.TYPE, subSeries.get(SubSeries.SERIES)));
    BudgetArea budgetArea = BudgetArea.get(series.get(Series.BUDGET_AREA));
    Double value = subSeriesStat.get(SubSeriesStat.ACTUAL_AMOUNT);
    return format(value, budgetArea);
  }

  private String getSummaryLabelText(Glob seriesWrapper) {
    Integer id = seriesWrapper.get(SeriesWrapper.ID);
    if (id.equals(SeriesWrapper.BALANCE_SUMMARY_ID)) {
      Glob budgetStat = repository.find(Key.create(BudgetStat.TYPE, referenceMonthId));
      return format(budgetStat, BudgetStat.MONTH_BALANCE, null);
    }

    if (id.equals(SeriesWrapper.MAIN_POSITION_SUMMARY_ID)) {
      Glob budgetStat = repository.find(Key.create(MainAccountStat.ACCOUNT, Account.MAIN_SUMMARY_ACCOUNT_ID,
                                                   MainAccountStat.MONTH, referenceMonthId));
//      Glob budgetStat = repository.find(Key.create(BudgetStat.TYPE, referenceMonthId));
      return format(budgetStat, MainAccountStat.END_POSITION, null);
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
    return format(glob.get(field, 0.), budgetArea);
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
