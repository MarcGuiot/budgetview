package org.designup.picsou.gui.signpost;

import net.java.balloontip.BalloonTip;
import net.java.balloontip.styles.ModernBalloonStyle;
import org.designup.picsou.gui.utils.Gui;
import org.designup.picsou.model.SignpostStatus;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.color.ColorChangeListener;
import org.globsframework.gui.splits.color.ColorLocator;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.metamodel.fields.BooleanField;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.KeyChangeListener;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.InvalidState;

import javax.swing.*;
import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;

public abstract class Signpost implements Disposable {

  protected JComponent component;
  private BooleanField completionField;
  protected GlobRepository repository;
  protected Directory directory;
  protected SelectionService selectionService;

  private BalloonTip balloonTip;
  private BalloonTip.Orientation orientation;
  private BalloonTip.AttachLocation attachLocation;

  protected ModernBalloonStyle balloonStyle;

  private KeyChangeListener completionListener;
  private HierarchyListener hierarchyListener;

  public Color fillTopColor;
  public Color fillBottomColor;
  public Color borderColor;

  protected Signpost(BooleanField completionField, GlobRepository repository, Directory directory) {
    this.completionField = completionField;
    this.repository = repository;
    this.directory = directory;
    this.selectionService = directory.get(SelectionService.class);
    this.orientation = BalloonTip.Orientation.RIGHT_BELOW;
    this.attachLocation = BalloonTip.AttachLocation.SOUTH;

    directory.get(ColorService.class).addListener(new ColorChangeListener() {
      public void colorsChanged(ColorLocator colorLocator) {
        fillTopColor = colorLocator.get("signpost.bg.top");
        fillBottomColor = colorLocator.get("signpost.bg.bottom");
        borderColor = colorLocator.get("signpost.border");
        balloonStyle = createBalloonStyle();
        if (balloonTip != null) {
          balloonTip.setStyle(balloonStyle);
        }
      }
    });
  }

  private ModernBalloonStyle createBalloonStyle() {
    ModernBalloonStyle style =
      new ModernBalloonStyle(15, 7, fillTopColor, fillBottomColor, borderColor);
    style.setBorderThickness(2);
    return style;
  }

  protected void setLocation(BalloonTip.Orientation orientation, BalloonTip.AttachLocation attachLocation) {
    this.orientation = orientation;
    this.attachLocation = attachLocation;
  }

  public void attach(JComponent component) {
    if (this.component != null) {
      throw new InvalidState("A component is already set for " + getClass().getSimpleName());
    }
    this.component = component;
    setup();
  }

  private void setup() {
    if (isCompleted()) {
      return;
    }
    completionListener = new KeyChangeListener(SignpostStatus.KEY) {
      protected void update() {
        if (isShowing() && isCompleted()) {
          dispose();
        }
      }
    };

    repository.addChangeListener(completionListener);

    hierarchyListener = new HierarchyListener() {
      public void hierarchyChanged(HierarchyEvent e) {
        if (balloonTip == null) {
          return;
        }
        boolean componentVisible = Gui.isVisibleInWindow(component);
        boolean tipVisible = balloonTip.isVisible();
        if (componentVisible != tipVisible) {
          balloonTip.setVisible(componentVisible);
        }
      }
    };
    component.addHierarchyListener(hierarchyListener);
    init();
  }

  protected abstract void init();

  public boolean isCompleted() {
    return SignpostStatus.isCompleted(completionField, repository);
  }

  protected boolean canShow() {
    return !isCompleted();
  }

  protected boolean isShowing() {
    return balloonTip != null;
  }

  protected void show(String text) {
    if (isCompleted()) {
      return;
    }
    if (isShowing()) {
      balloonTip.setText(text);
    }
    else {
      balloonTip = createBalloonTip(component, text);
      boolean visible = Gui.isVisibleInWindow(component);
      balloonTip.setVisible(visible);
    }
  }

  protected BalloonTip createBalloonTip(JComponent component, String text) {
    return new BalloonTip(component,
                          text,
                          getBalloonStyle(),
                          orientation,
                          attachLocation,
                          40, 20, false);
  }

  public void hide() {
    if (balloonTip != null) {
      balloonTip.closeBalloon();
      balloonTip = null;
    }
    if (hierarchyListener != null) {
      component.removeHierarchyListener(hierarchyListener);
      hierarchyListener = null;
    }
    if (completionListener != null) {
      repository.removeChangeListener(completionListener);
      completionListener = null;
    }
    onHide();
  }

  protected void onHide() {
  }

  public void dispose() {
    hide();
    SignpostStatus.setCompleted(completionField, repository);
  }

  public ModernBalloonStyle getBalloonStyle() {
    return balloonStyle;
  }
}
