package org.designup.picsou.gui.messages;

import java.util.Date;

interface MessageDisplay {
  int getId();

  Date getDate();

  String getMessage();

  boolean isCleared();

  void clear(boolean clear);
}
