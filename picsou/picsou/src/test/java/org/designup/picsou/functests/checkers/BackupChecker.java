package org.designup.picsou.functests.checkers;

import org.uispec4j.interception.WindowInterceptor;
import org.uispec4j.interception.FileChooserHandler;
import org.uispec4j.interception.WindowHandler;
import org.uispec4j.Trigger;
import org.uispec4j.Window;

public class BackupChecker {
  private OperationChecker operationsChecker;

  public BackupChecker(OperationChecker operationsChecker) {
    this.operationsChecker = operationsChecker;
  }

  public void backup(String fileName){
  WindowInterceptor.init(operationsChecker.getRestoreTrigger())
    .process(FileChooserHandler.init().select(fileName))
    .process(new WindowHandler() {
      public Trigger process(Window window) throws Exception {
        PasswordDialogChecker dialog = new PasswordDialogChecker(window);
        dialog.checkTitle("Secure backup");
        dialog.setPassword("password");
        return dialog.getOkTrigger();
      }
    })
    .process(new WindowHandler() {
      public Trigger process(Window window) throws Exception {
        MessageFileDialogChecker dialog = new MessageFileDialogChecker(window);
        dialog.checkMessageContains("Restore done");
        return dialog.getOkTrigger();
      }
    })
    .run();

}
}
