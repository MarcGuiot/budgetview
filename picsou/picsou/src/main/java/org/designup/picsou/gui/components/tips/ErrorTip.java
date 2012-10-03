package org.designup.picsou.gui.components.tips;

import net.java.balloontip.BalloonTip;
import net.java.balloontip.positioners.Left_Above_Positioner;
import net.java.balloontip.styles.RoundedBalloonStyle;
import org.designup.picsou.gui.utils.Gui;
import org.globsframework.gui.splits.color.ColorChangeListener;
import org.globsframework.gui.splits.color.ColorLocator;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.gui.splits.utils.AutoDispose;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;

public class ErrorTip implements Disposable, ColorChangeListener {

  private BalloonTip balloonTip;
  private Color fillColor;
  private Color borderColor;
  private HierarchyListener visibilityUpdater;
  private JComponent component;
  private Directory directory;

  public static ErrorTip showLeft(JComponent component, String text, Directory directory) {
    return show(component, text, directory, TipPosition.TOP_LEFT);
  }

  public static ErrorTip showLeft(JTextField component, String text, Directory directory) {
    return show(component, text, directory, TipPosition.TOP_LEFT);
  }

  public static ErrorTip showRight(JComponent component, String text, Directory directory) {
    return show(component, text, directory, TipPosition.TOP_RIGHT);
  }

  public static ErrorTip show(JComponent component, String text, Directory directory, TipPosition position) {
    return new ErrorTip(component, text, directory, position);
  }

  public static ErrorTip show(JTextField component, String text, Directory directory, TipPosition position) {
    ErrorTip errorTip = new ErrorTip(component, text, directory, position);
    AutoDispose.registerTextEdition(component, errorTip);
    return errorTip;
  }

  private ErrorTip(final JComponent component, final String text, Directory directory, TipPosition position) {
    this.component = component;
    this.directory = directory;

    directory.get(ColorService.class).addListener(this);

    balloonTip = new PatchedBallonTip(component,
                                text,
                                new RoundedBalloonStyle(5, 5, fillColor, borderColor),
                                BalloonTip.Orientation.LEFT_ABOVE,
                                BalloonTip.AttachLocation.NORTHEAST,
                                0, 20,
                                false);
    balloonTip.setPositioner(position.getPositioner());
    balloonTip.setVisible(true);

    visibilityUpdater = new HierarchyListener() {
      public void hierarchyChanged(HierarchyEvent e) {
        if (balloonTip == null) {
          return;
        }
        boolean visible = Gui.isVisibleInWindow(component);
        balloonTip.setVisible(visible);
        if (visible) {
          balloonTip.refreshLocation();
        }
      }
    };
    component.addHierarchyListener(visibilityUpdater);
  }

  public void colorsChanged(ColorLocator colorLocator) {
    fillColor = colorLocator.get("errorTip.bg");
    borderColor = colorLocator.get("errorTip.border");
  }

  public void dispose() {
    if (component == null) {
      return;
    }
    component.removeHierarchyListener(visibilityUpdater);
    component = null;
    balloonTip.closeBalloon();
    balloonTip = null;
    directory.get(ColorService.class).removeListener(this);
  }
}
