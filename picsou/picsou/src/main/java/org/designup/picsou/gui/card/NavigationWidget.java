package org.designup.picsou.gui.card;

import org.designup.picsou.gui.card.widgets.NavigationWidgetListener;

import javax.swing.*;

public interface NavigationWidget {
  String getName();

  String getTitle();

  Icon getIcon();

  Icon getRolloverIcon();

  Action getAction();

  JComponent getComponent();

  void addListener(NavigationWidgetListener listener);

  void removeListener(NavigationWidgetListener listener);

  boolean isHighlighted();
}
