package org.designup.picsou.gui.components.tips;

import net.java.balloontip.BalloonTip;
import net.java.balloontip.styles.BalloonTipStyle;
import org.designup.picsou.gui.utils.Gui;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class DetailsTip implements BalloonTipHolder, Disposable {

  private BalloonTip balloonTip;
  private HierarchyListener visibilityUpdater;
  private JComponent component;
  private String text;
  private AWTEventListener mouseListener;
  private boolean clickThrough;
  private TipPosition position = TipPosition.TOP_RIGHT;
  private TipAnchor anchor = TipAnchor.NORTHEAST;
  private BalloonTipStyle balloonStyle;
  private Disposable styleUpdater;

  public DetailsTip(final JComponent component, String text, Directory directory) {
    this.component = component;
    this.text = text;
    this.styleUpdater = initUpdater(directory);
  }

  protected Disposable initUpdater(Directory directory) {
    return DetailsTipStyleUpdater.install(this, directory);
  }

  public void setStyle(BalloonTipStyle balloonStyle) {
    this.balloonStyle = balloonStyle;
  }

  public void setPosition(TipPosition position) {
    this.position = position;
  }

  public void setAnchor(TipAnchor anchor) {
    this.anchor = anchor;
  }

  public void setClickThrough() {
    this.clickThrough = true;
  }

  public void show() {
    balloonTip = new BalloonTip(component,
                                new JLabel(text),
                                balloonStyle,
                                BalloonTip.Orientation.LEFT_ABOVE,
                                anchor.getLocation(),
                                0, 20,
                                false) {
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
          if ((mouseEvent.getComponent() == component) && !clickThrough) {
            mouseEvent.consume();
          }
          dispose();
        }
      }
    };
    Toolkit.getDefaultToolkit().addAWTEventListener(mouseListener, AWTEvent.MOUSE_EVENT_MASK);
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

    styleUpdater.dispose();
    styleUpdater = null;

    component = null;
    balloonTip.closeBalloon();
    balloonTip = null;
  }
}