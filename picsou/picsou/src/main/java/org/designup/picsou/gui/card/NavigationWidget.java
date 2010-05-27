package org.designup.picsou.gui.card;

import org.designup.picsou.gui.signpost.Signpost;

import javax.swing.*;

public interface NavigationWidget {
  String getName();

  String getTitle();

  Icon getIcon();

  Icon getRolloverIcon();

  Action getAction();

  boolean isNavigation();

  JComponent getComponent();

  Signpost getSignpost();

}
