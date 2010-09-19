package org.designup.picsou.gui.budget.components;

import org.designup.picsou.gui.model.PeriodSeriesStat;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.SeriesOrder;
import org.designup.picsou.model.UserPreferences;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.SplitsNode;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.model.ChangeSet;
import org.globsframework.model.ChangeSetListener;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.model.format.GlobStringifier;
import org.globsframework.model.utils.GlobFieldComparator;
import org.globsframework.utils.InvertedComparator;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.InvalidParameter;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.*;

public abstract class SeriesOrderManager implements ChangeSetListener {
  private List<Order> orders = new ArrayList<Order>();
  private IntegerField field;
  private SplitsNode<JButton> previousNode;
  private BudgetArea budgetArea;
  private GlobRepository repository;
  private DescriptionService descriptionService;

  public SeriesOrderManager(BudgetArea budgetArea, GlobRepository repository, Directory directory) {
    this.budgetArea = budgetArea;
    this.field = getField(budgetArea);
    this.repository = repository;
    this.descriptionService = directory.get(DescriptionService.class);
    this.repository.addChangeListener(this);
  }

  private IntegerField getField(BudgetArea budgetArea) {
    switch (budgetArea) {
      case INCOME:
        return UserPreferences.SERIES_ORDER_INCOME;
      case RECURRING:
        return UserPreferences.SERIES_ORDER_RECURRING;
      case VARIABLE:
        return UserPreferences.SERIES_ORDER_VARIABLE;
      case EXTRAS:
        return UserPreferences.SERIES_ORDER_EXTRA;
      case SAVINGS:
        return UserPreferences.SERIES_ORDER_SAVINGS;
      default:
        throw new InvalidParameter("Unexpected budgetArea " + budgetArea);
    }
  }

  public SeriesOrder getUserCurrentOrder() {
    Glob glob = repository.find(UserPreferences.KEY);
    if (glob == null) {
      return SeriesOrder.DEFAULT;
    }
    Integer order = glob.get(field);
    if (order == null) {
      return SeriesOrder.DEFAULT;
    }
    return SeriesOrder.get(order);
  }

  public void setCurrentOrder(SplitsNode<JButton> node, SeriesOrder order) {
    if (previousNode != null && previousNode != node) {
      previousNode.applyStyle("none");
    }
    this.previousNode = node;
    if (order == SeriesOrder.DEFAULT) {
      node.applyStyle("none");
    }
    else {
      node.applyStyle(order.isAscending() ? "up" : "down");
    }

    repository.update(UserPreferences.KEY, field, order.getId());

    setComparator(getComparator());
  }

  protected abstract void setComparator(Comparator<Glob> comparator);

  public void setPreviousNode(SplitsNode<JButton> previousNode) {
    this.previousNode = previousNode;
  }

  public Comparator<Glob> getComparator() {
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
      Glob userPrefs = repository.find(UserPreferences.KEY);
      if (userPrefs != null) {
        for (Order order : orders) {
          order.update();
        }
        setComparator(getComparator());
      }
    }
  }

  public void registerSeriesNameButton(GlobsPanelBuilder builder, String name) {
    registerOrder(builder, name, new SeriesNameOrder(SeriesOrder.NAME_ASCENDING,
                                                     SeriesOrder.NAME_DESCENDING,
                                                     this));
  }

  public void registerRealAmountButton(GlobsPanelBuilder builder, String name) {
    registerOrder(builder, name, new AmountOrder(PeriodSeriesStat.AMOUNT,
                                                 SeriesOrder.REAL_AMOUNT_ASCENDING,
                                                 SeriesOrder.REAL_AMOUNT_DESCENDING,
                                                 this,
                                                 budgetArea.isIncome()));
  }

  public void registerPlannedAmountButton(GlobsPanelBuilder builder, String name) {
    registerOrder(builder, name, new AmountOrder(PeriodSeriesStat.PLANNED_AMOUNT,
                                                 SeriesOrder.PLANNED_AMOUNT_ASCENDING,
                                                 SeriesOrder.PLANNED_AMOUNT_DESCENDING,
                                                 this,
                                                 budgetArea.isIncome()));
  }

  private void registerOrder(GlobsPanelBuilder builder, String name, Order order) {
    order.setStyleNode(builder.add(name, new JButton(order)));
  }

  public void init() {
    for (Order order : orders) {
      order.update();
    }
  }

  private abstract class Order extends AbstractAction {
    private SeriesOrderManager orderManager;
    private SeriesOrder currentOrder;
    private SeriesOrder order;
    private SeriesOrder oppositeOrder;
    private SplitsNode<JButton> splitsNode;
    private boolean inverted;

    public Order(SeriesOrder order, SeriesOrder oppositeOrder, SeriesOrderManager manager, boolean inverted) {
      this.order = order;
      this.oppositeOrder = oppositeOrder;
      this.inverted = inverted;

      orderManager = manager;
      manager.add(this);

      currentOrder = orderManager.getUserCurrentOrder();
    }

    Comparator<Glob> getComparator() {
      SeriesOrder currentOrder = orderManager.getUserCurrentOrder();
      if (currentOrder.equals(order)) {
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
      if (currentOrder == SeriesOrder.DEFAULT) {
        currentOrder = order;
      }
      else if (currentOrder.equals(orderManager.getUserCurrentOrder())) {
        if (currentOrder == order) {
          currentOrder = oppositeOrder;
        }
        else {
          currentOrder = SeriesOrder.DEFAULT;
        }
      }
      orderManager.setCurrentOrder(splitsNode, currentOrder);
    }

    public void setStyleNode(SplitsNode<JButton> splitsNode) {
      this.splitsNode = splitsNode;
    }

    public void update() {
      SeriesOrder realOrder = orderManager.getUserCurrentOrder();
      if (realOrder == order || realOrder == oppositeOrder) {
        currentOrder = realOrder;
        splitsNode.applyStyle(currentOrder.isAscending() ? "up" : "down");
        orderManager.setPreviousNode(splitsNode);
      }
      else {
        splitsNode.applyStyle("none");
      }
    }
  }

  private class SeriesNameOrder extends Order {
    private Comparator<Glob> globComparator;

    public SeriesNameOrder(SeriesOrder order, SeriesOrder oppositeOrder, final SeriesOrderManager orderManager) {
      super(order, oppositeOrder, orderManager, false);
      GlobStringifier stringifier = descriptionService.getStringifier(Series.TYPE);
      final Comparator<Glob> seriesComparator = stringifier.getComparator(repository);
      globComparator = new Comparator<Glob>() {
        public int compare(Glob o1, Glob o2) {
          Glob series1 = repository.findLinkTarget(o1, PeriodSeriesStat.SERIES);
          Glob series2 = repository.findLinkTarget(o2, PeriodSeriesStat.SERIES);
          return seriesComparator.compare(series1, series2);
        }
      };
    }

    public Comparator<Glob> getOrder() {
      return globComparator;
    }
  }

  private class AmountOrder extends Order {
    private DoubleField field;

    public AmountOrder(DoubleField field,
                       SeriesOrder order,
                       SeriesOrder oppositeOrder,
                       SeriesOrderManager manager,
                       boolean invert) {
      super(order, oppositeOrder, manager, invert);
      this.field = field;
    }

    public Comparator<Glob> getOrder() {
      return new GlobFieldComparator(field);
    }
  }
}