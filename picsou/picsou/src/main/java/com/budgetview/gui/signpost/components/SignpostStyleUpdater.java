package com.budgetview.gui.signpost.components;

import com.budgetview.gui.components.tips.BalloonTipHolder;
import net.java.balloontip.styles.ModernBalloonStyle;
import org.globsframework.gui.splits.color.ColorChangeListener;
import org.globsframework.gui.splits.color.ColorLocator;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.utils.directory.Directory;

import java.awt.*;

public class SignpostStyleUpdater implements Disposable {

  private final BalloonTipHolder tipHolder;
  private final Directory directory;
  private ColorChangeListener colorChangeListener;

  public static SignpostStyleUpdater install(BalloonTipHolder tipHolder, Directory directory) {
    SignpostStyleUpdater updater = new SignpostStyleUpdater(tipHolder, directory);
    updater.init();
    return updater;
  }

  private SignpostStyleUpdater(BalloonTipHolder tipHolder, Directory directory) {
    this.tipHolder = tipHolder;
    this.directory = directory;
  }

  private void init() {
    colorChangeListener = new ColorChangeListener() {
      public void colorsChanged(ColorLocator colorLocator) {
        Color fillTopColor = colorLocator.get("signpost.bg.top");
        Color fillBottomColor = colorLocator.get("signpost.bg.bottom");
        Color borderColor = colorLocator.get("signpost.border");
        ModernBalloonStyle balloonStyle =
          new ModernBalloonStyle(15, 7, fillTopColor, fillBottomColor, borderColor);
        balloonStyle.setBorderThickness(2);
        tipHolder.setStyle(balloonStyle);
      }
    };
    directory.get(ColorService.class).addListener(colorChangeListener);
  }

  public void dispose() {
    directory.get(ColorService.class).removeListener(colorChangeListener);
  }
}
