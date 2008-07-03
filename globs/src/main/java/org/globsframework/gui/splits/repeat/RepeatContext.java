package org.globsframework.gui.splits.repeat;

import org.globsframework.gui.splits.IconLocator;
import org.globsframework.gui.splits.SplitsContext;
import org.globsframework.gui.splits.TextLocator;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.gui.splits.font.FontLocator;
import org.globsframework.gui.splits.impl.AbstractSplitsContext;
import org.globsframework.gui.splits.styles.StyleService;

import java.util.ArrayList;
import java.util.List;

public class RepeatContext extends AbstractSplitsContext {
  private SplitsContext innerContext;
  private List<RepeatCellBuilder.DisposeListener> disposeListeners = new ArrayList<RepeatCellBuilder.DisposeListener>();

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

  public void addDisposeListener(RepeatCellBuilder.DisposeListener listener) {
    this.disposeListeners.add(listener);
  }

  public void dispose() {
    super.dispose();
    for (RepeatCellBuilder.DisposeListener listener : disposeListeners) {
      listener.dispose();
    }
  }
}
