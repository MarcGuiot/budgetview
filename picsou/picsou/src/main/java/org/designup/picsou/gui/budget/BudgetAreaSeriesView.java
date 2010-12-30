package org.designup.picsou.gui.budget;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.budget.components.SeriesOrderManager;
import org.designup.picsou.gui.budget.footers.BudgetAreaSeriesFooter;
import org.designup.picsou.gui.card.NavigationService;
import org.designup.picsou.gui.components.TextDisplay;
import org.designup.picsou.gui.components.charts.BudgetAreaGaugeFactory;
import org.designup.picsou.gui.components.charts.Gauge;
import org.designup.picsou.gui.components.charts.GlobGaugeView;
import org.designup.picsou.gui.components.tips.DetailsTipFactory;
import org.designup.picsou.gui.description.ForcedPlusGlobListStringifier;
import org.designup.picsou.gui.model.PeriodSeriesStat;
import org.designup.picsou.gui.series.SeriesAmountEditionDialog;
import org.designup.picsou.gui.series.SeriesEditionDialog;
import org.designup.picsou.gui.signpost.Signpost;
import org.designup.picsou.gui.signpost.guides.SeriesAmountSignpost;
import org.designup.picsou.gui.signpost.guides.SeriesGaugeSignpost;
import org.designup.picsou.gui.signpost.guides.SeriesPeriodicitySignpost;
import org.designup.picsou.gui.utils.Matchers;
import org.designup.picsou.model.*;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.SplitsLoader;
import org.globsframework.gui.splits.SplitsNode;
import org.globsframework.gui.splits.repeat.Repeat;
import org.globsframework.gui.splits.repeat.RepeatCellBuilder;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.gui.views.GlobButtonView;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.model.*;
import org.globsframework.model.format.GlobListStringifier;
import org.globsframework.model.format.GlobListStringifiers;
import org.globsframework.model.utils.*;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class BudgetAreaSeriesView extends View {
  private String name;
  private BudgetArea budgetArea;
  private Set<Integer> selectedMonthIds = Collections.emptySet();
  private Matchers.SeriesFirstEndDateFilter seriesDateFilter;
  private List<Key> currentSeries = Collections.emptyList();

  private BudgetAreaSeriesFooter footerGenerator;

  private Repeat<Glob> seriesRepeat;
  private GlobMatcher seriesFilter;
  private SeriesEditionButtons seriesButtons;
  private JEditorPane footerArea = GuiUtils.createReadOnlyHtmlComponent();

  private SeriesAmountEditionDialog seriesAmountEditionDialog;
  private SeriesOrderManager orderManager;
  private Comparator<Glob> comparator;

  public BudgetAreaSeriesView(String name,
                              final BudgetArea budgetArea,
                              final GlobRepository repository,
                              Directory directory,
                              BudgetAreaSeriesFooter footerGenerator,
                              final SeriesEditionDialog seriesEditionDialog,
                              final SeriesAmountEditionDialog seriesAmountEditionDialog) {
    super(repository, directory);
    this.name = name;
    this.budgetArea = budgetArea;
    this.footerGenerator = footerGenerator;
    this.seriesAmountEditionDialog = seriesAmountEditionDialog;

    this.orderManager = new SeriesOrderManager(budgetArea, repository, directory) {
      protected void setComparator(Comparator<Glob> newComparator) {
        comparator = newComparator;
        updateRepeat();
      }
    };
    this.comparator = orderManager.getComparator();

    this.seriesButtons = new SeriesEditionButtons(budgetArea, repository, directory, seriesEditionDialog);

    this.selectionService.addListener(new GlobSelectionListener() {
      public void selectionUpdated(GlobSelection selection) {
        selectedMonthIds = selection.getAll(Month.TYPE).getValueSet(Month.ID);
        seriesDateFilter.filterDates(selectedMonthIds);
        updateRepeat();
      }
    }, Month.TYPE);

    repository.addChangeListener(new ChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        if (changeSet.containsChanges(PeriodSeriesStat.TYPE) ||
            changeSet.containsChanges(SeriesBudget.TYPE)
            || changeSet.containsChanges(Series.TYPE)) {
          updateRepeat();
        }
      }

      public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
        if (changedTypes.contains(PeriodSeriesStat.TYPE) || changedTypes.contains(Series.TYPE)
            || changedTypes.contains(SeriesBudget.TYPE)) {
          updateRepeat();
        }
      }
    });
  }

  private void updateRepeat() {
    List<Key> newSeries = repository.getAll(PeriodSeriesStat.TYPE, seriesFilter)
      .sort(comparator)
      .toKeyList();
    GlobUtils.diff(currentSeries, newSeries, new GlobUtils.DiffFunctor<Key>() {
      public void add(Key key, int index) {
        seriesRepeat.insert(repository.get(key), index);
      }

      public void remove(int index) {
        seriesRepeat.remove(index);
      }

      public void move(int previousIndex, int newIndex) {
        seriesRepeat.move(previousIndex, newIndex);
      }
    });
    currentSeries = newSeries;
    footerGenerator.update(currentSeries);
  }

  public void registerComponents(GlobsPanelBuilder parentBuilder) {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/budget/budgetAreaSeriesView.splits",
                                                      repository, directory);

    builder.add("budgetAreaTitle", new JLabel(budgetArea.getLabel()));
    JLabel amountLabel = builder.add("totalObservedAmount", new JLabel()).getComponent();
    JLabel plannedLabel = builder.add("totalPlannedAmount", new JLabel()).getComponent();

    Gauge gauge = BudgetAreaGaugeFactory.createGauge(budgetArea);
    gauge.enableDetailsTips(new DetailsTipFactory(repository, directory));
    builder.add("totalGauge", gauge);

    BudgetAreaHeaderUpdater headerUpdater =
      new BudgetAreaHeaderUpdater(TextDisplay.create(amountLabel), TextDisplay.create(plannedLabel), gauge,
                                  repository, directory);
    headerUpdater.setColors("block.total",
                            "block.total.overrun.error",
                            "block.total.overrun.positive");

    BudgetAreaHeader.init(budgetArea, headerUpdater, repository, directory);

    seriesRepeat =
      builder.addRepeat("seriesRepeat", new GlobList(), new SeriesRepeatComponentFactory(builder));

    seriesButtons.registerButtons(builder);

    parentBuilder.add(name, builder);
    if (budgetArea == BudgetArea.SAVINGS) {
      seriesDateFilter =
        Matchers.seriesDateSavingsAndAccountFilter(Account.MAIN_SUMMARY_ACCOUNT_ID);
    }
    else {
      seriesDateFilter = Matchers.seriesDateFilter(budgetArea.getId(), false);
    }

    seriesFilter = new GlobMatcher() {
      public boolean matches(Glob periodSeriesStat, GlobRepository repository) {
        Glob series = repository.findLinkTarget(periodSeriesStat, PeriodSeriesStat.SERIES);
        if (series == null) {
          return false;
        }
        ReadOnlyGlobRepository.MultiFieldIndexed seriesBudgetIndex =
          repository.findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES, series.get(Series.ID));
        int notActive = 0;
        for (Integer monthId : selectedMonthIds) {
          GlobList seriesBudget =
            seriesBudgetIndex.findByIndex(SeriesBudget.MONTH, monthId).getGlobs();
          if (seriesBudget.size() == 0 || !seriesBudget.getFirst().isTrue(SeriesBudget.ACTIVE)) {
            notActive++;
          }
        }
        return !(selectedMonthIds.size() == notActive) && seriesDateFilter.matches(series, repository);
      }
    };

    footerGenerator.init(footerArea);
    builder.add("footerArea", footerArea);
  }

  private class SeriesRepeatComponentFactory implements RepeatComponentFactory<Glob> {
    List<GlobGaugeView> visibles = new ArrayList<GlobGaugeView>();

    private SeriesRepeatComponentFactory(GlobsPanelBuilder builder) {
      orderManager.registerSeriesNameButton(builder, "titleSeries");
      orderManager.registerRealAmountButton(builder, "titleAmountReal");
      orderManager.registerPlannedAmountButton(builder, "titleAmountPlanned");

      builder.addLoader(new SplitsLoader() {
        public void load(Component component, SplitsNode node) {
          orderManager.init();
        }
      });
    }

    public void registerComponents(RepeatCellBuilder cellBuilder, final Glob periodSeriesStat) {

      final Glob series = repository.findLinkTarget(periodSeriesStat, PeriodSeriesStat.SERIES);

      GlobButtonView seriesNameButton = seriesButtons.createSeriesButton(series);
      cellBuilder.add("seriesName", seriesNameButton.getComponent());
      cellBuilder.addDisposeListener(seriesNameButton);

      addAmountButton("observedSeriesAmount", PeriodSeriesStat.AMOUNT, series, cellBuilder, new GlobListFunctor() {
        public void run(GlobList list, GlobRepository repository) {
          directory.get(NavigationService.class).gotoDataForSeries(series);
        }
      });

      JButton amountButton = addAmountButton("plannedSeriesAmount", PeriodSeriesStat.PLANNED_AMOUNT, series, cellBuilder, new GlobListFunctor() {
        public void run(GlobList list, GlobRepository repository) {
          SignpostStatus.setCompleted(SignpostStatus.SERIES_AMOUNT_SHOWN, repository);
          seriesAmountEditionDialog.show(series, selectedMonthIds);
        }
      });

      final GlobGaugeView gaugeView =
        new GlobGaugeView(PeriodSeriesStat.TYPE, budgetArea, PeriodSeriesStat.AMOUNT,
                          PeriodSeriesStat.PLANNED_AMOUNT,
                          PeriodSeriesStat.PAST_REMAINING, PeriodSeriesStat.FUTURE_REMAINING,
                          PeriodSeriesStat.PAST_OVERRUN, PeriodSeriesStat.FUTURE_OVERRUN,
                          GlobMatchers.fieldEquals(PeriodSeriesStat.SERIES, series.get(Series.ID)),
                          repository, directory);
      visibles.add(gaugeView);
      cellBuilder.add("gauge", gaugeView.getComponent());
      cellBuilder.addDisposeListener(gaugeView);
      cellBuilder.addDisposeListener(new Disposable() {
        public void dispose() {
          visibles.remove(gaugeView);
        }
      });

      if (SignpostStatus.isPeriodicitySeries(repository, series.getKey())) {
        Signpost signpost = new SeriesPeriodicitySignpost(repository, directory);
        cellBuilder.addDisposeListener(signpost);
        signpost.attach(seriesNameButton.getComponent());
      }

      if (SignpostStatus.isAmountSeries(repository, series.getKey())) {
        Signpost gaugeSignpost = new SeriesGaugeSignpost(repository, directory);
        cellBuilder.addDisposeListener(gaugeSignpost);
        gaugeSignpost.attach(gaugeView.getComponent());

        Signpost amountSignpost = new SeriesAmountSignpost(repository, directory);
        cellBuilder.addDisposeListener(amountSignpost);
        amountSignpost.attach(amountButton);
      }
    }

    private JButton addAmountButton(String name,
                                    DoubleField field,
                                    final Glob series,
                                    RepeatCellBuilder cellBuilder,
                                    final GlobListFunctor callback) {
      final GlobButtonView amountButtonView =
        GlobButtonView.init(PeriodSeriesStat.TYPE, repository, directory, getStringifier(field), callback)
          .setFilter(GlobMatchers.linkedTo(series, PeriodSeriesStat.SERIES));
      JButton button = amountButtonView.getComponent();
      cellBuilder.add(name, button);
      cellBuilder.addDisposeListener(amountButtonView);
      return button;
    }

    private GlobListStringifier getStringifier(final DoubleField field) {
      ForcedPlusGlobListStringifier plusGlobListStringifier = new ForcedPlusGlobListStringifier(budgetArea,
                                                                                                GlobListStringifiers.sum(field, decimalFormat, !budgetArea.isIncome()));
      return new UnsetGlobListStringifier(field, plusGlobListStringifier);
    }

    private class UnsetGlobListStringifier implements GlobListStringifier {
      private final DoubleField field;
      private ForcedPlusGlobListStringifier stringifier;

      public UnsetGlobListStringifier(DoubleField field, ForcedPlusGlobListStringifier stringifier) {
        this.field = field;
        this.stringifier = stringifier;
      }

      public String toString(GlobList list, GlobRepository repository) {
        for (Glob glob : list) {
          if (glob.get(field) == null){
            return Lang.get("gauge.plannetUnset");
          }
        }
        return stringifier.toString(list, repository);
      }
    }
  }
}
