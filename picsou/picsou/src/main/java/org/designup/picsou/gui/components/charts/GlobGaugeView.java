package org.designup.picsou.gui.components.charts;

import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.util.Amounts;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.utils.AbstractGlobComponentHolder;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.model.*;
import org.globsframework.model.utils.DefaultChangeSetVisitor;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.utils.directory.Directory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class GlobGaugeView extends AbstractGlobComponentHolder<GlobGaugeView> implements ChangeSetListener,
                                                                                         GlobSelectionListener {

  private Gauge gauge;
  private DoubleField actualField;
  private DoubleField targetField;
  private GlobMatcher matcher;
  private List<Key> currentSelection = new ArrayList<Key>();
  private BudgetArea budgetArea;
  private DoubleField pastOverrunField;
  private DoubleField futureOverrunField;
  private DoubleField pastRemainingField;
  private DoubleField futureRemainingField;

  public GlobGaugeView(GlobType type, BudgetArea budgetArea,
                       DoubleField actualField, DoubleField targetField,
                       DoubleField pastRemainingField, DoubleField futureRemainingField,
                       DoubleField pastOverrunField, DoubleField futureOverrunField,
                       GlobMatcher matcher, GlobRepository repository, Directory directory) {
    super(type, repository, directory);
    this.budgetArea = budgetArea;
    this.pastOverrunField = pastOverrunField;
    this.futureOverrunField = futureOverrunField;
    this.pastRemainingField = pastRemainingField;
    this.futureRemainingField = futureRemainingField;
    this.gauge = BudgetAreaGaugeFactory.createGauge(budgetArea);
    this.actualField = actualField;
    this.targetField = targetField;
    this.matcher = matcher;
    repository.addChangeListener(this);
    selectionService.addListener(this, type);
    currentSelection = selectionService.getSelection(type).filterSelf(matcher, repository).toKeyList();
    update();
  }

  public GlobGaugeView(GlobType type, Gauge gauge, BudgetArea budgetArea,
                       DoubleField actualField, DoubleField targetField,
                       DoubleField pastRemainingField, DoubleField futureRemainingField,
                       DoubleField pastOverrunField, DoubleField futureOverrunField,
                       GlobMatcher matcher, GlobRepository repository, Directory directory) {
    super(type, repository, directory);
    this.gauge = gauge;
    this.budgetArea = budgetArea;
    this.actualField = actualField;
    this.targetField = targetField;
    this.pastOverrunField = pastOverrunField;
    this.futureOverrunField = futureOverrunField;
    this.pastRemainingField = pastRemainingField;
    this.futureRemainingField = futureRemainingField;
    this.matcher = matcher;
    repository.addChangeListener(this);
    selectionService.addListener(this, type);
    currentSelection = selectionService.getSelection(type).filterSelf(matcher, repository).toKeyList();
    update();
  }

  public Gauge getComponent() {
    return gauge;
  }

  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    changeSet.safeVisit(type, new DefaultChangeSetVisitor() {
      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
        if (currentSelection.contains(key)) {
          update();
        }
      }

      public void visitDeletion(Key key, FieldValues values) throws Exception {
        update();
      }
    });
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    if (changedTypes.contains(type)) {
      update();
    }
  }

  public void selectionUpdated(GlobSelection selection) {
    this.currentSelection = selection.getAll(type).filterSelf(matcher, repository).toKeyList();
    update();
  }

  public void dispose() {
    repository.removeChangeListener(this);
    selectionService.removeListener(this);
  }

  private void update() {
    double actual = 0;
    double target = 0;
    double pastOverrun = 0;
    double futureOverrun = 0;
    double pastRemaining = 0;
    double futureRemaining = 0;
    for (Iterator<Key> iter = currentSelection.iterator(); iter.hasNext();) {
      Key key = iter.next();
      Glob glob = repository.find(key);
      if ((glob == null) || !matcher.matches(glob, repository)) {
        iter.remove();
        continue;
      }
      actual += getValue(glob, actualField);
      target += getValue(glob, targetField);
      pastOverrun += getValue(glob, pastOverrunField);
      futureOverrun += getValue(glob, futureOverrunField);
      pastRemaining += getValue(glob, pastRemainingField);
      futureRemaining += getValue(glob, futureRemainingField);
    }
    Amounts.updateGauge(futureRemaining > 0. ? futureRemaining : 0.,
                        futureOverrun > 0 ? futureOverrun : 0.,
                        futureRemaining < 0. ? futureRemaining : 0., 
                        futureOverrun < 0 ? futureOverrun : 0.,
                        pastRemaining, pastOverrun, target, actual, gauge, budgetArea);
  }

  protected double getValue(Glob glob, DoubleField field) {
    Double globActual = glob.get(field);
    if (globActual == null) {
      return 0;
    }
    return globActual;
  }
}
