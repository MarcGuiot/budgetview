package org.designup.picsou.gui.actions;

import org.designup.picsou.gui.WindowManager;
import org.designup.picsou.gui.config.ConfigService;
import org.designup.picsou.gui.feedback.UserEvaluationDialog;
import org.designup.picsou.gui.feedback.UserProgressInfoSender;
import org.designup.picsou.model.SignpostStatus;
import org.designup.picsou.model.User;
import org.designup.picsou.model.UserPreferences;
import org.designup.picsou.utils.Lang;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ExitAction extends AbstractAction {
  private JFrame frame;
  private WindowManager windowManager;
  private GlobRepository repository;
  private Directory directory;
  private boolean showUserEvaluation = true;
  private boolean exitRequested = false;

  public ExitAction(WindowManager windowManager, GlobRepository repository, Directory directory) {
    super(Lang.get("exit"));
    this.windowManager = windowManager;
    this.repository = repository;
    this.directory = directory;
    this.frame = directory.get(JFrame.class);
  }

  public ExitAction(WindowManager windowManager, GlobRepository repository, Directory directory, boolean showUserEvaluation) {
    this(windowManager, repository, directory);
    this.showUserEvaluation = showUserEvaluation;
  }

  public void actionPerformed(ActionEvent e) {
    if (exitRequested) {
      return;
    }
    exitRequested = true;

    if (showUserEvaluation) {
      UserEvaluationDialog.showIfNeeded(repository, directory);
    }

    frame.setVisible(false);
    frame.dispose();
    windowManager.shutdown();
  }
}
