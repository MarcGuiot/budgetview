package com.budgetview.gui.signpost;

import com.budgetview.gui.components.tips.BalloonTipHolder;
import com.budgetview.model.SignpostStatus;
import net.java.balloontip.BalloonTip;
import net.java.balloontip.styles.BalloonTipStyle;
import com.budgetview.gui.components.tips.CustomBalloonTip;
import com.budgetview.gui.signpost.components.SignpostStyleUpdater;
import com.budgetview.gui.utils.Gui;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.KeyChangeListener;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.InvalidState;

import javax.swing.*;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;

public abstract class Signpost implements BalloonTipHolder, Disposable {

  protected JComponent component;
  protected GlobRepository repository;
  protected Directory directory;
  protected SelectionService selectionService;

  private BalloonTip balloonTip;
  private SignpostStyleUpdater styleUpdater;
  protected BalloonTipStyle balloonTipStyle;
  private BalloonTip.Orientation orientation;
  private BalloonTip.AttachLocation attachLocation;

  protected Signpost(GlobRepository repository, Directory directory) {
    this.repository = repository;
    this.directory = directory;
    this.selectionService = directory.get(SelectionService.class);
    this.orientation = BalloonTip.Orientation.RIGHT_BELOW;
    this.attachLocation = BalloonTip.AttachLocation.SOUTH;
    this.styleUpdater = SignpostStyleUpdater.install(this, directory);
  }

  public void setStyle(BalloonTipStyle style) {
    this.balloonTipStyle = style;
  }

  protected void setLocation(BalloonTip.Orientation orientation, BalloonTip.AttachLocation attachLocation) {
    this.orientation = orientation;
    this.attachLocation = attachLocation;
  }

  public void attach(JComponent component) {
    if (this.component != null) {
      throw new InvalidState("Signpost '" + getClass().getSimpleName() + "' is already attached to component " + this.component);
    }
    this.component = component;
    setup();
  }

  private void setup() {
    if (isCompleted()) {
      return;
    }

    repository.addChangeListener(new KeyChangeListener(SignpostStatus.KEY) {
      public void update() {
        if (isShowing() && isCompleted()) {
          dispose();
        }
      }
    });

    component.addHierarchyListener(new HierarchyListener() {
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
    });

    init();
  }

  protected abstract void init();

  public abstract boolean isCompleted();

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
      ((JLabel)balloonTip.getContents()).setText(text);
    }
    else {
      balloonTip = createBalloonTip(component, text);
      balloonTip.setVisible(true);
    }
  }

  protected BalloonTip createBalloonTip(JComponent component, String text) {
    return new CustomBalloonTip(component,
                                text,
                                balloonTipStyle,
                                orientation,
                                attachLocation,
                                40, 20, false);
  }

  public void hide() {
    if (styleUpdater != null) {
      styleUpdater.dispose();
      styleUpdater = null;
    }
    if (balloonTip != null) {
      balloonTip.closeBalloon();
      balloonTip = null;
    }
  }

  public void dispose() {
    hide();
  }
}
