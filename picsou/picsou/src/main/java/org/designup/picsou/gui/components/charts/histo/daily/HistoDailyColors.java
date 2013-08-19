package org.designup.picsou.gui.components.charts.histo.daily;

import org.designup.picsou.gui.components.charts.histo.line.HistoLineColors;
import org.globsframework.gui.splits.color.ColorChangeListener;
import org.globsframework.gui.splits.color.ColorLocator;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.utils.directory.Directory;

import java.awt.*;

public class HistoDailyColors implements Disposable {

  public final HistoLineColors line;

  private String currentDayKey;
  private String currentDayAnnotationKey;
  private String positiveInnerLabelKey;
  private String negativeInnerLabelKey;
  private String innerLabelLineKey;
  private String rolloverDayKey;
  private String selectedDayKey;
  private final Directory directory;

  private Color currentDayColor;
  private Color currentDayAnnotationColor;
  private Color positiveInnerLabelColor;
  private Color negativeInnerLabelColor;
  private Color rolloverDayColor;
  private Color selectedDayColor;
  private HistoDailyColors.ColorUpdater colorUpdater;
  private Color innerLabelLineColor;

  public HistoDailyColors(HistoLineColors line,
                          String currentDayKey,
                          String currentDayAnnotationKey,
                          String positiveInnerLabelKey,
                          String negativeInnerLabelKey,
                          String innerLabelLineKey,
                          String rolloverDayKey,
                          String selectedDayKey,
                          Directory directory) {
    this.line = line;
    this.currentDayKey = currentDayKey;
    this.currentDayAnnotationKey = currentDayAnnotationKey;
    this.positiveInnerLabelKey = positiveInnerLabelKey;
    this.negativeInnerLabelKey = negativeInnerLabelKey;
    this.innerLabelLineKey = innerLabelLineKey;
    this.rolloverDayKey = rolloverDayKey;
    this.selectedDayKey = selectedDayKey;
    this.directory = directory;

    this.colorUpdater = new ColorUpdater();
    directory.get(ColorService.class).addListener(colorUpdater);
  }

  public void dispose() {
    directory.get(ColorService.class).removeListener(colorUpdater);
  }

  private class ColorUpdater implements ColorChangeListener {

    public void colorsChanged(ColorLocator colorLocator) {
      currentDayColor = colorLocator.get(currentDayKey);
      currentDayAnnotationColor = colorLocator.get(currentDayAnnotationKey);
      positiveInnerLabelColor = colorLocator.get(positiveInnerLabelKey);
      negativeInnerLabelColor = colorLocator.get(negativeInnerLabelKey);
      innerLabelLineColor = colorLocator.get(innerLabelLineKey);
      rolloverDayColor = colorLocator.get(rolloverDayKey);
      selectedDayColor = colorLocator.get(selectedDayKey);
    }
  }

  public Color getRolloverDayColor() {
    return rolloverDayColor;
  }

  public Color getCurrentDayColor() {
    return currentDayColor;
  }

  public Color getInnerLabelColor(double value) {
    return value >= 0 ? positiveInnerLabelColor : negativeInnerLabelColor;
  }

  public Color getInnerLabelLineColor() {
    return innerLabelLineColor;
  }

  public Color getSelectedDayColor() {
    return selectedDayColor;
  }

  public Color getCurrentDayAnnotationColor() {
    return currentDayAnnotationColor;
  }
}
