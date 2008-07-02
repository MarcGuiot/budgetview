package org.globsframework.gui.splits;

import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.gui.splits.font.FontLocator;
import org.globsframework.gui.splits.styles.StyleService;
import org.globsframework.gui.splits.repeat.Repeat;

import javax.swing.*;
import java.awt.*;

public interface SplitsContext {

  void addComponent(String id, Component component);

  Component findComponent(String id);

  <T extends Component> T findOrCreateComponent(String ref, String name, Class<T> componentClass, String splitterName);

  Repeat getRepeat(String name);

  void add(String name, Action action);

  Action getAction(String id);

  ColorService getColorService();

  IconLocator getIconLocator();

  TextLocator getTextLocator();

  FontLocator getFontLocator();

  StyleService getStyleService();

  Class getReferenceClass();

  void addOrReplaceComponent(String id, Component component);

  void addAutoHide(Component targetComponent, String sourceComponentName);

  void cleanUp();
}
