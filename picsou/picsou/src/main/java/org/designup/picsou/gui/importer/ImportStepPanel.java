package org.designup.picsou.gui.importer;

import javax.swing.*;

public interface ImportStepPanel extends MessageHandler {
  JPanel getPanel();

  void requestFocus();
}
