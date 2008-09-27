package org.designup.picsou.functests.checkers;

import junit.framework.Assert;
import org.designup.picsou.model.MasterCategory;
import org.designup.picsou.utils.Lang;
import org.uispec4j.Panel;

import javax.swing.*;
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

  protected <T extends JComponent> void checkComponentNotVisible(final Panel panel,
                                                                 final Class<T> swingComponentClass,
                                                                 final String componentName) {
    JComponent component = panel.findSwingComponent(swingComponentClass, componentName);
    Assert.assertTrue(component == null || !component.isVisible());
  }
}
