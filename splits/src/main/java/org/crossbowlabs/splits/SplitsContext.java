package org.crossbowlabs.splits;

import org.crossbowlabs.splits.color.ColorService;

import javax.swing.*;
import java.awt.*;

public interface SplitsContext {
  void addComponent(String id, Component component);

  Component findComponent(String id);

  <T extends Component> T findOrCreateComponent(String id, String name, Class<T> componentClass);

  Action getAction(String id);

  ColorService getColorService();

  IconLocator getIconLocator();

  TextLocator getTextLocator();
}
