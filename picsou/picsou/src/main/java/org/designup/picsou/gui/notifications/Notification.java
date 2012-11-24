package org.designup.picsou.gui.notifications;

import java.util.Date;

interface Notification {
  int getId();

  Date getDate();

  String getMessage();

  void clear();
}
