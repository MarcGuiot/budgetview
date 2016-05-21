package com.budgetview.gui.accounts.utils;

import com.budgetview.gui.description.Formatting;
import com.budgetview.gui.notifications.NotificationHandler;
import com.budgetview.gui.notifications.Notification;
import com.budgetview.model.AccountPositionError;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobFieldsComparator;
import org.globsframework.utils.directory.Directory;

import java.util.ArrayList;
import java.util.List;

import static org.globsframework.model.utils.GlobMatchers.isFalse;

public class AccountPositionNotificationHandler implements NotificationHandler {

  public GlobType getType() {
    return AccountPositionError.TYPE;
  }

  public int getNotificationCount(GlobRepository repository) {
    return repository.getAll(AccountPositionError.TYPE, isFalse(AccountPositionError.CLEARED)).size();
  }

  public List<Notification> getNotifications(GlobRepository repository, Directory directory) {
    List<Notification> notifications = new ArrayList<Notification>();
    GlobList errors =
      repository
        .getAll(AccountPositionError.TYPE, isFalse(AccountPositionError.CLEARED))
        .sortSelf(new GlobFieldsComparator(AccountPositionError.UPDATE_DATE, false,
                                           AccountPositionError.ACCOUNT, true));
    for (Glob error : errors) {
      Integer fullDate = error.get(AccountPositionError.LAST_PREVIOUS_IMPORT_DATE);
      String date = fullDate != null ? Formatting.toString(fullDate) : null;
      Glob account = repository.findLinkTarget(error, AccountPositionError.ACCOUNT);
      notifications.add(new AccountPositionNotification(error, date, account, repository, directory));
    }
    return notifications;
  }

}
