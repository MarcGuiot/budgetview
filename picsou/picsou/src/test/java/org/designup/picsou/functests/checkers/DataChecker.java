package org.designup.picsou.functests.checkers;

import org.designup.picsou.model.MasterCategory;
import org.designup.picsou.utils.Lang;

import java.text.DecimalFormat;

public abstract class DataChecker {
  private DecimalFormat format = new DecimalFormat("#.00");

  protected String toString(double value) {
    return format.format(value);
  }

  public static String getCategoryName(MasterCategory category) {
    return Lang.get("category." + category.getName());
  }
}
