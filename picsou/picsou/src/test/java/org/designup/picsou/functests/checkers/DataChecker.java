package org.designup.picsou.functests.checkers;

import junit.framework.Assert;
import org.designup.picsou.model.MasterCategory;
import org.designup.picsou.utils.Lang;
import org.uispec4j.Panel;
import org.uispec4j.Window;
import org.uispec4j.Key;
import org.uispec4j.utils.KeyUtils;

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

  protected <T extends JComponent> void checkComponentVisible(Panel panel,
                                                              Class<T> swingComponentClass,
                                                              String componentName,
                                                              boolean visible) {
    JComponent component = panel.findSwingComponent(swingComponentClass, componentName);
    Assert.assertEquals(!visible, component == null || !component.isVisible());
  }

  protected void pressEsc(final Window dialog) {
    final JDialog jDialog = (JDialog)dialog.getAwtComponent();
    KeyUtils.pressKey(jDialog.getRootPane(), Key.ESCAPE);
  }
}
