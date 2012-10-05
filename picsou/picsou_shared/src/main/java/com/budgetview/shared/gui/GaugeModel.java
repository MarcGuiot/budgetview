package com.budgetview.shared.gui;

import com.budgetview.shared.utils.AmountFormat;
import com.budgetview.shared.utils.Amounts;
import org.globsframework.utils.Strings;

import java.util.ArrayList;
import java.util.List;

public class GaugeModel {

  private double actualValue;
  private double targetValue;

  private boolean invertAll;
  private double overrunPart = 0;
  private double fillPercent = 0;
  private double overrunPercent = 0;
  private double emptyPercent = 1.0;
  private double beginPercent = 0.;
  private boolean overrunError = false;
  private double remainder;

  private String detailsTooltip;
  private String description;
  private List<GaugeModelListener> listeners = new ArrayList<GaugeModelListener>();
  private GaugeTextSource textSource;

  public GaugeModel() {
    this(new GaugeTextSource() {
      public String getText(String key, String... args) {
        return "";
//        return Lang.get(key, args);
      }
    });
  }

  public GaugeModel(GaugeTextSource textSource) {
    this.textSource = textSource;
    setTooltipText("gauge.unset");
  }

  public void addListener(GaugeModelListener listener) {
    listeners.add(listener);
    listener.updateTooltip(getTooltipText());
  }

  public void removeListener(GaugeModelListener listener) {
    listeners.remove(listener);
  }

  public void setValues(double actualValue, double targetValue) {

    this.actualValue = actualValue * (invertAll ? -1.0 : 1.0);
    this.targetValue = targetValue * (invertAll ? -1.0 : 1.0);

    boolean sameSign = Amounts.sameSign(this.actualValue, this.targetValue);
    double absActual = Math.abs(this.actualValue);
    double absTarget = Math.abs(this.targetValue);
    remainder = this.targetValue - this.actualValue;

    fillPercent = 0;
    overrunPercent = 0;
    emptyPercent = 1;
    beginPercent = 0;
    overrunError = false;

    if (Amounts.isNearZero(this.targetValue) && Amounts.isNearZero(this.actualValue)) {
      setTooltipText("gauge.unset");
    }
    else if (Amounts.isNearZero(this.targetValue - this.actualValue)) { // passer par remaining et overrun
      fillPercent = 1;
      emptyPercent = 0;
      setTooltipText("gauge.complete");
    }
    else if (this.targetValue > 0) {
      if (this.actualValue > this.targetValue) {
        fillPercent = absTarget / absActual;  //==> differencie passé et futur
        overrunPercent = 1 - fillPercent;
        setTooltipText("gauge.overrun.ok", Math.abs(remainder));
        emptyPercent = 0;
      }
      else {
        if (!sameSign && !Amounts.isNearZero(this.actualValue)) {
          beginPercent = absActual / (absActual + absTarget);
        }
        else {
          fillPercent = absActual / absTarget;
        }
        emptyPercent = 1 - fillPercent - beginPercent;
        setTooltipText("gauge.expected", Math.abs(remainder));
      }
    }
    else if (this.targetValue < 0) {
      if (this.actualValue < this.targetValue) {
        fillPercent = absTarget / absActual;
        overrunPercent = 1 - fillPercent;
        overrunError = true;
        emptyPercent = 0;
        setTooltipText("gauge.overrun.error", Math.abs(remainder));
      }
      else {
        if (!sameSign && !Amounts.isNearZero(this.actualValue)) {
          fillPercent = absActual / (absActual + absTarget);
        }
        else {
          fillPercent = absActual / absTarget;
        }
        emptyPercent = 1 - fillPercent;
        setTooltipText("gauge.partial", Math.abs(remainder));
      }
    }
    else {
      if (this.actualValue != 0) {
        fillPercent = 0;
        overrunPercent = 1;
        emptyPercent = 0;
        if (this.actualValue > 0) {
          setTooltipText("gauge.overrun.ok", Math.abs(remainder));
        }
        else {
          overrunError = true;
          setTooltipText("gauge.overrun.error", Math.abs(remainder));
        }
      }
    }
    notifyUpdate();
  }

