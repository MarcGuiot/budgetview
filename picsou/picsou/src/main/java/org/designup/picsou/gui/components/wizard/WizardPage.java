package org.designup.picsou.gui.components.wizard;

import javax.swing.*;

public interface WizardPage {

  String getId();

  String getTitle();

  JComponent getPanel();
  
  void init();
  void update();
  void applyChanges();
}
