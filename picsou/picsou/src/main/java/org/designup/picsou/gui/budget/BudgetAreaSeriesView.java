package org.designup.picsou.gui.budget;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.budget.components.SeriesOrderManager;
import org.designup.picsou.gui.budget.footers.BudgetAreaSeriesFooter;
import org.designup.picsou.gui.card.NavigationService;
import org.designup.picsou.gui.components.JPopupButton;
import org.designup.picsou.gui.components.TextDisplay;
import org.designup.picsou.gui.components.charts.*;
import org.designup.picsou.gui.components.highlighting.HighlightUpdater;
import org.designup.picsou.gui.components.tips.ShowDetailsTipAction;
import org.designup.picsou.gui.components.utils.BlankAction;
import org.designup.picsou.gui.description.stringifiers.ForcedPlusGlobListStringifier;
import org.designup.picsou.gui.model.PeriodBudgetAreaStat;
import org.designup.picsou.gui.model.PeriodSeriesStat;
import org.designup.picsou.gui.projects.actions.CreateProjectAction;
import org.designup.picsou.gui.savings.ToggleToSavingsAction;
import org.designup.picsou.gui.series.SeriesEditor;
import org.designup.picsou.gui.signpost.Signpost;
import org.designup.picsou.gui.signpost.guides.SavingsViewToggleSignpost;
import org.designup.picsou.gui.signpost.guides.SeriesAmountSignpost;
import org.designup.picsou.gui.utils.Matchers;
import org.designup.picsou.gui.utils.MonthMatcher;
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
import org.globsframework.model.utils.GlobListFunctor;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.model.utils.GlobUtils;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.*;
import java.util.List;

public class BudgetAreaSeriesView extends View {
  private String name;
  private BudgetArea budgetArea;
  private Set<Integer> selectedMonthIds = Collections.emptySet();
  private MonthMatcher seriesDateFilter;
  private boolean monthFilteringEnabled = true;
  private List<Key> currentSeries = Collections.emptyList();

  private BudgetAreaSeriesFooter footerGenerator;

  private Repeat<Glob> seriesRepeat;
  private GlobMatcher seriesFilter;
  private SeriesEditionButtons seriesButtons;
  private JEditorPane footerArea = GuiUtils.createReadOnlyHtmlComponent();

  private SeriesOrderManager orderManager;
  private Comparator<Glob> comparator;
  private JMenuItem monthFilteringButton;
  private Collection<SeriesRepeatComponentFactory.SeriesButtonsUpdater> updaters =
    new ArrayList<SeriesRepeatComponentFactory.SeriesButtonsUpdater>();

