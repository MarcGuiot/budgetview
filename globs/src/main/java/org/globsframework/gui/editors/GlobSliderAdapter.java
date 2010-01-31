package org.globsframework.gui.editors;

import org.globsframework.model.GlobList;

import javax.swing.*;

public interface GlobSliderAdapter {

  void init(JSlider slider);

  void setSliderValue(Double value, JSlider slider, GlobList selection);

  Double convertToGlobsValue(int sliderValue);
}
