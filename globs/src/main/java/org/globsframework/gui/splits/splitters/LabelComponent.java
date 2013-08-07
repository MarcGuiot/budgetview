package org.globsframework.gui.splits.splitters;

import org.globsframework.gui.splits.SplitProperties;
import org.globsframework.gui.splits.SplitsContext;
import org.globsframework.gui.splits.Splitter;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.gui.splits.color.ColorUpdater;
import org.globsframework.gui.splits.color.Colors;
import org.globsframework.gui.splits.components.ShadowedLabelUI;
import org.globsframework.utils.Strings;
import org.globsframework.utils.exceptions.ItemNotFound;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

public class LabelComponent extends DefaultComponent<JLabel> {
  public LabelComponent(SplitProperties properties, Splitter[] subSplitters) {
    super(JLabel.class, "label", properties, subSplitters, false);
  }

  protected void postCreateComponent(JLabel label, SplitsContext context) {
    label.setOpaque(false);

    processShadow(label, context);
    processLabelFor(label, context);
  }

  private void processShadow(JLabel label, SplitsContext context) {
    String shadowColor = properties.get("shadowColor");
    String shadowDirection = properties.get("shadowDirection");
    if ((shadowColor == null) && (shadowDirection == null)) {
      return;
    }

    final ShadowedLabelUI ui = new ShadowedLabelUI();
    label.setUI(ui);

    if (shadowColor != null) {
      if (Colors.isHexaString(shadowColor)) {
        ui.setShadowColor(Colors.toColor(shadowColor));
      }
      else if (shadowColor.length() == 0) {
        ui.setShadowColor(null);
      }
      else {
        ColorUpdater updater = new ColorUpdater(shadowColor) {
          public void updateColor(Color color) {
            ui.setShadowColor(color);
          }
        };
        updater.install(context.getService(ColorService.class));
        context.addDisposable(updater);
      }
    }

    if (shadowDirection != null) {
      ShadowedLabelUI.Direction direction = ShadowedLabelUI.Direction.parse(shadowDirection);
      if (direction == null) {
        throw new ItemNotFound("Error for label attribute shadowDirection: unknown value '" + shadowDirection + "', " +
                               "should be one of: " +
                               Arrays.toString(ShadowedLabelUI.Direction.values()).toLowerCase() + context.dump());
      }
      ui.setShadowTypedDirection(direction);
    }
  }

  private void processLabelFor(JLabel label, SplitsContext context) {
    String labelFor = properties.get("labelFor");
    if (Strings.isNullOrEmpty(labelFor)) {
      return;
    }

    context.addLabelFor(label, labelFor);
  }

  protected String[] getExcludedParameters() {
    return new String[]{"shadowColor", "shadowDirection", "labelFor"};
  }
}