package com.budgetview.desktop.budget;

import com.budgetview.desktop.View;
import com.budgetview.desktop.budget.components.NameLabelPopupButton;
import com.budgetview.desktop.budget.components.SeriesOrderManager;
import com.budgetview.desktop.card.NavigationService;
import com.budgetview.desktop.components.JPopupButton;
import com.budgetview.desktop.components.charts.DeltaGauge;
import com.budgetview.desktop.components.charts.Gauge;
import com.budgetview.desktop.components.charts.GlobDeltaGaugeView;
import com.budgetview.desktop.components.charts.GlobGaugeView;
import com.budgetview.desktop.components.filtering.Filterable;
import com.budgetview.desktop.components.highlighting.HighlightUpdater;
import com.budgetview.desktop.components.tips.ShowDetailsTipAction;
import com.budgetview.desktop.description.AmountStringifier;
import com.budgetview.desktop.description.Labels;
import com.budgetview.desktop.model.PeriodBudgetAreaStat;
import com.budgetview.desktop.model.PeriodSeriesStat;
import com.budgetview.desktop.projects.actions.CreateProjectAction;
import com.budgetview.desktop.series.SeriesEditor;
import com.budgetview.desktop.signpost.guides.SeriesAmountSignpost;
import com.budgetview.model.*;
import com.budgetview.shared.model.BudgetArea;
import com.budgetview.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.actions.ToggleBooleanAction;
import org.globsframework.gui.splits.PanelBuilder;
import org.globsframework.gui.splits.SplitsLoader;
import org.globsframework.gui.splits.SplitsNode;
import org.globsframework.gui.splits.repeat.Repeat;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.gui.splits.utils.GlobListener;
import org.globsframework.gui.utils.PopupMenuFactory;
import org.globsframework.gui.views.GlobButtonView;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.model.*;
import org.globsframework.model.format.GlobListStringifier;
import org.globsframework.model.utils.GlobListFunctor;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.model.utils.GlobUtils;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.UnexpectedValue;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.*;
import java.util.List;

public class BudgetAreaSeriesView extends View implements Filterable {
  private String name;
  private BudgetArea budgetArea;
  private Set<Integer> selectedMonthIds = Collections.emptySet();
  private List<Key> currentSeriesStat = Collections.emptyList();

  private Repeat<Glob> seriesRepeat;
  private BudgetAreaStatFilter statFilter;
  private SeriesEditionButtons seriesButtons;
  private SeriesGroupEditionButtons seriesGroupButtons;

  private SeriesOrderManager orderManager;
  private Comparator<Glob> comparator;
  private JMenuItem monthFilteringButton;
  private Collection<SeriesRepeatComponentFactory.SeriesButtonsUpdater> signpostUpdaters =
    new ArrayList<SeriesRepeatComponentFactory.SeriesButtonsUpdater>();

  public static final String IS_GROUP_ELEMENT_PROPERTY = "budgetAreaSeriesView.isGroupElement";

