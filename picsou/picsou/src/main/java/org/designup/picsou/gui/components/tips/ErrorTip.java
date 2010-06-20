package org.designup.picsou.gui.components.tips;

import net.java.balloontip.BalloonTip;
import net.java.balloontip.positioners.Right_Above_Positioner;
import net.java.balloontip.styles.RoundedBalloonStyle;
import org.designup.picsou.gui.utils.Gui;
import org.globsframework.gui.splits.color.ColorChangeListener;
import org.globsframework.gui.splits.color.ColorLocator;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;

public class ErrorTip implements Disposable {

  private BalloonTip balloonTip;
  private Color fillColor;
  private Color borderColor;
  private HierarchyListener visibilityUpdater;
  private JComponent component;

  public static ErrorTip show(JComponent component, String text, Directory directory) {
    return new ErrorTip(component, text, directory);
  }

  private ErrorTip(final JComponent component, String text, Directory directory) {
    this.component = component;

    directory.get(ColorService.class).addListener(new ColorChangeListener() {
      public void colorsChanged(ColorLocator colorLocator) {
        fillColor = colorLocator.get("errorTip.bg");
        borderColor = colorLocator.get("errorTip.border");
      }
    });

    balloonTip = new BalloonTip(component,
                                text,
                                new RoundedBalloonStyle(5, 5, fillColor, borderColor),
                                BalloonTip.Orientation.LEFT_ABOVE,
                                BalloonTip.AttachLocation.NORTHEAST,
                                0, 20,
                                false);
    balloonTip.setPositioner(new Right_Above_Positioner(10, 20));

    visibilityUpdater = new HierarchyListener() {
      public void hierarchyChanged(HierarchyEvent e) {
        if (balloonTip == null) {
          return;
        }
        boolean visible = Gui.isVisible(component);
        balloonTip.setVisible(visible);
        if (visible) {
          balloonTip.refreshLocation();
        }
      }
    };
    component.addHierarchyListener(visibilityUpdater);
  }

  public void dispose() {
    if (component == null) {
      return;
    }
    component.removeHierarchyListener(visibilityUpdater);
    component = null;
    balloonTip.closeBalloon();
    balloonTip = null;
  }
}
