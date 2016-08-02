package com.budgetview.desktop.notifications;

import com.budgetview.desktop.View;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.ChangeSet;
import org.globsframework.model.ChangeSetListener;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Set;

public class NotificationsFlagView extends View {
  private final NotificationService notificationService;
  private ShowErrorAction showMessagesAction;
  private GlobRepository repository;

  public NotificationsFlagView(GlobRepository repository, Directory directory) {
    super(repository, directory);
    this.repository = repository;
    this.notificationService = directory.get(NotificationService.class);
  }

  public void registerComponents(GlobsPanelBuilder builder) {
    showMessagesAction = new ShowErrorAction();
    updateButton(repository);
    builder.add("notificationsFlag", new JButton(showMessagesAction));
    repository.addChangeListener(new ChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        for (GlobType type : notificationService.getTypes()) {
          if (changeSet.containsChanges(type)) {
            updateButton(repository);
          }
        }
      }

      public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
        for (GlobType type : notificationService.getTypes()) {
          if (changedTypes.contains(type)) {
            updateButton(repository);
          }
        }
      }
    });
    updateButton(repository);
  }

  private void updateButton(GlobRepository repository) {
    int count = notificationService.getNotificationCount(repository);
    showMessagesAction.putValue(Action.NAME, Integer.toString(count));
    showMessagesAction.setEnabled(count > 0);
  }

  private class ShowErrorAction extends AbstractAction {
    public void actionPerformed(ActionEvent e) {
      NotificationsDialog dialog = new NotificationsDialog(repository, directory);
      dialog.show();
    }
  }
}
