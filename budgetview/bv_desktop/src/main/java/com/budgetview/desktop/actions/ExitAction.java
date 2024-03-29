package com.budgetview.desktop.actions;

import com.budgetview.desktop.WindowManager;
import com.budgetview.desktop.feedback.UserEvaluationDialog;
import com.budgetview.utils.Lang;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ExitAction extends AbstractAction {
  private JFrame frame;
  private WindowManager windowManager;
  private GlobRepository repository;
  private Directory directory;
  private boolean enabled = true;
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
    if (System.getProperty("realExit", "true").equalsIgnoreCase("true")){
      System.exit(0);
    }
  }
}
