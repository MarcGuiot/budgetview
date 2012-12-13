package org.designup.picsou.gui.notifications;

import org.designup.picsou.gui.accounts.utils.AccountPositionNotification;
import org.designup.picsou.gui.components.dialogs.CancelAction;
import org.designup.picsou.gui.components.dialogs.PicsouDialog;
import org.designup.picsou.gui.description.Formatting;
import org.designup.picsou.model.Account;
import org.designup.picsou.model.AccountPositionError;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.splits.SplitsBuilder;
import org.globsframework.gui.splits.repeat.Repeat;
import org.globsframework.gui.splits.repeat.RepeatCellBuilder;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.repository.LocalGlobRepository;
import org.globsframework.model.repository.LocalGlobRepositoryBuilder;
import org.globsframework.model.utils.GlobComparators;
import org.globsframework.model.utils.GlobFieldsComparator;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import static org.globsframework.model.utils.GlobMatchers.isFalse;

public class NotificationsDialog {
  private Directory directory;
  private LocalGlobRepository repository;
  private Repeat<Notification> repeat;
  private List<Notification> notifications;

  public NotificationsDialog(GlobRepository parentRepository, Directory directory) {
    this.directory = directory;
    this.repository =
      LocalGlobRepositoryBuilder.init(parentRepository)
        .copy(Account.TYPE, AccountPositionError.TYPE)
        .get();
    this.notifications = loadNotifications(repository);
  }

  public void show() {
    PicsouDialog dialog = PicsouDialog.create(directory.get(JFrame.class), directory);
    SplitsBuilder builder = SplitsBuilder.init(directory);
    builder.setSource(getClass(), "/layout/notifications/notificationsDialog.splits");

    repeat = builder
      .addRepeat("messages", notifications, new RepeatComponentFactory<Notification>() {
        public void registerComponents(RepeatCellBuilder cellBuilder, Notification notification) {
          cellBuilder.add("date", new JLabel(Formatting.toString(notification.getDate())));

          JTextArea messageText = new JTextArea(notification.getMessage());
          cellBuilder.add("message", messageText);

          cellBuilder.add("action", notification.getAction());
          cellBuilder.add("delete", new DeleteNotificationAction(notification));
        }
      });

    dialog.addPanelWithButtons(builder.<JPanel>load(),
                               new ValidateAction(dialog),
                               new CancelAction(dialog));

    dialog.pack();
    dialog.showCentered();
  }

  private List<Notification> loadNotifications(GlobRepository localRepository) {
    List<Notification> notifications = new ArrayList<Notification>();
    int i = 0;
    GlobList errors =
      repository
        .getAll(AccountPositionError.TYPE, isFalse(AccountPositionError.CLEARED))
        .sort(new GlobFieldsComparator(AccountPositionError.UPDATE_DATE, false,
                                       AccountPositionError.ACCOUNT, true));
    for (Glob error : errors) {
      Integer fullDate = error.get(AccountPositionError.LAST_PREVIOUS_IMPORT_DATE);
      String date = fullDate != null ? Formatting.toString(fullDate) : null;
      Glob account = repository.findLinkTarget(error, AccountPositionError.ACCOUNT);
      int id = ++i;
      notifications.add(createNotification(localRepository, error, date, account, id));
    }
    return notifications;
  }

  private GlobNotification createNotification(GlobRepository localRepository, final Glob error, final String date, final Glob account, final int id) {
    return new AccountPositionNotification(localRepository, directory, error, date, account, id);
  }

  private class ValidateAction extends AbstractAction {
    private final PicsouDialog dialog;

    public ValidateAction(PicsouDialog dialog) {
      super(Lang.get("ok"));
      this.dialog = dialog;
    }

    public void actionPerformed(ActionEvent e) {
      repository.commitChanges(true);
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
}
