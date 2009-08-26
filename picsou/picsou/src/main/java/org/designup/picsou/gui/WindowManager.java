package org.designup.picsou.gui;

import org.designup.picsou.gui.components.PicsouFrame;

import javax.swing.*;

public interface WindowManager {
  PicsouFrame getFrame();

  void setPanel(JPanel panel);

  void logout();

  void logOutAndDeleteUser(String name, char[] passwd);
}