  public void setValues(double actualValue, double targetValue, double partialOverrun, double remaining,
                        String detailsTooltipText, boolean targetValueUnset) {
    fillPercent = 0;
    overrunPercent = 0;
    emptyPercent = 1;
    beginPercent = 0;
    overrunError = false;

    this.actualValue = actualValue;
    this.targetValue = targetValue;
    this.overrunPart = partialOverrun;
    this.remainder = remaining;
    boolean sameSign = Amounts.sameSign(this.actualValue, this.targetValue);

    this.detailsTooltip = detailsTooltipText;
    if (targetValueUnset) {
      setTooltipText("gauge.plannetUnset");
    }
    else if (Amounts.isNearZero(this.targetValue) && Amounts.isNearZero(this.actualValue)) {
      setTooltipText("gauge.unset");
    }
    else if (Amounts.isNearZero(this.targetValue - this.actualValue) && Amounts.isNearZero(partialOverrun)
             && Amounts.isNearZero(remaining)) { // passer par remaining et overrun
      fillPercent = 1;
      emptyPercent = 0;
      setTooltipText("gauge.complete");
    }
    else if (Math.abs(actualValue) > Math.abs(targetValue) && sameSign) {
      double total = Math.abs(remaining) + Math.abs(actualValue);
      fillPercent = (Math.abs(actualValue) - Math.abs(partialOverrun)) / total;
      overrunPercent = Math.abs(partialOverrun / total);
    }
    else if (!sameSign && !Amounts.isNearZero(this.actualValue)) {
      double total = Math.abs(remaining) + Math.abs(actualValue) + Math.abs(targetValue);
      beginPercent = (Math.abs(actualValue) - Math.abs(partialOverrun)) / total;
      overrunPercent = Math.abs(partialOverrun / total);
    }
    else {
      double total = Math.abs(targetValue);
      fillPercent = (Math.abs(actualValue) - Math.abs(partialOverrun)) / total;
      overrunPercent = Math.abs(partialOverrun / total);
    }
    emptyPercent = 1 - overrunPercent - fillPercent - beginPercent;

    if (Amounts.isNearZero(targetValue)) {
      if (actualValue > 0) {
        overrunError = invertAll;
      }
      else {
        overrunError = !invertAll;
      }
    }
    else {
      if (partialOverrun > 0) {
        overrunError = invertAll;
      }
      else {
        overrunError = !invertAll;
      }
    }

    notifyUpdate();
    updateTooltip();
  }

  public void setInvertAll(boolean invertAll) {
    this.invertAll = invertAll;
  }

  public boolean shouldInvertAll() {
    return invertAll;
  }

  public void setDescription(String text) {
    this.description = text;
    updateTooltip();
  }

  private void setTooltipText(String key, Double... values) {
    if (Strings.isNullOrEmpty(key)) {
      this.detailsTooltip = "";
      updateTooltip();
      return;
    }

    String[] formattedValues = new String[values.length];
    for (int i = 0; i < values.length; i++) {
      formattedValues[i] = AmountFormat.DECIMAL_FORMAT.format(values[i]);
    }
    this.detailsTooltip = textSource.getText(key, formattedValues);
    updateTooltip();
  }

  private void updateTooltip() {
    notifyTooltipUpdate(getTooltipText());
  }

  private String getTooltipText() {
    StringBuilder builder = new StringBuilder("<html>");
    if (Strings.isNotEmpty(description)) {
      builder.append(description).append("<br/>");
    }
    builder.append(detailsTooltip);
    builder.append("</html>");
    return builder.toString();
  }

  public double getBeginPercent() {
    return beginPercent;
  }

  public boolean hasOverrunError() {
    return overrunError;
  }

  public double getActualValue() {
    return actualValue;
  }

  public String getDescription() {
    return description;
  }

  public double getEmptyPercent() {
    return emptyPercent;
  }

  public double getFillPercent() {
    return fillPercent;
  }

  public double getOverrunPart() {
    return overrunPart;
  }

  public double getOverrunPercent() {
    return overrunPercent;
  }

  public double getRemainder() {
    return remainder;
  }

  public double getTargetValue() {
    return targetValue;
  }

  private void notifyUpdate() {
    for (GaugeModelListener listener : listeners) {
      listener.modelUpdated();
    }
  }

  private void notifyTooltipUpdate(String text) {
    for (GaugeModelListener listener : listeners) {
      listener.updateTooltip(text);
    }
  }
}
