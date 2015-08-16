package org.designup.picsou.gui.utils.dev;

import org.designup.picsou.model.SignpostStatus;
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