  public BudgetAreaSeriesView(String name,
                              final BudgetArea budgetArea,
                              final GlobRepository repository,
                              Directory directory,
                              BudgetAreaSeriesFooter footerGenerator) {
    super(repository, directory);
    this.name = name;
    this.budgetArea = budgetArea;
    this.footerGenerator = footerGenerator;

    this.orderManager = new SeriesOrderManager(budgetArea, repository, directory) {
      protected void setComparator(Comparator<Glob> newComparator) {
        comparator = newComparator;
        updateRepeat();
      }
    };
    this.comparator = orderManager.getComparator();

    this.seriesButtons = new SeriesEditionButtons(budgetArea, repository, directory);

    this.selectionService.addListener(new GlobSelectionListener() {
      public void selectionUpdated(GlobSelection selection) {
        selectedMonthIds = selection.getAll(Month.TYPE).getValueSet(Month.ID);
        seriesDateFilter.filterMonths(selectedMonthIds);
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
    for (SeriesRepeatComponentFactory.SeriesButtonsUpdater updater : updaters) {
      updater.releaseSignPost();
    }
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

    if (!SignpostStatus.isCompleted(SignpostStatus.SERIES_AMOUNT_SHOWN, repository)) {
      if (!newSeries.isEmpty()) {
        Key key = newSeries.get(0);
        for (SeriesRepeatComponentFactory.SeriesButtonsUpdater updater : updaters) {
          updater.updateSignPost(key);
        }
      }
    }
  }

  public void registerComponents(GlobsPanelBuilder parentBuilder) {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/budget/budgetAreaSeriesView.splits",
                                                      repository, directory);

    builder.add("budgetAreaTitle", new JLabel(budgetArea.getLabel()));
    JLabel amountLabel = builder.add("totalObservedAmount", new JLabel()).getComponent();
    JLabel plannedLabel = builder.add("totalPlannedAmount", new JLabel()).getComponent();

    Gauge gauge = BudgetAreaGaugeFactory.createGauge(budgetArea);
    gauge.setActionListener(new ShowDetailsTipAction(gauge, directory));
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

    JPopupMenu menu = new JPopupMenu();
    menu.add(seriesButtons.createSeriesAction());
    if (budgetArea == BudgetArea.EXTRAS) {
      menu.add(new CreateProjectAction(directory));
    }
    menu.addSeparator();
    menu.add(createMonthFilteringButton());
    builder.add("seriesActions", new JPopupButton(Lang.get("budgetView.actions"), menu));

    parentBuilder.add(name, builder);
    if (budgetArea == BudgetArea.SAVINGS) {
      seriesDateFilter =
        Matchers.seriesDateSavingsAndAccountFilter(Account.MAIN_SUMMARY_ACCOUNT_ID);
    }
    else {
      seriesDateFilter = Matchers.seriesActiveInPeriod(budgetArea.getId(), false);
    }

    seriesFilter = new GlobMatcher() {
      public boolean matches(Glob periodSeriesStat, GlobRepository repository) {
        Glob series = repository.findLinkTarget(periodSeriesStat, PeriodSeriesStat.SERIES);
        if (series == null) {
          return false;
        }

        if (!monthFilteringEnabled) {
          return budgetArea.getId().equals(series.get(Series.BUDGET_AREA));
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
        boolean activeMonthsInPeriod = !(selectedMonthIds.size() == notActive);
        return activeMonthsInPeriod && seriesDateFilter.matches(series, repository);
      }
    };

    footerGenerator.init(footerArea);
    builder.add("footerArea", footerArea);

    builder.add("specificAction", getSpecificActionButton());
  }

  private JButton getSpecificActionButton() {
    if (budgetArea.equals(BudgetArea.SAVINGS)) {
      JButton button = new JButton(new ToggleToSavingsAction(repository, directory));
      Signpost signpost = new SavingsViewToggleSignpost(repository, directory);
      signpost.attach(button);
      return button;
    }
    return new JButton(new BlankAction());
  }

  private JMenuItem createMonthFilteringButton() {
    monthFilteringButton = new JMenuItem();
    monthFilteringButton.setAction(new AbstractAction() {
      public void actionPerformed(ActionEvent actionEvent) {
        monthFilteringEnabled = !monthFilteringEnabled;
        updateMonthFilteringButton();
        updateRepeat();
      }
    });
    updateMonthFilteringButton();
    return monthFilteringButton;
  }

  private void updateMonthFilteringButton() {
    String key =
      monthFilteringEnabled ? "budgetView.actions.disableMonthFiltering" : "budgetView.actions.enableMonthFiltering";
    monthFilteringButton.setText(Lang.get(key));
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

      final GlobButtonView seriesNameButton = seriesButtons.createSeriesButton(series);
      final SplitsNode<JButton> seriesName = cellBuilder.add("seriesName", seriesNameButton.getComponent());
      cellBuilder.addDisposeListener(seriesNameButton);

      final SplitsNode<JButton> observedAmountButton = addAmountButton("observedSeriesAmount", PeriodSeriesStat.AMOUNT, series, cellBuilder, new GlobListFunctor() {
        public void run(GlobList list, GlobRepository repository) {
          directory.get(NavigationService.class).gotoDataForSeries(series);
        }
      });

      final SplitsNode<JButton> plannedAmountButton = addAmountButton("plannedSeriesAmount", PeriodSeriesStat.PLANNED_AMOUNT, series, cellBuilder, new GlobListFunctor() {
        public void run(GlobList list, GlobRepository repository) {
          SignpostStatus.setCompleted(SignpostStatus.SERIES_AMOUNT_SHOWN, repository);
          SeriesEditor.get(directory).showAmount(series, selectedMonthIds);
        }
      });

      final SeriesButtonsUpdater updater = new SeriesButtonsUpdater(periodSeriesStat.getKey(),
                                                                    seriesName,
                                                                    observedAmountButton,
                                                                    plannedAmountButton);
      cellBuilder.addDisposeListener(updater);
      if (budgetArea == BudgetArea.VARIABLE && !SignpostStatus.isCompleted(SignpostStatus.SERIES_AMOUNT_SHOWN, repository)) {
        cellBuilder.addDisposeListener(new Disposable() {
          public void dispose() {
            updaters.remove(updater);
          }
        });
        updaters.add(updater);
      }

      final GlobGaugeView gaugeView =
        new GlobGaugeView(PeriodSeriesStat.TYPE, budgetArea,
                          PeriodSeriesStat.AMOUNT, PeriodSeriesStat.PLANNED_AMOUNT,
                          PeriodSeriesStat.PAST_REMAINING, PeriodSeriesStat.FUTURE_REMAINING,
                          PeriodSeriesStat.PAST_OVERRUN, PeriodSeriesStat.FUTURE_OVERRUN,
                          PeriodSeriesStat.ACTIVE,
                          GlobMatchers.fieldEquals(PeriodSeriesStat.SERIES, series.get(Series.ID)),
                          repository, directory);
      final Gauge gauge = gaugeView.getComponent();
      gauge.setActionListener(new ShowDetailsTipAction(gauge, directory));
      gaugeView
        .setMaxValueUpdater(Key.create(PeriodBudgetAreaStat.TYPE, budgetArea.getId()),
                            PeriodBudgetAreaStat.ABS_SUM_AMOUNT)
        .setDescriptionSource(series.getKey(), Series.DESCRIPTION);
      visibles.add(gaugeView);

      HighlightUpdater highlightUpdater = new HighlightUpdater(series.getKey(), directory) {
        protected void setHighlighted(boolean highlighted) {
          seriesName.applyStyle(highlighted ? "highlightedAmount" : "standardAmount");
        }
      };
      cellBuilder.addDisposeListener(highlightUpdater);

      cellBuilder.add("gauge", gaugeView.getComponent());
      cellBuilder.addDisposeListener(gaugeView);
      cellBuilder.addDisposeListener(new Disposable() {
        public void dispose() {
          visibles.remove(gaugeView);
        }
      });

      GlobDeltaGaugeView deltaGaugeView =
        new GlobDeltaGaugeView(Key.create(PeriodSeriesStat.TYPE, series.get(Series.ID)), budgetArea,
                               PeriodSeriesStat.PREVIOUS_SUMMARY_AMOUNT, PeriodSeriesStat.NEW_SUMMARY_AMOUNT,
                               PeriodSeriesStat.PREVIOUS_SUMMARY_MONTH, PeriodSeriesStat.NEW_SUMMARY_MONTH,
                               repository, directory);
      DeltaGauge deltaGauge = deltaGaugeView.getComponent();
      cellBuilder.add("deltaGauge", deltaGauge);
      deltaGauge.setActionListener(new ShowDetailsTipAction(deltaGauge, directory));
      cellBuilder.addDisposeListener(deltaGaugeView);
    }

    private class SeriesButtonsUpdater implements ChangeSetListener, Disposable {
      private Key key;
      private SplitsNode<JButton> seriesName;
      private SplitsNode<JButton> observedAmountButton;
      private SplitsNode<JButton> plannedAmountButton;
      private Signpost signpost;

      public SeriesButtonsUpdater(Key key,
                                  SplitsNode<JButton> seriesName,
                                  SplitsNode<JButton> observedAmountButton,
                                  SplitsNode<JButton> plannedAmountButton) {
        this.key = key;
        this.seriesName = seriesName;
        this.observedAmountButton = observedAmountButton;
        this.plannedAmountButton = plannedAmountButton;
        update();
        repository.addChangeListener(this);
      }

      public void dispose() {
        repository.removeChangeListener(this);
        if (signpost != null) {
          signpost.hide();
          signpost = null;
        }
      }

      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        if (changeSet.containsChanges(key)) {
          update();
        }
      }

      public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
        if (changedTypes.contains(PeriodSeriesStat.TYPE)) {
          update();
        }
      }

      private void update() {
        Glob stat = repository.find(key);
        if (stat == null) {
          return;
        }

        boolean active = stat.isTrue(PeriodSeriesStat.ACTIVE);
        seriesName.applyStyle(active ? "seriesEnabled" : "seriesDisabled");
        observedAmountButton.getComponent().setEnabled(active);
        plannedAmountButton.getComponent().setEnabled(active);

        if (!active) {
          observedAmountButton.getComponent().setText("-");
          plannedAmountButton.getComponent().setText("-");
        }

        boolean toSet = active && stat.isTrue(PeriodSeriesStat.TO_SET);
        plannedAmountButton.applyStyle(toSet ? "plannedToSet" : "plannedAlreadySet");
      }

      public void updateSignPost(Key key) {
        if (key.equals(this.key)) {
          signpost = new SeriesAmountSignpost(repository, directory);
          signpost.attach(plannedAmountButton.getComponent());
        }
      }

      public void releaseSignPost() {
        if (signpost != null) {
          signpost.hide();
          signpost = null;
        }
      }
    }

    private SplitsNode<JButton> addAmountButton(String name,
                                                DoubleField field,
                                                final Glob series,
                                                RepeatCellBuilder cellBuilder,
                                                final GlobListFunctor callback) {
      final GlobButtonView amountButtonView =
        GlobButtonView.init(PeriodSeriesStat.TYPE, repository, directory, getStringifier(field), callback)
          .setFilter(GlobMatchers.linkedTo(series, PeriodSeriesStat.SERIES));
      JButton button = amountButtonView.getComponent();
      SplitsNode<JButton> node = cellBuilder.add(name, button);
      cellBuilder.addDisposeListener(amountButtonView);
      return node;
    }

    private GlobListStringifier getStringifier(final DoubleField field) {
      ForcedPlusGlobListStringifier plusGlobListStringifier =
        new ForcedPlusGlobListStringifier(budgetArea,
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

      public String toString(GlobList periodStatList, GlobRepository repository) {
        for (Glob periodStat : periodStatList) {
          if (!periodStat.isTrue(PeriodSeriesStat.ACTIVE)) {
            return "-";
          }
        }
        for (Glob periodStat : periodStatList) {
          if (periodStat.get(field) == null) {
            return Lang.get("gauge.plannetUnset");
          }
        }
        return stringifier.toString(periodStatList, repository);
      }
    }
  }
}
