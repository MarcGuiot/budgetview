package org.designup.picsou.gui.actions;

import org.designup.picsou.gui.feedback.UserEvaluationDialog;
import org.designup.picsou.utils.Lang;
import org.designup.picsou.gui.WindowManager;
import org.designup.picsou.gui.config.ConfigService;
import org.designup.picsou.model.SignpostStatus;
import org.designup.picsou.model.UserPreferences;
import org.designup.picsou.model.User;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Glob;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ExitAction extends AbstractAction {
  private JFrame frame;
  private WindowManager windowManager;
  private GlobRepository repository;
  private Directory directory;
  private boolean showUserEvaluation = true;

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

    if (showUserEvaluation){
      UserEvaluationDialog.showIfNeeded(repository, directory);
    }

    Glob glob = repository.get(SignpostStatus.KEY);
    Glob userPrefs = repository.get(UserPreferences.KEY);
    int exitCount = userPrefs.get(UserPreferences.EXIT_COUNT, 0);

    if (repository.get(User.KEY).isTrue(User.CONNECTED) && exitCount < 3) {
      try {
        Boolean importDone = glob.get(SignpostStatus.IMPORT_DONE, Boolean.FALSE);
        Boolean categorisationDone = glob.get(SignpostStatus.CATEGORIZATION_SELECTION_DONE, Boolean.FALSE);
        Boolean categorisationAreaSelection = glob.get(SignpostStatus.CATEGORIZATION_AREA_SELECTION_DONE, Boolean.FALSE);
        Boolean firstCategorisationDone = glob.get(SignpostStatus.FIRST_CATEGORIZATION_DONE, Boolean.FALSE);
        Boolean categorisationSkipped = glob.get(SignpostStatus.CATEGORIZATION_SKIPPED, Boolean.FALSE);
        Boolean gotoBudgetShown = glob.get(SignpostStatus.GOTO_BUDGET_SHOWN, Boolean.FALSE);
        directory.get(ConfigService.class).sendUse("use : " + exitCount + "" +
                                                   ", import : " + importDone +"" +
                                                   ", categorizationDone : " + categorisationDone + "" +
                                                   ", categorisationAreaSelection : " + categorisationAreaSelection + "" +
                                                   ", firstCategorisationDone : " + firstCategorisationDone + "" +
                                                   ", categorisationSkipped : " + categorisationSkipped + "" +
                                                   ", gotoBudgetShown : " + gotoBudgetShown);
      }
      catch (Exception exception) {
      }
    }

    frame.setVisible(false);
    frame.dispose();
    windowManager.shutdown();
  }
}
