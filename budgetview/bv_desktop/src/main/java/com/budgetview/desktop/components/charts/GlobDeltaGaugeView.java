package com.budgetview.desktop.components.charts;

import com.budgetview.desktop.description.Formatting;
import com.budgetview.shared.model.BudgetArea;
import com.budgetview.model.Month;
import com.budgetview.shared.utils.Amounts;
import com.budgetview.utils.Lang;
import org.globsframework.gui.utils.AbstractGlobComponentHolder;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.model.*;
import org.globsframework.utils.directory.Directory;

import java.util.Set;

public class GlobDeltaGaugeView extends AbstractGlobComponentHolder<GlobDeltaGaugeView>
  implements ChangeSetListener {

  private DeltaGauge gauge;
  private Key key;
  private BudgetArea budgetArea;
  private DoubleField previousValueField;
  private DoubleField newValueField;
  private IntegerField previousMonthField;
  private IntegerField newMonthField;

  public GlobDeltaGaugeView(Key key, BudgetArea budgetArea,
                            DoubleField previousValueField, DoubleField newValueField,
                            IntegerField previousMonthField, IntegerField newMonthField,
                            GlobRepository repository, Directory directory) {
    super(key.getGlobType(), repository, directory);
    this.key = key;
    this.budgetArea = budgetArea;
    this.previousMonthField = previousMonthField;
    this.newMonthField = newMonthField;
    this.gauge = new DeltaGauge();
    this.previousValueField = previousValueField;
    this.newValueField = newValueField;
    repository.addChangeListener(this);
    update();
  }

  public DeltaGauge getComponent() {
    return gauge;
  }

  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    if (changeSet.containsChanges(key)) {
      update();
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    if (changedTypes.contains(type)) {
      update();
    }
  }

  public void dispose() {
    repository.removeChangeListener(this);
  }

  private void update() {
    Glob glob = repository.find(key);
    if (glob == null) {
      gauge.setValues(null, null);
      return;
    }

    Double previousValue = glob.get(previousValueField);
    Double newValue = glob.get(newValueField);
    gauge.setValues(previousValue, newValue);
    gauge.setToolTipText("<html>" +
                         getTooltipText(previousValue, newValue, glob.get(previousMonthField), glob.get(newMonthField)) +
                         "</html>");
  }

  private String getTooltipText(Double previousValue, Double newValue,
                                Integer previousMonth, Integer newMonth) {

    if ((previousMonth == null) || (newMonth == null)) {
      return "";
    }

    String previousMonthLabel = Month.getFullMonthLabelWith4DigitYear(previousMonth, true).toLowerCase();
    String newMonthLabel = Month.getFullMonthLabelWith4DigitYear(newMonth, true).toLowerCase();

    if (Amounts.isNullOrZero(newValue) && Amounts.isNullOrZero(previousValue)) {
      return Lang.get("deltaGauge.alwayszero", previousMonthLabel, newMonthLabel);
    }

    if (Amounts.isNullOrZero(previousValue)) {
      return Lang.get("deltaGauge.noprevious", previousMonthLabel);
    }

    String previousValueLabel = Formatting.toString(previousValue, budgetArea);
    if (Amounts.isNullOrZero(newValue)) {
      return Lang.get("deltaGauge.tozero", previousValueLabel, previousMonthLabel, newMonthLabel);
    }

    if (Amounts.equal(newValue, previousValue)) {
      return Lang.get("deltaGauge.same", previousMonthLabel);
    }

    if ((previousValue < 0) && (newValue > 0)) {
      return Lang.get("deltaGauge.signchange.negative", previousValueLabel, previousMonthLabel);
    }
    if ((previousValue > 0) && (newValue < 0)) {
      return Lang.get("deltaGauge.signchange.positive", previousValueLabel, previousMonthLabel);
    }

    if (Math.abs(previousValue) > Math.abs(newValue)) {
      return Lang.get("deltaGauge.samesign.decreasing", previousValueLabel, previousMonthLabel);
    }
    else {
      return Lang.get("deltaGauge.samesign.increasing", previousValueLabel, previousMonthLabel);
    }
  }
}
