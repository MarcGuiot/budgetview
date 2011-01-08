package org.designup.picsou.gui.components.tips;

import net.java.balloontip.BalloonTip;
import net.java.balloontip.positioners.BalloonTipPositioner;
import net.java.balloontip.positioners.Left_Above_Positioner;
import net.java.balloontip.positioners.Right_Above_Positioner;
import net.java.balloontip.styles.RoundedBalloonStyle;
import org.designup.picsou.gui.utils.Gui;
import org.globsframework.gui.splits.color.ColorChangeListener;
import org.globsframework.gui.splits.color.ColorLocator;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.gui.splits.utils.AutoDispose;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.gui.splits.utils.GuiUtils;
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
  private String text;
  private Directory directory;

  public static ErrorTip showLeft(JComponent component, String text, Directory directory) {
    return show(component, text, directory, new Left_Above_Positioner(10, 20));
  }

  public static ErrorTip showLeft(JTextField component, String text, Directory directory) {
    ErrorTip tip = showLeft((JComponent)component, text, directory);
    AutoDispose.registerTextEdition(component, tip);
    return tip;
  }

  public static ErrorTip showRight(JTextField component, String text, Directory directory) {
    ErrorTip tip = showRight((JComponent)component, text, directory);
    AutoDispose.registerTextEdition(component, tip);
    return tip;
  }

  public static ErrorTip showRight(JComponent component, String text, Directory directory) {
    return show(component, text, directory, new Right_Above_Positioner(10, 20));
  }

  private static ErrorTip show(JComponent component, String text, Directory directory, BalloonTipPositioner positioner) {
    return new ErrorTip(component, text, directory, positioner);
  }

  private ErrorTip(final JComponent component, final String text, Directory directory, BalloonTipPositioner positioner) {
    this.component = component;
    this.text = text;
    this.directory = directory;

    directory.get(ColorService.class).addListener(this);

    balloonTip = new BalloonTip(component,
                                text,
                                new RoundedBalloonStyle(5, 5, fillColor, borderColor),
                                BalloonTip.Orientation.LEFT_ABOVE,
                                BalloonTip.AttachLocation.NORTHEAST,
                                0, 20,
                                false);
    balloonTip.setPositioner(positioner);
    balloonTip.setVisible(true);

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
