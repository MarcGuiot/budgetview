package com.budgetview.desktop.components.charts;

import com.budgetview.shared.model.BudgetArea;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.gui.utils.AbstractGlobComponentHolder;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.BooleanField;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.model.*;
import org.globsframework.model.format.GlobStringifier;
import org.globsframework.model.format.GlobStringifiers;
import org.globsframework.model.utils.DefaultChangeSetVisitor;
import org.globsframework.utils.directory.Directory;

import java.util.Set;

public class GlobGaugeView extends AbstractGlobComponentHolder<GlobGaugeView>
  implements ChangeSetListener {

  private Key targetKey;
  private Gauge gauge;
  private DoubleField actualField;
  private DoubleField targetField;
  private BooleanField activeField;
  private BudgetArea budgetArea;
  private DoubleField pastOverrunField;
  private DoubleField futureOverrunField;
  private DoubleField pastRemainingField;
  private DoubleField futureRemainingField;

  private TextUpdater descriptionUpdater;

  public GlobGaugeView(Key targetKey, BudgetArea budgetArea,
                       DoubleField actualField, DoubleField targetField,
                       DoubleField pastRemainingField, DoubleField futureRemainingField,
                       DoubleField pastOverrunField, DoubleField futureOverrunField,
                       BooleanField activeField,
                       GlobRepository repository, Directory directory) {
    this(targetKey,
         BudgetAreaGaugeFactory.createGauge(budgetArea),
         budgetArea,
         actualField, targetField,
         pastRemainingField, futureRemainingField, pastOverrunField, futureOverrunField,
         activeField, repository, directory);
  }

  public GlobGaugeView(Key targetKey, final Gauge gauge, BudgetArea budgetArea,
                       DoubleField actualField, DoubleField targetField,
                       DoubleField pastRemainingField, DoubleField futureRemainingField,
                       DoubleField pastOverrunField, DoubleField futureOverrunField,
                       BooleanField activeField,
                       GlobRepository repository, Directory directory) {
    super(targetKey.getGlobType(), repository, directory);
    this.targetKey = targetKey;
    this.gauge = gauge;
    this.budgetArea = budgetArea;
    this.actualField = actualField;
    this.targetField = targetField;
    this.pastOverrunField = pastOverrunField;
    this.futureOverrunField = futureOverrunField;
    this.pastRemainingField = pastRemainingField;
    this.futureRemainingField = futureRemainingField;
    this.activeField = activeField;
    repository.addChangeListener(this);
    update();
  }

  public GlobGaugeView setDescriptionSource(Key key, StringField field) {
    if (descriptionUpdater != null) {
      descriptionUpdater.dispose();
    }
    descriptionUpdater = new TextUpdater(key, field) {
      protected void setValue(Gauge gauge, String text) {
        gauge.getModel().setDescription(text);
      }
    };
    return this;
  }

  public Gauge getComponent() {
    return gauge;
  }

  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    changeSet.safeVisit(type, new DefaultChangeSetVisitor() {

      public void visitCreation(Key key, FieldValues values) throws Exception {
        if (targetKey.equals(key)) {
          update();
        }
      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
        if (targetKey.equals(key)) {
          update();
        }
      }

      public void visitDeletion(Key key, FieldValues values) throws Exception {
        if (targetKey.equals(key)) {
          update();
        }
      }
    });
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    if (changedTypes.contains(type)) {
      update();
    }
  }

  public void dispose() {
    repository.removeChangeListener(this);
    if (descriptionUpdater != null) {
      descriptionUpdater.dispose();
    }
  }

  private void update() {
    Glob glob = repository.find(targetKey);
    if (glob == null) {
      GaugeUpdater.updateGauge(0, 0, 0, 0,
                               0, 0, false, gauge, budgetArea, false);
      return;
    }
    double actual = getValue(glob, actualField);
    boolean isUnset = glob.get(actualField) == null;
    double target = getValue(glob, targetField);
    double pastOverrun = getValue(glob, pastOverrunField);
    double futureOverrun = getValue(glob, futureOverrunField);
    double pastRemaining = getValue(glob, pastRemainingField);
    double futureRemaining = getValue(glob, futureRemainingField);
    boolean active = glob.isTrue(activeField);
    GaugeUpdater.updateGauge(futureRemaining, futureOverrun, pastRemaining, pastOverrun,
                             target, actual, active, gauge, budgetArea, isUnset);
  }

  protected double getValue(Glob glob, DoubleField field) {
    Double globActual = glob.get(field);
    if (globActual == null) {
      return 0;
    }
    return globActual;
  }

  private abstract class TextUpdater implements ChangeSetListener, Disposable {
    private Key key;
    private GlobStringifier stringifier;

    private TextUpdater(Key key, StringField field) {
      this(key, GlobStringifiers.get(field));
    }

    private TextUpdater(Key key, GlobStringifier stringifier) {
      this.key = key;
      this.stringifier = stringifier;
      repository.addChangeListener(this);
      update();
    }

    public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
      if (changeSet.containsChanges(key)) {
        update();
      }
    }

    public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
      if (changedTypes.contains(key.getGlobType())) {
        update();
      }
    }

    protected void update() {
      Glob glob = repository.find(key);
      setValue(gauge, glob == null ? null : stringifier.toString(glob, repository));
    }

    protected abstract void setValue(Gauge gauge, String text);

    public void dispose() {
      repository.removeChangeListener(this);
    }
  }
}
