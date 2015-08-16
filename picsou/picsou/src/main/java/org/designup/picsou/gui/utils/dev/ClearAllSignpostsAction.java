package org.designup.picsou.gui.utils.dev;

import org.designup.picsou.model.SignpostStatus;
import org.globsframework.model.GlobRepository;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ClearAllSignpostsAction extends AbstractAction {
  private GlobRepository repository;

  public ClearAllSignpostsAction(GlobRepository repository) {
    super("Hide signposts");
    this.repository = repository;
  }

  public void actionPerformed(ActionEvent e) {
    SignpostStatus.setAllCompleted(repository);
  }
}
