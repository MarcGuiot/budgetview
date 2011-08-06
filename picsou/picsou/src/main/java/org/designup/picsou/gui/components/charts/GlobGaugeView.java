package org.designup.picsou.gui.components.charts;

import org.designup.picsou.model.BudgetArea;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.gui.utils.AbstractGlobComponentHolder;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.BooleanField;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.model.*;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.model.format.GlobStringifier;
import org.globsframework.model.format.GlobStringifiers;
import org.globsframework.model.utils.DefaultChangeSetVisitor;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.model.utils.KeyChangeListener;
import org.globsframework.utils.directory.Directory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class GlobGaugeView extends AbstractGlobComponentHolder<GlobGaugeView>
  implements ChangeSetListener,
             GlobSelectionListener {

  private Gauge gauge;
  private DoubleField actualField;
  private DoubleField targetField;
  private BooleanField activeField;
  private GlobMatcher matcher;
  private List<Key> currentSelection = new ArrayList<Key>();
  private BudgetArea budgetArea;
  private DoubleField pastOverrunField;
  private DoubleField futureOverrunField;
  private DoubleField pastRemainingField;
  private DoubleField futureRemainingField;

  private TextUpdater textUpdater;
  private MaxValueUpdater maxValueUpdater;
  private TextUpdater descriptionUpdater;

  public GlobGaugeView(GlobType type, BudgetArea budgetArea,
                       DoubleField actualField, DoubleField targetField,
                       DoubleField pastRemainingField, DoubleField futureRemainingField,
                       DoubleField pastOverrunField, DoubleField futureOverrunField,
                       BooleanField activeField,
                       GlobMatcher matcher, GlobRepository repository, Directory directory) {
    this(type,
         BudgetAreaGaugeFactory.createGauge(budgetArea),
         budgetArea,
         actualField, targetField,
         pastRemainingField, futureRemainingField, pastOverrunField, futureOverrunField,
         activeField, matcher, repository, directory);
  }

  public GlobGaugeView(GlobType type, final Gauge gauge, BudgetArea budgetArea,
                       DoubleField actualField, DoubleField targetField,
                       DoubleField pastRemainingField, DoubleField futureRemainingField,
                       DoubleField pastOverrunField, DoubleField futureOverrunField,
                       BooleanField activeField,
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
    this.activeField = activeField;
    this.matcher = matcher;
    repository.addChangeListener(this);
    selectionService.addListener(this, type);
    currentSelection = selectionService.getSelection(type).filterSelf(matcher, repository).toKeyList();
    update();
  }

  public GlobGaugeView setLabelSource(Key key) {
    GlobStringifier stringifier = directory.get(DescriptionService.class).getStringifier(key.getGlobType());
    if (textUpdater != null) {
      textUpdater.dispose();
    }
    this.textUpdater = new TextUpdater(key, stringifier) {
      protected void setValue(Gauge gauge, String text) {
        gauge.setLabel(text);
      }
    };
    return this;
  }

  public GlobGaugeView setDescriptionSource(Key key, StringField field) {
    if (descriptionUpdater != null) {
      descriptionUpdater.dispose();
    }
    descriptionUpdater = new TextUpdater(key, field) {
      protected void setValue(Gauge gauge, String text) {
        gauge.setDescription(text);
      }
    };
    return this;
  }

  public GlobGaugeView setMaxValueUpdater(Key maxValueKey, DoubleField maxValueField) {
    if (maxValueUpdater != null) {
      maxValueUpdater.dispose();
    }
    maxValueUpdater = new MaxValueUpdater(maxValueKey, maxValueField);
    return this;
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
    if (textUpdater != null) {
      textUpdater.dispose();
    }
    if (descriptionUpdater != null) {
      descriptionUpdater.dispose();
    }
    if (maxValueUpdater != null) {
      maxValueUpdater.dispose();
    }
  }

  private void update() {
    double actual = 0;
    double target = 0;
    double pastOverrun = 0;
    double futureOverrun = 0;
    double pastRemaining = 0;
    double futureRemaining = 0;
    boolean isUnset = false;
    boolean active = false;
    for (Iterator<Key> iter = currentSelection.iterator(); iter.hasNext(); ) {
      Key key = iter.next();
      Glob glob = repository.find(key);
      if ((glob == null) || !matcher.matches(glob, repository)) {
        iter.remove();
        continue;
      }
      actual += getValue(glob, actualField);
      if (glob.get(actualField) == null) {
        isUnset = true;
      }
      target += getValue(glob, targetField);
      pastOverrun += getValue(glob, pastOverrunField);
      futureOverrun += getValue(glob, futureOverrunField);
      pastRemaining += getValue(glob, pastRemainingField);
      futureRemaining += getValue(glob, futureRemainingField);
      active |= glob.isTrue(activeField);
    }
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

  private class MaxValueUpdater extends KeyChangeListener implements Disposable {
    private DoubleField field;

    private MaxValueUpdater(Key maxValueKey, DoubleField maxValueField) {
      super(maxValueKey);
      field = maxValueField;
      repository.addChangeListener(this);
      update();
    }

    protected void update() {
      Glob glob = repository.find(key);
      if (glob == null) {
        gauge.setMaxValue(null);
      }
      else {
        gauge.setMaxValue(glob.get(field));
      }
    }

    public void dispose() {
      repository.removeChangeListener(this);
    }
  }

}
