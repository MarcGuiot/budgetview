package org.designup.picsou.functests.checkers;

import org.designup.picsou.gui.PicsouApplication;
import org.uispec4j.Trigger;
import org.uispec4j.Window;
import org.uispec4j.interception.WindowInterceptor;

public class StartupChecker extends GuiChecker {
  private PicsouApplication application;

  public Window enterMain() {
    Window sla = WindowInterceptor.getModalDialog(new startMainTrigger());
    return enterFromSla(sla);
  }

  public PicsouApplication getApplication() {
    return application;
  }

  public static Window enterFromSla(Window sla) {
    final SlaValidationDialogChecker slaValidationDialogChecker =
      new SlaValidationDialogChecker(sla);

    slaValidationDialogChecker.acceptTerms();
    SlaValidationDialogChecker.TriggerSlaOk trigger =
      new SlaValidationDialogChecker.TriggerSlaOk(slaValidationDialogChecker);
    trigger.run();
    return trigger.getMainWindow();
  }

  public class startMainTrigger implements Trigger {

    public void run() throws Exception {
      application = new PicsouApplication();
      application.run();
    }
  }
}
