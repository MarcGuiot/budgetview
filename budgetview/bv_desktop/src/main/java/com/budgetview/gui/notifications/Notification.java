package com.budgetview.gui.notifications;

import javax.swing.*;
import java.util.Date;

public interface Notification {

  Date getDate();

  String getMessage();

  Action getAction();

  void clear();
}
