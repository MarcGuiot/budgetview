package com.budgetview.desktop;

import com.budgetview.desktop.components.PicsouFrame;

import javax.swing.*;

public interface WindowManager {
  PicsouFrame getFrame();

  void setPanel(JPanel panel);

  void logout();

  void logOutAndDeleteUser(String name, char[] passwd);

  void logOutAndOpenDemo();

  void logOutAndAutoLogin();

  void shutdown();
}
