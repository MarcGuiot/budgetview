package org.designup.picsou.gui.series.edition;

import org.designup.picsou.gui.components.AmountEditor;
import org.designup.picsou.gui.utils.Gui;
import org.designup.picsou.model.SeriesBudget;
import org.designup.picsou.model.util.Amounts;
import org.globsframework.gui.editors.GlobSliderAdapter;
import org.globsframework.gui.splits.color.ColorChangeListener;
import org.globsframework.gui.splits.color.ColorLocator;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Set;

import static org.globsframework.model.utils.GlobMatchers.isTrue;

public class SeriesBudgetSliderAdapter implements GlobSliderAdapter, ColorChangeListener {

  private java.util.List<Scale> scales = new ArrayList<Scale>();

  private Font labelFont;
  private Color labelColor;
  private AmountEditor amountEditor;
  private GlobRepository repository;

  public SeriesBudgetSliderAdapter(AmountEditor amountEditor, GlobRepository repository) {
    this.amountEditor = amountEditor;
    this.repository = repository;

    labelFont = Gui.getDefaultFont().deriveFont(Font.PLAIN, 9);
    scales.add(new Scale(200, 25, 5, 25));
    scales.add(new Scale(500, 50, 10, 100));
    scales.add(new Scale(1000, 100, 25, 250));
    scales.add(new Scale(2000, 250, 50, 500));
    scales.add(new Scale(5000, 500, 100, 1000));
    scales.add(new Scale(7500, 750, 100, 1000));
    scales.add(new Scale(10000, 1000, 250, 1000));
    scales.add(new Scale(20000, 2500, 500, 5000));
  }

  public void colorsChanged(ColorLocator colorLocator) {
    labelColor = colorLocator.get("dialog.block.slider.label");
  }

  public void init(final JSlider slider) {
    slider.setExtent(0);
    slider.setMinimum(0);
    slider.setMaximum(1);
    slider.setPaintTicks(true);
    slider.setPaintLabels(false);
    slider.setSnapToTicks(true);
  }

  public void setSliderValue(Double value, final JSlider slider, GlobList selection) {
    if (value == null) {
      updateScale(0, slider, scales.get(2));
      return;
    }

    final int absValue = (int)Math.abs(value);
    final Scale scale = getScale(absValue);
    updateScale(absValue, slider, scale);
  }

  public Double convertToGlobsValue(int sliderValue) {
    return amountEditor.adjustSign((double)sliderValue);
  }

  private void updateScale(int value, JSlider slider, Scale scale) {

    if (scale.max != slider.getMaximum()) {

      slider.setPaintLabels(true);
      slider.setExtent(0);
      slider.setMinimum(0);
      slider.setMaximum(scale.max);
      slider.setMajorTickSpacing(scale.major);
      slider.setMinorTickSpacing(scale.minor);

      Hashtable<Integer, JLabel> table = new Hashtable<Integer, JLabel>();
      for (int labelTick = 0; labelTick <= scale.max; labelTick += scale.label) {
        // Subclassed to prevent the slider from changing the values
        table.put(labelTick, new JLabel(Integer.toString(labelTick)) {
          public Font getFont() {
            return labelFont;
          }

          public Color getForeground() {
            return labelColor;
          }
        });
      }
      slider.setLabelTable(table);
      slider.setPaintLabels(true);
    }

    slider.setValue(value);
  }

  private Scale getScale(int absValue) {
    double maxValue = absValue;
    Set<Double> allValues =
      repository.getAll(SeriesBudget.TYPE, isTrue(SeriesBudget.ACTIVE))
        .getValueSet(SeriesBudget.AMOUNT);
    for (Double value : allValues) {
      if (value != null) {
        maxValue = Math.max(maxValue, Math.abs(value));
      }
    }
    maxValue *= 1.5;

    for (Scale scale : scales) {
      if (scale.max > maxValue) {
        return scale;
      }
    }

    int max = (int)Amounts.upperOrder(maxValue);
    return new Scale(max, max / 10, max / 100, max / 5);
  }

  private class Scale {
    private final int max;
    private final int major;
    private final int minor;
    private final int label;

    private Scale(int max, int major, int minor, int label) {
      this.max = max;
      this.major = major;
      this.minor = minor;
      this.label = label;
    }

    public String toString() {
      return label + " max:" + max + " major:" + major + " minor:" + minor;
    }
  }


}
