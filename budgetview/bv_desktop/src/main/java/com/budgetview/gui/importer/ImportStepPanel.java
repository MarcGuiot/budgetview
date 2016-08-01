package com.budgetview.gui.importer;

import javax.swing.*;

public interface ImportStepPanel extends MessageHandler {
  JPanel getPanel();

  void requestFocus();
}
