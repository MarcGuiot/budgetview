package org.designup.picsou.gui.signpost;

import net.java.balloontip.BalloonTip;
import net.java.balloontip.styles.ModernBalloonStyle;
import org.designup.picsou.model.SignpostStatus;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.SelectionService;
import org.globsframework.metamodel.fields.BooleanField;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.KeyChangeListener;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.InvalidState;

import javax.swing.*;
import java.awt.*;

public abstract class Signpost {

  private JComponent component;
  private BooleanField completionField;
  protected GlobRepository repository;
  protected Directory directory;
  protected SelectionService selectionService;

  protected static final ModernBalloonStyle BALLOON_STYLE = createBalloonStyle();

  private static ModernBalloonStyle createBalloonStyle() {
    ModernBalloonStyle style =
      new ModernBalloonStyle(15, 7, Color.YELLOW.brighter(), Color.YELLOW, Color.ORANGE);
    style.setBorderThickness(2);
    return style;
  }

  private BalloonTip balloonTip;
  private KeyChangeListener completionListener;

  protected Signpost(JComponent component, BooleanField completionField, GlobRepository repository, Directory directory) {
    this(completionField, repository, directory);
    this.component = component;
  }

  protected Signpost(BooleanField completionField, GlobRepository repository, Directory directory) {
    this.completionField = completionField;
    this.repository = repository;
    this.directory = directory;
    this.selectionService = directory.get(SelectionService.class);
  }

  public void activate() {
    if (this.component == null) {
      throw new InvalidState("No component was set for " + getClass().getSimpleName());
    }
    setup();
  }

  public void activate(JComponent component) {
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
          hide();
        }
      }
    };
    repository.addChangeListener(completionListener);

    init();
  }

  protected abstract void init();

  private boolean isCompleted() {
    return SignpostStatus.isCompleted(completionField, repository);
  }

  protected boolean canShow() {
    return !isCompleted();
  }

  protected boolean isShowing() {
    return balloonTip != null;
  }

  protected void show(String textKey, Object... args) {
    if (isCompleted()) {
      return;
    }
    String text = Lang.get(textKey, args);
    if (isShowing()) {
      balloonTip.setText(text);
    }
    else {
      balloonTip = createBalloonTip(component, text);
    }
  }

  protected BalloonTip createBalloonTip(JComponent component, String text) {
    return new BalloonTip(component,
                          text,
                          BALLOON_STYLE,
                          BalloonTip.Orientation.RIGHT_BELOW,
                          BalloonTip.AttachLocation.SOUTH,
                          40, 20, false);
  }

  protected void hide() {
    if (balloonTip != null) {
      balloonTip.closeBalloon();
      balloonTip = null;
    }
    SignpostStatus.setCompleted(completionField, repository);
  }
}
