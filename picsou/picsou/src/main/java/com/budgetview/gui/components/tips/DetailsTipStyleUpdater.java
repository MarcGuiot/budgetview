package com.budgetview.gui.components.tips;

import net.java.balloontip.styles.RoundedBalloonStyle;
import org.globsframework.gui.splits.color.ColorChangeListener;
import org.globsframework.gui.splits.color.ColorLocator;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.utils.directory.Directory;

import java.awt.*;

public class DetailsTipStyleUpdater implements Disposable {

  private final BalloonTipHolder tipHolder;
  private final Directory directory;
  private ColorChangeListener colorListener;
  private Color fillColor;
  private Color borderColor;

  public static DetailsTipStyleUpdater install(BalloonTipHolder tipHolder, Directory directory) {
    return new DetailsTipStyleUpdater(tipHolder, directory);
  }

  public DetailsTipStyleUpdater(final BalloonTipHolder tipHolder, Directory directory) {
    this.tipHolder = tipHolder;
    this.directory = directory;

    colorListener = new ColorChangeListener() {
      public void colorsChanged(ColorLocator colorLocator) {
        fillColor = colorLocator.get("detailsTip.bg");
        borderColor = colorLocator.get("detailsTip.border");
          tipHolder.setStyle(createStyle());
      }
    };
    directory.get(ColorService.class).addListener(colorListener);
  }

  private RoundedBalloonStyle createStyle() {
    return new RoundedBalloonStyle(5, 5, fillColor, borderColor);
  }

  public void dispose() {
    directory.get(ColorService.class).removeListener(colorListener);
    colorListener = null;
  }
}
