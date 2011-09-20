package org.designup.picsou.functests.checkers;

import junit.framework.Assert;
import org.designup.picsou.gui.PicsouApplication;
import org.uispec4j.Trigger;
import org.uispec4j.Window;
import static org.uispec4j.assertion.UISpecAssert.assertFalse;
import org.uispec4j.interception.WindowInterceptor;

public class ApplicationChecker extends GuiChecker {
  private PicsouApplication application;
  private static Window window;

  private NewVersionChecker newVersion;
  private OperationChecker operations;
  private ViewSelectionChecker views;
  private TransactionChecker transactions;

  public Window start() {
    application = new PicsouApplication();
    Window slaWindow = WindowInterceptor.getModalDialog(new Trigger() {
      public void run() throws Exception {
        application.run();
      }
    });
    return enterFromSla(slaWindow);
  }


  public Window startWithoutSLA() {
    application = new PicsouApplication();
    clearCheckers();
    window = WindowInterceptor.run(new Trigger() {
      public void run() throws Exception {
        application.run();
      }
    });
    return window;
  }

  public void restart() {
    if (application == null) {
      Assert.fail("Application was not started");
    }
    getOperations().checkExitWithoutDialog();
    startWithoutSLA();
  }

  public Window enterFromSla(Window sla) {
    final SlaValidationDialogChecker slaValidation =
      new SlaValidationDialogChecker(sla);
    slaValidation.acceptTerms();
    SlaValidationDialogChecker.TriggerSlaOk trigger =
      new SlaValidationDialogChecker.TriggerSlaOk(slaValidation);
    trigger.run();
    window = trigger.getMainWindow();
    clearCheckers();
    return window;
  }

  public PicsouApplication getApplication() {
    return application;
  }

  public void dispose() {
    if (window != null) {
      window.dispose();
      window = null;
    }
    if (application != null) {
      application.shutdown();
      application = null;
    }
    clearCheckers();
  }

  private void clearCheckers() {
    operations = null;
    newVersion = null;
    views = null;
    transactions = null;
  }

  public Window getWindow() {
    if (window == null) {
      Assert.fail("Window not opened");
    }
    return window;
  }

  public OperationChecker getOperations() {
    checkApplicationStarted();
    if (operations == null) {
      operations = new OperationChecker(window);
    }
    return operations;
  }

  public ViewSelectionChecker getViews() {
    checkApplicationStarted();
    if (views == null) {
      views = new ViewSelectionChecker(window);
    }
    return views;
  }

  public TransactionChecker getTransactions() {
    checkApplicationStarted();
    if (transactions == null) {
      transactions = new TransactionChecker(window);
    }
    return transactions;
  }

  public NewVersionChecker getNewVersion() {
    checkApplicationStarted();
    if (newVersion == null) {
      newVersion = new NewVersionChecker(window);
    }
    return newVersion;
  }

  public void checkClosed() {
    checkApplicationStarted();
    assertFalse(window.isVisible());
  }

  private void checkApplicationStarted() {
    if (window == null) {
      Assert.fail("Application not started");
    }
  }
}
