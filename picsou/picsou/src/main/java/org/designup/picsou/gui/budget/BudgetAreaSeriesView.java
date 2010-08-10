package org.designup.picsou.gui.budget;

import org.designup.picsou.gui.View;
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
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.SplitsNode;
import org.globsframework.gui.splits.repeat.Repeat;
import org.globsframework.gui.splits.repeat.RepeatCellBuilder;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.gui.views.GlobButtonView;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.model.*;
import org.globsframework.model.format.GlobListStringifier;
import org.globsframework.model.format.GlobListStringifiers;
import org.globsframework.model.format.GlobStringifier;
import org.globsframework.model.utils.*;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.*;

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
  private OrderManager orderManager;
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

    updateDefaultComparator();

    seriesButtons = new SeriesEditionButtons(budgetArea, repository, directory, seriesEditionDialog);

    selectionService.addListener(new GlobSelectionListener() {
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

  private void updateDefaultComparator() {
    orderManager = new OrderManager();
    repository.addChangeListener(orderManager);
    comparator = orderManager.getComparator();
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
      SeriesOrder seriesOrder = new SeriesOrder(1, 2, orderManager);
      seriesOrder.setStyleNode(builder.add("titleSeries", new JButton(seriesOrder)));
      AmountOrder amountRealOrder = new AmountOrder(PeriodSeriesStat.AMOUNT, 3, 4, orderManager,
                                                    budgetArea.isIncome());
      amountRealOrder.setStyleNode(builder.add("titleAmountReal", new JButton(amountRealOrder)));
      AmountOrder plannedAmountOrder = new AmountOrder(PeriodSeriesStat.PLANNED_AMOUNT, 5, 6, orderManager,
                                                       budgetArea.isIncome());
      plannedAmountOrder.setStyleNode(builder.add("titleAmountPlanned", new JButton(plannedAmountOrder)));
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
      return new ForcedPlusGlobListStringifier(budgetArea,
                                               GlobListStringifiers.sum(field, decimalFormat, !budgetArea.isIncome()));
    }
  }

  private class OrderManager implements ChangeSetListener {
    private List<Order> orders = new ArrayList<Order>();
    private IntegerField field;
    private SplitsNode<JButton> previousNode;

    private OrderManager() {
      if (budgetArea == BudgetArea.INCOME) {
        field = UserPreferences.ORDER_INCOME;
      }
      else if (budgetArea == BudgetArea.RECURRING) {
        field = UserPreferences.ORDER_RECURRING;
      }
      else if (budgetArea == BudgetArea.VARIABLE) {
        field = UserPreferences.ORDER_VARIABLE;
      }
      else if (budgetArea == BudgetArea.EXTRAS) {
        field = UserPreferences.ORDER_EXTRA;
      }
      else if (budgetArea == BudgetArea.SAVINGS) {
        field = UserPreferences.ORDER_SAVINGS;
      }
    }

    public Integer getUserCurrentOrder() {
      Glob glob = repository.find(UserPreferences.KEY);
      if (glob == null) {
        return 0;
      }
      Integer order = glob.get(field);
      if (order == null) {
        return 0;
      }
      return order;
    }

    public void setCurrentOrder(SplitsNode<JButton> node, Integer order) {
      if (previousNode != null && previousNode != node) {
        previousNode.applyStyle("none");
      }
      this.previousNode = node;
      if (order == 0) {
        node.applyStyle("none");
      }
      else {
        node.applyStyle((order % 2) == 0 ? "up" : "down");
      }
      repository.update(UserPreferences.KEY, field, order);
      comparator = getComparator();
      updateRepeat();
    }

    public void setPreviousNode(SplitsNode<JButton> previousNode) {
      this.previousNode = previousNode;
    }

    Comparator<Glob> getComparator() {
      for (Order order : orders) {
        Comparator<Glob> globComparator = order.getComparator();
        if (globComparator != null) {
          return globComparator;
        }
      }
      return Collections.reverseOrder(new GlobFieldComparator(PeriodSeriesStat.ABS_SUM_AMOUNT));
    }

    public void add(Order order) {
      orders.add(order);
    }

    public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    }

    public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
      if (changedTypes.contains(UserPreferences.TYPE)) {
        Glob glob = repository.find(UserPreferences.KEY);
        if (glob != null) {
          for (Order order : orders) {
            order.update();
          }
          comparator = getComparator();
        }
      }
    }
  }

  private abstract class Order extends AbstractAction {
    private OrderManager orderManager;
    private Integer currentOrder = 0;
    private int ordered;
    private int oppositeOrder;
    private SplitsNode<JButton> splitsNode;
    private boolean inverted;

    public Order(int ordered, int oppositeOrder, BudgetAreaSeriesView.OrderManager manager, boolean inverted) {
      this.inverted = inverted;
      manager.add(this);
      this.ordered = ordered;
      this.oppositeOrder = oppositeOrder;
      orderManager = manager;
      Integer order = orderManager.getUserCurrentOrder();
      if (order == ordered) {
        currentOrder = order;
      }
      else if (order == oppositeOrder) {
        currentOrder = order;
      }
    }

    Comparator<Glob> getComparator() {
      Integer currentOrder = orderManager.getUserCurrentOrder();
      if (currentOrder.equals(ordered)) {
        if (inverted) {
          return new InvertedComparator(getOrder());
        }
        else {
          return getOrder();
        }
      }
      if (currentOrder.equals(oppositeOrder)) {
        if (inverted) {
          return getOrder();
        }
        else {
          return new InvertedComparator(getOrder());
        }
      }
      return null;
    }

    public abstract Comparator<Glob> getOrder();

    public void actionPerformed(ActionEvent e) {
      if (currentOrder == 0) {
        currentOrder = ordered;
      }
      else if (currentOrder.equals(orderManager.getUserCurrentOrder())) {
        if (currentOrder == ordered) {
          currentOrder = oppositeOrder;
        }
        else {
          currentOrder = 0;
        }
      }
      orderManager.setCurrentOrder(splitsNode, currentOrder);
    }

    public void setStyleNode(SplitsNode<JButton> splitsNode) {
      this.splitsNode = splitsNode;
    }

    public void update() {
      Integer realOrder = orderManager.getUserCurrentOrder();
      if (realOrder == ordered || realOrder == oppositeOrder){
        currentOrder = realOrder;
        splitsNode.applyStyle((currentOrder % 2) == 0 ? "up" : "down");
        orderManager.setPreviousNode(splitsNode);
      }
      else {
        splitsNode.applyStyle("none");
      }
    }
  }

  private class SeriesOrder extends Order {
    private Comparator<Glob> globComparator;

    public SeriesOrder(int ordered, int oppositeOrder, final OrderManager orderManager) {
      super(ordered, oppositeOrder, orderManager, false);
      GlobStringifier stringifier = descriptionService.getStringifier(Series.TYPE);
      final Comparator<Glob> seriesComparator = stringifier.getComparator(repository);
      globComparator = new Comparator<Glob>() {
        public int compare(Glob o1, Glob o2) {
          Glob s1 = repository.findLinkTarget(o1, PeriodSeriesStat.SERIES);
          Glob s2 = repository.findLinkTarget(o2, PeriodSeriesStat.SERIES);
          return seriesComparator.compare(s1, s2);
        }
      };
    }

    public Comparator<Glob> getOrder() {
      return globComparator;
    }
  }

  private class AmountOrder extends Order {
    private DoubleField field;

    public AmountOrder(DoubleField field, int ordered, int oppositeOrder, OrderManager manager, boolean invert) {
      super(ordered, oppositeOrder, manager, invert);
      this.field = field;
    }

    public Comparator<Glob> getOrder() {
      return new GlobFieldComparator(field);
    }
  }

  private class InvertedComparator implements Comparator<Glob> {
    private Comparator<Glob> comparator;

    public InvertedComparator(Comparator<Glob> comparator) {
      this.comparator = comparator;
    }

    public int compare(Glob o1, Glob o2) {
      return comparator.compare(o2, o1);
    }
  }
}
