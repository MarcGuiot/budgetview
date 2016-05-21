package com.budgetview.gui.notifications.standard;

import com.budgetview.gui.notifications.GlobNotification;
import com.budgetview.model.StandardMessage;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;

import javax.swing.*;

public class StandardMessageNotification extends GlobNotification {
  public StandardMessageNotification(Glob glob, GlobRepository repository) {
    super(glob, glob.get(StandardMessage.MESSAGE), StandardMessage.DATE, StandardMessage.CLEARED, repository);
  }

  public Action getAction() {
    return null;
  }
}
