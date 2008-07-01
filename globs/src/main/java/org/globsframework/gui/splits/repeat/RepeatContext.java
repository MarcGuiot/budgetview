package org.globsframework.gui.splits.repeat;

import org.globsframework.gui.splits.IconLocator;
import org.globsframework.gui.splits.SplitsContext;
import org.globsframework.gui.splits.TextLocator;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.gui.splits.font.FontLocator;
import org.globsframework.gui.splits.impl.AbstractSplitsContext;
import org.globsframework.gui.splits.styles.StyleService;

public class RepeatContext extends AbstractSplitsContext {
  private SplitsContext innerContext;

  public RepeatContext(SplitsContext innerContext) {
    this.innerContext = innerContext;
  }

  public ColorService getColorService() {
    return innerContext.getColorService();
  }

  public IconLocator getIconLocator() {
    return innerContext.getIconLocator();
  }

  public TextLocator getTextLocator() {
    return innerContext.getTextLocator();
  }

  public FontLocator getFontLocator() {
    return innerContext.getFontLocator();
  }

  public StyleService getStyleService() {
    return innerContext.getStyleService();
  }

  public Class getReferenceClass() {
    return innerContext.getReferenceClass();
  }

}
