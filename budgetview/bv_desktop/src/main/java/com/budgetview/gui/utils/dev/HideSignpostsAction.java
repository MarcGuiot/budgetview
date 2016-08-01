package com.budgetview.gui.utils.dev;

import com.budgetview.model.SignpostStatus;
import org.globsframework.model.GlobRepository;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class HideSignpostsAction extends AbstractAction {
  public static final String LABEL = "Hide signposts";
  private GlobRepository repository;

  public HideSignpostsAction(GlobRepository repository) {
    super(LABEL);
    this.repository = repository;
  }

  public void actionPerformed(ActionEvent e) {
    SignpostStatus.setAllCompleted(repository);
  }
}
