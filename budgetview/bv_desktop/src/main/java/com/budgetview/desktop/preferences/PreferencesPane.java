package com.budgetview.desktop.preferences;

import javax.swing.*;

public interface PreferencesPane {
  void prepareForDisplay();

  JPanel getPanel();

  void validate(PreferencesResult result);

  void postValidate();

  void processCancel();
}
