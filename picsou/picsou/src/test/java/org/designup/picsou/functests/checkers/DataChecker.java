package org.designup.picsou.functests.checkers;

import org.designup.picsou.model.MasterCategory;
import org.designup.picsou.utils.Lang;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public abstract class DataChecker {
  private DecimalFormat format = new DecimalFormat("0.00", new DecimalFormatSymbols(Locale.US));

  protected String toString(double value) {
    return format.format(value);
  }

  public static String getCategoryName(MasterCategory category) {
    return Lang.get("category." + category.getName());
  }
}
