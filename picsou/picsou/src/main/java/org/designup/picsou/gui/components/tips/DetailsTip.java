package org.designup.picsou.gui.components.tips;

import net.java.balloontip.BalloonTip;
import net.java.balloontip.positioners.Right_Above_Positioner;
import net.java.balloontip.styles.RoundedBalloonStyle;
import org.designup.picsou.gui.utils.Gui;
import org.globsframework.gui.splits.color.ColorChangeListener;
import org.globsframework.gui.splits.color.ColorLocator;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class DetailsTip implements Disposable {

  private BalloonTip balloonTip;
  private Color fillColor;
  private Color borderColor;
  private HierarchyListener visibilityUpdater;
  private JComponent component;
  private String text;
  private AWTEventListener mouseListener;

  DetailsTip(final JComponent component, String text, Directory directory) {
    this.component = component;
    this.text = text;

    directory.get(ColorService.class).addListener(new ColorChangeListener() {
      public void colorsChanged(ColorLocator colorLocator) {
        fillColor = colorLocator.get("detailsTip.bg");
        borderColor = colorLocator.get("detailsTip.border");
        if (balloonTip != null) {
          balloonTip.setStyle(createStyle());
        }
      }
    });
  }

  public void show() {
    balloonTip = new BalloonTip(component,
                                text,
                                createStyle(),
                                BalloonTip.Orientation.LEFT_ABOVE,
                                BalloonTip.AttachLocation.NORTHEAST,
                                0, 20,
                                false);
    balloonTip.setPositioner(new Right_Above_Positioner(10, 20));
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
    GuiUtils.addShortcut(GuiUtils.getEnclosingFrame(component).getRootPane(),
                         "ESCAPE",
                         new AbstractAction() {
                           public void actionPerformed(ActionEvent e) {
                             dispose();
                           }
                         });

    GuiUtils.runInSwingThread(new Runnable() {
      public void run() {
        registerMouseListener();
      }
    });

  }

  private void registerMouseListener() {
    mouseListener = new AWTEventListener() {
      public void eventDispatched(AWTEvent event) {
        if (event instanceof MouseEvent) {
          MouseEvent mouseEvent = (MouseEvent)event;
          if (mouseEvent.getID() != MouseEvent.MOUSE_PRESSED) {
            return;
          }
          if (mouseEvent.getComponent() == component) {
            mouseEvent.consume();
          }
          dispose();
        }
      }
    };
    Toolkit.getDefaultToolkit().addAWTEventListener(mouseListener, AWTEvent.MOUSE_EVENT_MASK);
  }

  private RoundedBalloonStyle createStyle() {
    return new RoundedBalloonStyle(5, 5, fillColor, borderColor);
  }

  public void dispose() {
    if ((component == null) || (balloonTip == null)) {
      return;
    }
    Toolkit.getDefaultToolkit().removeAWTEventListener(mouseListener);
    mouseListener = null;
    GuiUtils.removeShortcut(GuiUtils.getEnclosingFrame(component).getRootPane(),
                            "ESCAPE", KeyStroke.getKeyStroke("ESCAPE"));
    component.removeHierarchyListener(visibilityUpdater);
    component = null;
    balloonTip.closeBalloon();
    balloonTip = null;
  }
}