package org.designup.picsou.gui.help.actions;

import org.designup.picsou.gui.components.dialogs.MessageAndDetailsDialog;
import org.designup.picsou.gui.startup.components.AppLogger;
import org.designup.picsou.utils.Lang;
import org.globsframework.utils.Files;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;

public class SendLogsAction extends AbstractAction {

  private Directory directory;

  public SendLogsAction(Directory directory) {
    super(Lang.get("sendLogs.action"));
    this.directory = directory;
  }

  public void actionPerformed(ActionEvent actionEvent) {
    MessageAndDetailsDialog dialog =
      new MessageAndDetailsDialog("sendLogs.title", "sendLogs.message",
                                  loadLog(), directory.get(JFrame.class), directory);
    dialog.show();
  }

  private String loadLog() {
    File logFile = AppLogger.getLogFile();
    if (!logFile.exists()) {
      return "No logs available in: " + logFile.getAbsolutePath();
    }
    return Files.loadFileToString(logFile.getAbsolutePath());
  }
}
