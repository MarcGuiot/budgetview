package com.budgetview.desktop.importer;

import javax.swing.*;

public interface ImportStepPanel extends MessageHandler {
  JPanel getPanel();

  void requestFocus();
}
