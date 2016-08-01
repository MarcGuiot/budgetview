package com.budgetview.gui.notifications.standard;

import com.budgetview.gui.notifications.Notification;
import com.budgetview.gui.notifications.NotificationHandler;
import com.budgetview.gui.time.TimeService;
import com.budgetview.model.StandardMessage;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.FieldValue;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobFieldsComparator;
import org.globsframework.utils.directory.Directory;

import java.util.ArrayList;
import java.util.List;

import static org.globsframework.model.FieldValue.value;
import static org.globsframework.model.utils.GlobMatchers.isFalse;

public class StandardMessageNotificationHandler implements NotificationHandler {

  public GlobType getType() {
    return StandardMessage.TYPE;
  }

  public int getNotificationCount(GlobRepository repository) {
    return repository.getAll(StandardMessage.TYPE, isFalse(StandardMessage.CLEARED)).size();
  }

  public List<Notification> getNotifications(GlobRepository repository, Directory directory) {
    List<Notification> notifications = new ArrayList<Notification>();
    GlobList messages =
      repository
        .getAll(StandardMessage.TYPE, isFalse(StandardMessage.CLEARED))
        .sortSelf(new GlobFieldsComparator(StandardMessage.DATE, false,
                                           StandardMessage.MESSAGE, true));
    for (Glob message : messages) {
      notifications.add(new StandardMessageNotification(message, repository));
    }
    return notifications;
  }

  public static void notify(String message, GlobRepository repository) {
    repository.create(StandardMessage.TYPE,
                      value(StandardMessage.MESSAGE, message),
                      FieldValue.value(StandardMessage.DATE, TimeService.getCurrentDate()));
  }
}
