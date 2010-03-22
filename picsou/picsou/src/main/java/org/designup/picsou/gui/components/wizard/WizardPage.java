package org.designup.picsou.gui.components.wizard;

import javax.swing.*;

public interface WizardPage {

  String getId();

  String getTitle();

  JComponent getPanel();

  void init();

  void updateBeforeDisplay();

  void updateAfterDisplay();

  void applyChanges();
}
