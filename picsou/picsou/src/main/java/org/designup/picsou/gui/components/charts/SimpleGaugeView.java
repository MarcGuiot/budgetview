package org.designup.picsou.gui.components.charts;

import com.budgetview.shared.utils.Amounts;
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
  private boolean autoHideIfEmpty;

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

  public SimpleGaugeView setAutoHideIfEmpty(boolean autoHide) {
    autoHideIfEmpty = autoHide;
    update();
    return this;
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
    double actual = glob != null ? glob.get(actualValueField, 0.00) : 0.00;
    double target = glob != null ? glob.get(targetValueField, 0.00) : 0.00;
    gauge.getModel().setValues(actual, target);
    if (autoHideIfEmpty) {
      gauge.setVisible(Amounts.isNotZero(actual) && Amounts.isNotZero(target));
    }
  }
}
