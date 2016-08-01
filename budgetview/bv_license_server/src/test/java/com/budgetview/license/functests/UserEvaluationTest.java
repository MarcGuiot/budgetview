package com.budgetview.license.functests;

import com.budgetview.functests.checkers.ApplicationChecker;
import com.budgetview.gui.PicsouApplication;
import com.budgetview.license.ConnectedTestCase;
import com.budgetview.utils.Lang;
import org.globsframework.utils.logging.Debug;

public class UserEvaluationTest extends ConnectedTestCase {
  private ApplicationChecker application;

  public void setUp() throws Exception {
    System.setProperty(PicsouApplication.IS_DATA_IN_MEMORY, "false");
    super.setUp();
    System.setProperty(PicsouApplication.USER_FEEDBACK_DISABLED, Boolean.toString(false));
    application = new ApplicationChecker();
    application.start();
    System.setProperty(PicsouApplication.DELETE_LOCAL_PREVAYLER_PROPERTY, "false");
  }

  public void tearDown() throws Exception {
    super.tearDown();
    application.dispose();
    System.setProperty(PicsouApplication.DELETE_LOCAL_PREVAYLER_PROPERTY, "true");
    application = null;
  }

  public void testSendYesWithCommentAndEmail() throws Exception {
    startServers();

    application.getOperations().exitWithUserEvaluation()
      .checkSendDisabled()
      .selectYes()
      .checkSendEnabled()
      .enterComment("Blah")
      .enterEmailAddress("toto@example.com")
      .send();

    Thread.sleep(1000);
    application.checkClosed();

    mailServer.checkReceivedMail("admin@mybudgetview.fr")
      .checkSubjectContains("User evaluation: ")
      .checkSubjectContains(PicsouApplication.APPLICATION_VERSION)
      .checkSubjectContains(Lang.getLang())
      .checkContains("Blah", "toto@example.com");

    application.startWithoutSLA();
    application.getOperations().checkExitWithoutDialog();
  }

  public void testSendNo() throws Exception {
    startServers();

    application.getOperations()
      .exitWithUserEvaluation()
      .checkSendDisabled()
      .selectNo()
      .checkSendEnabled()
      .enterComment("Blah")
      .send();

    Thread.sleep(1000);
    application.checkClosed();

    mailServer.checkReceivedMail("admin@mybudgetview.fr")
      .checkSubjectContains("User evaluation: ")
      .checkContains("Blah");

    application.startWithoutSLA();
    application.getOperations().checkExitWithoutDialog();
  }

  public void testNotConnected() throws Exception {

    Debug.enter("Restart 1");
    application.restart();
    Debug.exit();
    Debug.enter("Restart 2");
    application.restart();
    Debug.exit();
    Debug.enter("Exit");
    application.getOperations().checkExitWithoutDialog();
    Debug.exit();

    Debug.enter("Start servers");
    startServers();
    Debug.exit();

    Debug.enter("Restart");

    application.startWithoutSLA();

    Debug.print("Exit with user evaluation");

    application.getOperations().exitWithUserEvaluation()
      .checkSendDisabled()
      .selectYes()
      .checkSendEnabled()
      .enterComment("Blah")
      .enterEmailAddress("toto@example.com")
      .send();

    Thread.sleep(1000);
    application.checkClosed();

    mailServer.checkReceivedMail("admin@mybudgetview.fr")
      .checkSubjectContains("User evaluation: ")
      .checkSubjectContains(PicsouApplication.APPLICATION_VERSION)
      .checkSubjectContains(Lang.getLang())
      .checkContains("Blah", "toto@example.com");

    application.startWithoutSLA();
    application.getOperations().checkExitWithoutDialog();
  }
}
