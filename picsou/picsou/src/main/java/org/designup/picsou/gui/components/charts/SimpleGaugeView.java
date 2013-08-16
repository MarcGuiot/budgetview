package org.designup.picsou.gui.components.charts;

import org.globsframework.gui.utils.AbstractGlobComponentHolder;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.model.*;
import org.globsframework.utils.directory.Directory;

import java.util.Set;

public class SimpleGaugeView extends AbstractGlobComponentHolder<GlobGaugeView> implements ChangeSetListener {

  private Key key;
  private DoubleField actualValueField;
  private DoubleField targetValueField;
  private Gauge gauge = new Gauge();

  public static SimpleGaugeView init(DoubleField actualValueField, DoubleField targetValueField,
                                     GlobRepository repository, Directory directory) {
    return new SimpleGaugeView(actualValueField, targetValueField, repository, directory);
  }

  private SimpleGaugeView(DoubleField actualValueField, DoubleField targetValueField,
                          GlobRepository repository, Directory directory) {
    super(actualValueField.getGlobType(), repository, directory);
    this.actualValueField = actualValueField;
    this.targetValueField = targetValueField;
    repository.addChangeListener(this);
  }

  public void setKey(Key key) {
    this.key = key;
    update();
  }

  public Gauge getComponent() {
    return gauge;
  }

  public void dispose() {
    repository.removeChangeListener(this);
    gauge = null;
  }

  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    update();
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    update();
  }

  private void update() {
    if (gauge == null) {
      return;
    }
    Glob glob = repository.find(key);
    if (glob == null) {
      gauge.getModel().setValues((double)0, (double)0);
    }
    else {
      gauge.getModel().setValues(glob.get(actualValueField, 0.00), glob.get(targetValueField, 0.00));
    }
  }
}
