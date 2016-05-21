package com.budgetview.gui.components.charts;

import com.budgetview.shared.gui.gauge.GaugeTextSource;
import com.budgetview.utils.Lang;

public class LangGaugeTextSource implements GaugeTextSource {
  public String getText(String key, String... args) {
    return Lang.get(key, args);
  }
}
