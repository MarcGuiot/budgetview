package org.globsframework.gui.utils;

import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobListFunctor;

import javax.swing.*;

public class GlobListActionAdapter implements GlobListFunctor {
  private Action action;

  public GlobListActionAdapter(Action action) {
    this.action = action;
  }

  public void run(GlobList list, GlobRepository repository) {
    action.actionPerformed(null);
  }
}
