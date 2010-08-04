package org.designup.picsou.gui.signpost;

import net.java.balloontip.BalloonTip;
import net.java.balloontip.styles.ModernBalloonStyle;
import org.designup.picsou.gui.utils.Gui;
import org.designup.picsou.model.SignpostStatus;
import org.globsframework.gui.SelectionService;
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
  protected SelectionService selectionService;

  protected static final ModernBalloonStyle BALLOON_STYLE = createBalloonStyle();
  private BalloonTip.Orientation orientation;
  private BalloonTip.AttachLocation attachLocation;

  private static ModernBalloonStyle createBalloonStyle() {
    ModernBalloonStyle style =
      new ModernBalloonStyle(15, 7, Color.YELLOW.brighter(), Color.YELLOW, Color.ORANGE);
    style.setBorderThickness(2);
    return style;
  }

  private BalloonTip balloonTip;

  protected Signpost(BooleanField completionField, GlobRepository repository, Directory directory) {
    this.completionField = completionField;
    this.repository = repository;
    this.selectionService = directory.get(SelectionService.class);
    this.orientation = BalloonTip.Orientation.RIGHT_BELOW;
    this.attachLocation = BalloonTip.AttachLocation.SOUTH;
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
    KeyChangeListener completionListener = new KeyChangeListener(SignpostStatus.KEY) {
      protected void update() {
        if (isShowing() && isCompleted()) {
          dispose();
        }
      }
    };

    repository.addChangeListener(completionListener);

    component.addHierarchyListener(new HierarchyListener() {
      public void hierarchyChanged(HierarchyEvent e) {
        if (balloonTip == null) {
          return;
        }
        balloonTip.setVisible(Gui.isVisible(component));
      }
    });
    init();
  }

  protected abstract void init();

  protected boolean isCompleted() {
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
      balloonTip.setVisible(Gui.isVisible(component));
    }
  }

  protected BalloonTip createBalloonTip(JComponent component, String text) {
    return new BalloonTip(component,
                          text,
                          BALLOON_STYLE,
                          orientation,
                          attachLocation,
                          40, 20, false);
  }

  public void hide() {
    if (balloonTip != null) {
      balloonTip.closeBalloon();
      balloonTip = null;
    }
  }

  public void dispose() {
    hide();
    SignpostStatus.setCompleted(completionField, repository);
  }
}
