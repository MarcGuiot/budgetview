package org.designup.picsou.gui.notifications;

import org.designup.picsou.gui.components.dialogs.CancelAction;
import org.designup.picsou.gui.components.dialogs.PicsouDialog;
import org.designup.picsou.gui.description.Formatting;
import org.designup.picsou.model.Account;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.actions.DisabledAction;
import org.globsframework.gui.splits.PanelBuilder;
import org.globsframework.gui.splits.SplitsBuilder;
import org.globsframework.gui.splits.repeat.Repeat;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.repository.LocalGlobRepository;
import org.globsframework.model.repository.LocalGlobRepositoryBuilder;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class NotificationsDialog {
  private Directory directory;
  private LocalGlobRepository localRepository;
  private Repeat<Notification> repeat;
  private List<Notification> notifications;

  public NotificationsDialog(GlobRepository parentRepository, Directory directory) {
    this.directory = directory;
    NotificationService notificationService = directory.get(NotificationService.class);

    LocalGlobRepositoryBuilder builder = LocalGlobRepositoryBuilder.init(parentRepository);
    builder.copy(Account.TYPE);
    for (GlobType type : notificationService.getTypes()) {
      builder.copy(type);
    }
    this.localRepository = builder.get();
    this.notifications = notificationService.getNotifications(localRepository, directory);
  }

  public void show() {
    PicsouDialog dialog = PicsouDialog.create(directory.get(JFrame.class), directory);
    SplitsBuilder builder = SplitsBuilder.init(directory);
    builder.setSource(getClass(), "/layout/notifications/notificationsDialog.splits");

    repeat = builder
      .addRepeat("messages", notifications, new RepeatComponentFactory<Notification>() {
        public void registerComponents(PanelBuilder cellBuilder, Notification notification) {
          cellBuilder.add("date", new JLabel(Formatting.toString(notification.getDate())));

          JTextArea messageText = new JTextArea(notification.getMessage());
          cellBuilder.add("message", messageText);

          cellBuilder.add("action", getAction(notification));
          cellBuilder.add("delete", new DeleteNotificationAction(notification));
        }
      });

    dialog.addPanelWithButtons(builder.<JPanel>load(),
                               new ValidateAction(dialog),
                               new CancelAction(dialog),
                               new DeleteAllNotificationsAction());

    dialog.pack();
    dialog.showCentered();
  }

  public Action getAction(Notification notification) {
    Action action = notification.getAction();
    if (action == null) {
      return new DisabledAction();
    }
    return action;
  }

  private class ValidateAction extends AbstractAction {
    private final PicsouDialog dialog;

    public ValidateAction(PicsouDialog dialog) {
      super(Lang.get("ok"));
      this.dialog = dialog;
    }

    public void actionPerformed(ActionEvent e) {
      localRepository.commitChanges(true);
      dialog.setVisible(false);
    }
  }

  private class DeleteNotificationAction extends AbstractAction {
    private Notification notification;

    public DeleteNotificationAction(Notification notification) {
      super(Lang.get("notifications.delete"));
      this.notification = notification;
    }

    public void actionPerformed(ActionEvent actionEvent) {
      notification.clear();
      notifications.remove(notification);
      repeat.set(notifications);
    }
  }

  private class DeleteAllNotificationsAction extends AbstractAction {

    public DeleteAllNotificationsAction() {
      super(Lang.get("notifications.deleteAll"));
    }

    public void actionPerformed(ActionEvent actionEvent) {
      for (Notification notification : notifications) {
        notification.clear();
      }
      notifications.clear();
      repeat.set(notifications);
    }
  }
}
