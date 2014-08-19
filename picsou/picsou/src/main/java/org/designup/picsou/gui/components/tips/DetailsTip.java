package org.designup.picsou.gui.components.tips;

import net.java.balloontip.BalloonTip;
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
  private Directory directory;
  private AWTEventListener mouseListener;
  private boolean clickThrough;
  private TipPosition position = TipPosition.TOP_RIGHT;
  private ColorChangeListener colorListener;

  public DetailsTip(final JComponent component, String text, Directory directory) {
    this.component = component;
    this.text = text;
    this.directory = directory;

    colorListener = new ColorChangeListener() {
      public void colorsChanged(ColorLocator colorLocator) {
        fillColor = colorLocator.get("detailsTip.bg");
        borderColor = colorLocator.get("detailsTip.border");
        if (balloonTip != null) {
          balloonTip.setStyle(createStyle());
        }
      }
    };
    directory.get(ColorService.class).addListener(colorListener);
  }

  public void setPosition(TipPosition position) {
    this.position = position;
  }

  public void setClickThrough() {
    this.clickThrough = true;
  }

  public void show() {
    balloonTip = new BalloonTip(component,
                                new JLabel(text),
                                createStyle(),
                                BalloonTip.Orientation.LEFT_ABOVE,
                                BalloonTip.AttachLocation.NORTHEAST,
                                0, 20,
                                false){
    };
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
        else {
          dispose();
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
          if ((mouseEvent.getComponent() == component) && (!clickThrough)) {
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

    directory.get(ColorService.class).removeListener(colorListener);
    colorListener = null;

    component = null;
    balloonTip.closeBalloon();
    balloonTip = null;
  }
}