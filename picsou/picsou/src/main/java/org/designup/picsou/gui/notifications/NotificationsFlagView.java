package org.designup.picsou.gui.notifications;

import org.designup.picsou.gui.View;
import org.designup.picsou.model.AccountPositionError;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.model.*;
import org.globsframework.model.utils.TypeChangeSetListener;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

import static org.globsframework.model.utils.GlobMatchers.isFalse;

public class NotificationsFlagView extends View {
  private ShowErrorAction showMessagesAction;
  private GlobRepository repository;

  public NotificationsFlagView(GlobRepository repository, Directory directory) {
    super(repository, directory);
    this.repository = repository;
  }

  public void registerComponents(GlobsPanelBuilder builder) {
    showMessagesAction = new ShowErrorAction();
    updateButton(repository);
    builder.add("notificationsFlag", new JButton(showMessagesAction));
    repository.addChangeListener(new TypeChangeSetListener(AccountPositionError.TYPE) {
      public void update(GlobRepository repository) {
        updateButton(repository);
      }
    });
    updateButton(repository);
  }

  private void updateButton(GlobRepository repository) {
    GlobList notifications = repository.getAll(AccountPositionError.TYPE, isFalse(AccountPositionError.CLEARED));
    showMessagesAction.putValue(Action.NAME, Integer.toString(notifications.size()));
    showMessagesAction.setEnabled(!notifications.isEmpty());
  }

  private class ShowErrorAction extends AbstractAction {
    public void actionPerformed(ActionEvent e) {
      NotificationsDialog dialog = new NotificationsDialog(repository, directory);
      dialog.show();
    }
  }
}
