package org.designup.picsou.gui.components.wizard;

import javax.swing.*;

public interface WizardPage {

  String getId();

  String getHelpRef();

  JComponent getPanel();

  void init();

  void updateBeforeDisplay();

  void updateAfterDisplay();
}
