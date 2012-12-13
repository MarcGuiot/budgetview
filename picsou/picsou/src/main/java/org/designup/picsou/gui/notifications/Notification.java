package org.designup.picsou.gui.notifications;

import javax.swing.*;
import java.util.Date;

interface Notification {
  int getId();

  Date getDate();

  String getMessage();

  Action getAction();

  void clear();
}
