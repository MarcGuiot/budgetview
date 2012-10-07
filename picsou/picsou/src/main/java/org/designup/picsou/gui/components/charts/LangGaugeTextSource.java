package org.designup.picsou.gui.components.charts;

import com.budgetview.shared.gui.GaugeTextSource;
import org.designup.picsou.utils.Lang;

public class LangGaugeTextSource implements GaugeTextSource {
  public String getText(String key, String... args) {
    return Lang.get(key, args);
  }
}
