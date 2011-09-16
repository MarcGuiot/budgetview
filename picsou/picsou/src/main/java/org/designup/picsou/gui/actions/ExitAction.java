package org.designup.picsou.gui.actions;

import org.designup.picsou.gui.feedback.UserEvaluationDialog;
import org.designup.picsou.utils.Lang;
import org.designup.picsou.gui.WindowManager;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ExitAction extends AbstractAction {
  private JFrame frame;
  private WindowManager windowManager;
  private GlobRepository repository;
  private Directory directory;

  public ExitAction(WindowManager windowManager, GlobRepository repository, Directory directory) {
    super(Lang.get("exit"));
    this.windowManager = windowManager;
    this.repository = repository;
    this.directory = directory;
    this.frame = directory.get(JFrame.class);
  }

  public void actionPerformed(ActionEvent e) {

    UserEvaluationDialog.showIfNeeded(repository, directory);

    frame.setVisible(false);
    frame.dispose();
    windowManager.shutdown();
  }
}
