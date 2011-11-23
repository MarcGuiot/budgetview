package org.designup.picsou.gui.utils;

import org.designup.picsou.gui.PicsouApplication;
import org.designup.picsou.gui.about.AboutDialog;
import org.designup.picsou.gui.components.dialogs.MessageAndDetailsDialog;
import org.globsframework.utils.exceptions.ExceptionHandler;
import org.globsframework.utils.Log;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public class ShowDialogAndExitExceptionHandler implements ExceptionHandler {

  private boolean firstReset = true;
  private Directory directory;

  public ShowDialogAndExitExceptionHandler(Directory directory) {
    this.directory = directory;
  }

  public void onException(Throwable ex) {
    Log.write(ex.getMessage(), ex);
    if (!firstReset || !PicsouApplication.EXIT_ON_DATA_ERROR) {
      MessageAndDetailsDialog dialog = new MessageAndDetailsDialog("exception.title",
                                                                   "exception.content",
                                                                   getMessage(ex),
                                                                   directory.get(JFrame.class),
                                                                   directory);
      dialog.show();
      if (PicsouApplication.EXIT_ON_DATA_ERROR) {
        System.exit(10);
      }
    }
  }

  private String getMessage(Throwable ex) {
    StringBuilder builder = new StringBuilder();

    builder.append("version: ")
      .append(PicsouApplication.APPLICATION_VERSION)
      .append('\n');

    for (String property : AboutDialog.SYSTEM_PROPERTIES) {
      builder
        .append(property).append(": ")
        .append(System.getProperty(property))
        .append('\n');
    }

    builder.append(Strings.toString(ex));
    return builder.toString();
  }

  public void setFirstReset(boolean firstReset) {
    this.firstReset = firstReset;
  }
}
