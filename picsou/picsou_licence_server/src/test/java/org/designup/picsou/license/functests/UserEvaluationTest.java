package org.designup.picsou.license.functests;

import org.designup.picsou.functests.checkers.ApplicationChecker;
import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.gui.PicsouApplication;
import org.designup.picsou.license.ConnectedTestCase;
import org.globsframework.utils.logging.Debug;

public class UserEvaluationTest extends ConnectedTestCase {
  private ApplicationChecker application;

  public void setUp() throws Exception {
    System.setProperty(PicsouApplication.IS_DATA_IN_MEMORY, "false");
    super.setUp();
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

    application.restart();

    application.getOperations().exitWithUserEvaluation()
      .checkSendDisabled()
      .selectYes()
      .checkSendEnabled()
      .enterComment("Blah")
      .enterEmailAddress("toto@example.com")
      .send();

    application.checkClosed();

    mailServer.checkReceivedMail("support@mybudgetview.fr")
      .checkHeader("User evaluation: ")
      .checkContains("Blah", "toto@example.com");

    application.startWithoutSLA();
    application.getOperations().checkExitWithoutDialog();
  }

  public void testSendNo() throws Exception {
    startServers();

    application.restart();

    application.getOperations().exitWithUserEvaluation()
      .checkSendDisabled()
      .selectNo()
      .checkSendEnabled()
      .enterComment("Blah")
      .send();

    application.checkClosed();

    mailServer.checkReceivedMail("support@mybudgetview.fr")
      .checkHeader("User evaluation: ")
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

    application.checkClosed();

    mailServer.checkReceivedMail("support@mybudgetview.fr")
      .checkHeader("User evaluation: ")
      .checkContains("Blah", "toto@example.com");

    application.startWithoutSLA();
    application.getOperations().checkExitWithoutDialog();
  }

  public void testConnectionDroppedWhenDialogOpened() throws Exception {
    fail("[Regis] Todo");
  }
}
