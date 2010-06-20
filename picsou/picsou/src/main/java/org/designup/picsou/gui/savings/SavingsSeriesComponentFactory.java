package org.designup.picsou.gui.savings;

import org.designup.picsou.gui.card.NavigationService;
import org.designup.picsou.gui.components.charts.BudgetAreaGaugeFactory;
import org.designup.picsou.gui.components.charts.Gauge;
import org.designup.picsou.gui.components.charts.GlobGaugeView;
import org.designup.picsou.gui.description.Formatting;
import org.designup.picsou.gui.model.PeriodSeriesStat;
import org.designup.picsou.gui.series.SeriesEditionDialog;
import org.designup.picsou.gui.budget.SeriesEditionButtons;
import org.designup.picsou.model.Account;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.util.Amounts;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.repeat.RepeatCellBuilder;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.gui.views.GlobButtonView;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.model.format.GlobListStringifier;
import org.globsframework.model.format.GlobStringifier;
import org.globsframework.model.utils.GlobListFunctor;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.UnexpectedApplicationState;

public class SavingsSeriesComponentFactory implements RepeatComponentFactory<Glob> {
  private Glob account;
  private GlobRepository repository;
  private Directory directory;
  private GlobStringifier seriesStringifier;
  private SeriesEditionDialog seriesEditionDialog;
  private SeriesEditionButtons seriesButtons;

  public SavingsSeriesComponentFactory(Glob account,
                                       String accountName,
                                       GlobRepository repository,
                                       Directory directory,
                                       SeriesEditionDialog seriesEditionDialog,
                                       SeriesEditionButtons seriesButtons) {
    this.account = account;
    this.repository = repository;
    this.directory = directory;
    this.seriesEditionDialog = seriesEditionDialog;
    this.seriesButtons = seriesButtons;
    this.seriesStringifier = directory.get(DescriptionService.class).getStringifier(Series.TYPE);
  }

  public void registerComponents(RepeatCellBuilder cellBuilder, final Glob periodSeriesStat) {

    final Glob series = repository.findLinkTarget(periodSeriesStat, PeriodSeriesStat.SERIES);
    String name = account.get(Account.NAME) + "." + seriesStringifier.toString(series, repository);

    final GlobButtonView seriesNameButton =
      seriesButtons.createSeriesButton(series)
        .setName(name + ".edit");

    cellBuilder.add("seriesName", seriesNameButton.getComponent());

    addAmountButton(name + ".", "observedSeriesAmount", PeriodSeriesStat.AMOUNT, series, cellBuilder, new GlobListFunctor() {
      public void run(GlobList list, GlobRepository repository) {
        directory.get(NavigationService.class).gotoDataForSeries(series);
      }
    });

    addAmountButton(name + ".", "plannedSeriesAmount", PeriodSeriesStat.PLANNED_AMOUNT, series, cellBuilder, new GlobListFunctor() {
      public void run(GlobList list, GlobRepository repository) {
        showSeriesEdition(series);
      }
    });

    Glob fromAccount = repository.findLinkTarget(series, Series.FROM_ACCOUNT);
    Glob toAccount = repository.findLinkTarget(series, Series.TO_ACCOUNT);
    boolean mainAccount = account.get(Account.ID).equals(Account.MAIN_SUMMARY_ACCOUNT_ID);
    Gauge gauge;
    if (fromAccount != null && fromAccount.equals(account)) {
      gauge = BudgetAreaGaugeFactory.createSavingsGauge(!mainAccount);
    }
    else {
      if (toAccount == null || !toAccount.equals(account)) {
        throw new UnexpectedApplicationState("");
      }
      gauge = BudgetAreaGaugeFactory.createSavingsGauge(mainAccount);
    }
    final double multiplier = Account.getMultiplierForInOrOutputOfTheAccount(repository.findLinkTarget(series, Series.FROM_ACCOUNT),
                                                                             repository.findLinkTarget(series, Series.TO_ACCOUNT),
                                                                             account);

    final GlobGaugeView gaugeView =
      new GlobGaugeView(PeriodSeriesStat.TYPE, gauge, BudgetArea.SAVINGS, PeriodSeriesStat.AMOUNT,
                        PeriodSeriesStat.PLANNED_AMOUNT, PeriodSeriesStat.PAST_REMAINING, PeriodSeriesStat.FUTURE_REMAINING,
                        PeriodSeriesStat.PAST_OVERRUN, PeriodSeriesStat.FUTURE_OVERRUN, 
                        GlobMatchers.fieldEquals(PeriodSeriesStat.SERIES, series.get(Series.ID)),
                        repository, directory) {
        protected double getValue(Glob glob, DoubleField field) {
          return multiplier * Math.abs(super.getValue(glob, field));
        }
      };
    gaugeView.setName(name + ".gauge");
    cellBuilder.add("gauge", gaugeView.getComponent());

    cellBuilder.addDisposeListener(gaugeView);
    cellBuilder.addDisposeListener(seriesNameButton);
  }

  private void addAmountButton(String prefixName,
                               String buttonName,
                               DoubleField field,
                               final Glob series,
                               RepeatCellBuilder cellBuilder,
                               final GlobListFunctor callback) {
    String name = prefixName + buttonName;
    final GlobButtonView globButtonView =
      GlobButtonView.init(PeriodSeriesStat.TYPE, repository, directory, getStringifier(series, field), callback)
        .setName(name)
        .setFilter(GlobMatchers.linkedTo(series, PeriodSeriesStat.SERIES));
    cellBuilder.add(buttonName, globButtonView.getComponent());
    cellBuilder.addDisposeListener(globButtonView);
  }

  private GlobListStringifier getStringifier(Glob series, final DoubleField field) {
    final double multiplier = Account.getMultiplierForInOrOutputOfTheAccount(repository.findLinkTarget(series, Series.FROM_ACCOUNT),
                                                                             repository.findLinkTarget(series, Series.TO_ACCOUNT),
                                                                             account);
    return new GlobListStringifier() {
      public String toString(GlobList list, GlobRepository repository) {
        Glob first = list.getFirst();
        if (first == null) {
          return "";
        }
        Double amount = Math.abs(first.get(field));
        return Formatting.DECIMAL_FORMAT.format(Amounts.isNearZero(amount) ? 0 : multiplier * amount);
      }
    };
  }

  private void showSeriesEdition(Glob series) {
    seriesEditionDialog.show(series, directory.get(SelectionService.class).getSelection(Month.TYPE).getValueSet(Month.ID));
  }
}
