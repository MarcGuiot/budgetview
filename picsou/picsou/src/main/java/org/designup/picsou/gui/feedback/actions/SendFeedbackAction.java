package org.designup.picsou.gui.feedback.actions;

import org.designup.picsou.gui.browsing.BrowsingService;
import org.designup.picsou.model.User;
import org.designup.picsou.utils.Lang;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class SendFeedbackAction extends AbstractAction {
  private GlobRepository repository;
  private Directory directory;

  public SendFeedbackAction(String text, GlobRepository repository, Directory directory) {
    super(text);
    this.repository = repository;
    this.directory = directory;
  }

  public SendFeedbackAction(GlobRepository repository, Directory directory) {
    this(Lang.get("feedback"), repository, directory);
  }

  public void actionPerformed(ActionEvent e) {
    String email = repository.get(User.KEY).get(User.EMAIL);
    String url  =
      Strings.isNotEmpty(email) ? Lang.get("feedback.url.email", email) : Lang.get("feedback.url");
    directory.get(BrowsingService.class).launchBrowser(url);
  }
}