  public BudgetAreaSeriesView(String name,
                              final BudgetArea budgetArea,
                              final GlobRepository repository,
                              Directory directory) {
    super(repository, directory);
    this.name = name;
    this.budgetArea = budgetArea;

    this.orderManager = new SeriesOrderManager(budgetArea, repository, directory) {
      protected void setComparator(Comparator<Glob> newComparator) {
        comparator = newComparator;
        updateRepeat();
      }
    };
    this.comparator = orderManager.getComparator();

    this.seriesButtons = new SeriesEditionButtons(budgetArea, repository, directory);
    this.seriesGroupButtons = new SeriesGroupEditionButtons(repository, directory);

    this.selectionService.addListener(new GlobSelectionListener() {
      public void selectionUpdated(GlobSelection selection) {
        selectedMonthIds = selection.getAll(Month.TYPE).getValueSet(Month.ID);
        statFilter.setSelectedMonthIds(selectedMonthIds);
        updateRepeat();
      }
    }, Month.TYPE);

    repository.addChangeListener(new ChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        if (changeSet.containsChanges(PeriodSeriesStat.TYPE)
            || changeSet.containsChanges(SeriesBudget.TYPE)
            || changeSet.containsChanges(Series.TYPE)) {
          updateRepeat();
        }
      }

      public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
        if (changedTypes.contains(PeriodSeriesStat.TYPE)
            || changedTypes.contains(Series.TYPE)
            || changedTypes.contains(SeriesBudget.TYPE)) {
          updateRepeat();
        }
      }
    });
  }

  public void setFilter(GlobMatcher matcher) {
    statFilter.setAccountMatcher(matcher);
    updateRepeat();
  }

  private void updateRepeat() {
    for (SeriesRepeatComponentFactory.SeriesButtonsUpdater updater : signpostUpdaters) {
      updater.releaseSignpost();
    }
    List<Key> newStat = repository.getAll(PeriodSeriesStat.TYPE, statFilter)
      .sortSelf(comparator)
      .toKeyList();
    seriesRepeat.startUpdate();
    GlobUtils.diff(currentSeriesStat, newStat, new GlobUtils.DiffFunctor<Key>() {
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
    seriesRepeat.updateComplete();
    currentSeriesStat = newStat;

    if (!SignpostStatus.isCompleted(SignpostStatus.SERIES_AMOUNT_SHOWN, repository)) {
      if (!newStat.isEmpty()) {
        Key key = newStat.get(0);
        for (SeriesRepeatComponentFactory.SeriesButtonsUpdater updater : signpostUpdaters) {
          updater.updateSignpost(key);
        }
      }
    }
  }

  public void registerComponents(GlobsPanelBuilder parentBuilder) {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/budget/budgetAreaSeriesView.splits",
                                                      repository, directory);

    Key statKey = Key.create(PeriodBudgetAreaStat.TYPE, budgetArea.getId());

    JLabel actualAmountLabel = new JLabel();
    builder.add("totalActualAmount", actualAmountLabel);
    JLabel plannedAmountLabel = new JLabel();
    builder.add("totalPlannedAmount", plannedAmountLabel);

    GlobGaugeView totalGaugeView =
      new GlobGaugeView(statKey,
                        budgetArea,
                        PeriodBudgetAreaStat.AMOUNT,
                        PeriodBudgetAreaStat.PLANNED_AMOUNT,
                        PeriodBudgetAreaStat.PAST_REMAINING,
                        PeriodBudgetAreaStat.FUTURE_REMAINING,
                        PeriodBudgetAreaStat.PAST_OVERRUN,
                        PeriodBudgetAreaStat.FUTURE_OVERRUN,
                        PeriodBudgetAreaStat.ACTIVE,
                        repository, directory);
    Gauge totalGauge = totalGaugeView.getComponent();
    totalGauge.setActionListener(new ShowDetailsTipAction(totalGauge, directory));
    builder.add("totalGauge", totalGauge);

    BudgetAreaHeaderComponentsUpdater.init(budgetArea, statKey,
                                           actualAmountLabel, plannedAmountLabel,
                                           repository, directory);

    statFilter = new BudgetAreaStatFilter(budgetArea);

    seriesRepeat =
      builder.addRepeat("seriesRepeat", new GlobList(), new SeriesRepeatComponentFactory(builder));

    builder.add("budgetAreaTitle", new JPopupButton(Labels.get(budgetArea), new ActionsPopupFactory()));

    parentBuilder.add(name, builder);
  }

  private JMenuItem createMonthFilteringButton() {
    monthFilteringButton = new JMenuItem();
    monthFilteringButton.setAction(new AbstractAction() {
      public void actionPerformed(ActionEvent actionEvent) {
        statFilter.toggleMonthFilteringEnabled();
        updateMonthFilteringButton();
        updateRepeat();
      }
    });
    updateMonthFilteringButton();
    return monthFilteringButton;
  }

  private void updateMonthFilteringButton() {
    String key =
      statFilter.isMonthFilteringEnabled()
        ? "budgetView.actions.disableMonthFiltering"
        : "budgetView.actions.enableMonthFiltering";
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

    public void registerComponents(PanelBuilder cellBuilder, final Glob periodSeriesStat) {

      final Glob target = PeriodSeriesStat.findTarget(periodSeriesStat, repository);

      final NameLabelPopupButton nameButton = getNameButton(periodSeriesStat, target);
      final SplitsNode<JButton> seriesName = cellBuilder.add("seriesName", nameButton.getComponent());
      cellBuilder.addDisposable(nameButton);

      final SplitsNode<JButton> observedAmountButton = addAmountButton("observedSeriesAmount", PeriodSeriesStat.AMOUNT, periodSeriesStat, cellBuilder, new GlobListFunctor() {
        public void run(GlobList list, GlobRepository repository) {
          switch (PeriodSeriesStat.getSeriesType(periodSeriesStat)) {
            case SERIES:
              directory.get(NavigationService.class).gotoDataForSeries(target);
              break;
            case SERIES_GROUP:
              directory.get(NavigationService.class).gotoDataForSeriesGroup(target);
              break;
          }
        }
      });

      final SplitsNode<JButton> plannedAmountButton = addAmountButton("plannedSeriesAmount", PeriodSeriesStat.PLANNED_AMOUNT, periodSeriesStat, cellBuilder, new GlobListFunctor() {
        public void run(GlobList list, GlobRepository repository) {
          switch (PeriodSeriesStat.getSeriesType(periodSeriesStat)) {
            case SERIES:
              SignpostStatus.setCompleted(SignpostStatus.SERIES_AMOUNT_SHOWN, repository);
              SeriesEditor.get(directory).showAmount(target, selectedMonthIds);
              break;
            case SERIES_GROUP:
              repository.update(target.getKey(), SeriesGroup.EXPANDED, !target.isTrue(SeriesGroup.EXPANDED));
              break;
          }
        }
      });

      final GlobGaugeView gaugeView =
        new GlobGaugeView(periodSeriesStat.getKey(), budgetArea,
                          PeriodSeriesStat.AMOUNT, PeriodSeriesStat.PLANNED_AMOUNT,
                          PeriodSeriesStat.PAST_REMAINING, PeriodSeriesStat.FUTURE_REMAINING,
                          PeriodSeriesStat.PAST_OVERRUN, PeriodSeriesStat.FUTURE_OVERRUN,
                          PeriodSeriesStat.ACTIVE,
                          repository, directory);
      final Gauge gauge = gaugeView.getComponent();
      gauge.setActionListener(new ShowDetailsTipAction(gauge, directory));
      visibles.add(gaugeView);
      if (PeriodSeriesStat.isForSeries(periodSeriesStat)) {
        gaugeView.setDescriptionSource(Key.create(Series.TYPE, periodSeriesStat.get(PeriodSeriesStat.TARGET)),
                                       Series.DESCRIPTION);
      }

      final JButton groupToggle = new JButton();
      switch (PeriodSeriesStat.getSeriesType(periodSeriesStat)) {
        case SERIES:
          groupToggle.setEnabled(false);
          Disposable disposable = GlobListener.install(target.getKey(), repository, new GlobListener.Functor() {
            public void update(Glob series, GlobRepository repository) {
              groupToggle.putClientProperty(IS_GROUP_ELEMENT_PROPERTY, series != null && series.get(Series.GROUP) != null);
            }
          });
          cellBuilder.addDisposable(disposable);
          break;
        case SERIES_GROUP:
          ToggleBooleanAction action = new ToggleBooleanAction(target.getKey(), SeriesGroup.EXPANDED, "-", "+", repository);
          cellBuilder.addDisposable(action);
          groupToggle.setAction(action);
          break;
      }
      cellBuilder.add("groupToggle", groupToggle);

      HighlightUpdater highlightUpdater = new HighlightUpdater(target.getKey(), directory) {
        protected void setHighlighted(boolean highlighted) {
          seriesName.applyStyle(highlighted ? "highlightedAmount" : "standardAmount");
        }
      };
      cellBuilder.addDisposable(highlightUpdater);

      gaugeView.getComponent().setName("gauge");
      cellBuilder.add("gauge", gaugeView.getComponent());
      cellBuilder.addDisposable(gaugeView);
      cellBuilder.addDisposable(new Disposable() {
        public void dispose() {
          visibles.remove(gaugeView);
        }
      });

      GlobDeltaGaugeView deltaGaugeView =
        new GlobDeltaGaugeView(periodSeriesStat.getKey(), budgetArea,
                               PeriodSeriesStat.PREVIOUS_SUMMARY_AMOUNT, PeriodSeriesStat.NEW_SUMMARY_AMOUNT,
                               PeriodSeriesStat.PREVIOUS_SUMMARY_MONTH, PeriodSeriesStat.NEW_SUMMARY_MONTH,
                               repository, directory);
      DeltaGauge deltaGauge = deltaGaugeView.getComponent();
      cellBuilder.add("deltaGauge", deltaGauge);
      deltaGauge.setActionListener(new ShowDetailsTipAction(deltaGauge, directory));
      cellBuilder.addDisposable(deltaGaugeView);

      final SeriesButtonsUpdater updater = new SeriesButtonsUpdater(periodSeriesStat.getKey(),
                                                                    seriesName, observedAmountButton, plannedAmountButton);
      cellBuilder.addDisposable(updater);
      if (budgetArea == BudgetArea.VARIABLE && !SignpostStatus.isCompleted(SignpostStatus.SERIES_AMOUNT_SHOWN, repository)) {
        cellBuilder.addDisposable(new Disposable() {
          public void dispose() {
            signpostUpdaters.remove(updater);
          }
        });
        signpostUpdaters.add(updater);
      }
    }

    private NameLabelPopupButton getNameButton(Glob periodSeriesStat, Glob target) {
      switch (PeriodSeriesStat.getSeriesType(periodSeriesStat)) {
        case SERIES:
          return seriesButtons.createSeriesPopupButton(target);
        case SERIES_GROUP:
          return seriesGroupButtons.createPopupButton(target);
      }
      throw new UnexpectedValue(periodSeriesStat);
    }

    private class SeriesButtonsUpdater implements ChangeSetListener, Disposable {
      private Key key;
      private SplitsNode<JButton> seriesName;
      private SplitsNode<JButton> observedAmountButton;
      private SplitsNode<JButton> plannedAmountButton;
      private SeriesAmountSignpost signpost;

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
        releaseSignpost();
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

      public void updateSignpost(Key key) {
        if (key.equals(this.key)) {
          signpost = new SeriesAmountSignpost(repository, directory);
          signpost.attach(plannedAmountButton.getComponent());
        }
      }

      public void releaseSignpost() {
        if (signpost != null) {
          signpost.delete();
          signpost = null;
        }
      }
    }

    private SplitsNode<JButton> addAmountButton(String name,
                                                DoubleField field,
                                                final Glob periodSeriesStat,
                                                PanelBuilder cellBuilder,
                                                final GlobListFunctor callback) {
      final GlobButtonView amountButtonView =
        GlobButtonView.init(PeriodSeriesStat.TYPE, repository, directory, getStringifier(field), callback)
          .forceSelection(periodSeriesStat.getKey());
      JButton button = amountButtonView.getComponent();
      SplitsNode<JButton> node = cellBuilder.add(name, button);
      cellBuilder.addDisposable(amountButtonView);
      return node;
    }
  }

  private GlobListStringifier getStringifier(DoubleField field) {
    return new UnsetGlobListStringifier(field, AmountStringifier.getForList(field, budgetArea));
  }

  private static class UnsetGlobListStringifier implements GlobListStringifier {
    private final DoubleField field;
    private GlobListStringifier stringifier;

    public UnsetGlobListStringifier(DoubleField field, GlobListStringifier stringifier) {
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

  private class ActionsPopupFactory implements PopupMenuFactory {
    public JPopupMenu createPopup() {
      JPopupMenu menu = new JPopupMenu();
      menu.add(seriesButtons.createSeriesAction());
      if (budgetArea == BudgetArea.EXTRAS && AddOns.isEnabled(AddOns.PROJECTS, repository)) {
        menu.add(new CreateProjectAction(repository, directory));
      }
      menu.addSeparator();
      menu.add(createMonthFilteringButton());
      return menu;
    }
  }
}
