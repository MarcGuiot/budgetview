package org.designup.picsou.gui.feedback.actions;

import org.designup.picsou.gui.browsing.BrowsingService;
import org.designup.picsou.utils.Lang;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class SendFeedbackAction extends AbstractAction {
  private Directory directory;

  public SendFeedbackAction(String text, Directory directory) {
    super(text);
    this.directory = directory;
  }

  public SendFeedbackAction(Directory directory) {
    this(Lang.get("feedback"), directory);
  }

  public void actionPerformed(ActionEvent e) {
    directory.get(BrowsingService.class).launchBrowser(Lang.get("feedback.url"));
  }
}
