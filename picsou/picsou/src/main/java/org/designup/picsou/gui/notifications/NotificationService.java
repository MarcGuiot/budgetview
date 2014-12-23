package org.designup.picsou.gui.notifications;

import org.globsframework.metamodel.GlobType;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import java.util.ArrayList;
import java.util.List;

public class NotificationService {

  private List<NotificationHandler> handlers = new ArrayList<NotificationHandler>();
  private List<GlobType> types = new ArrayList<GlobType>();

  public void addHandler(NotificationHandler handler) {
    handlers.add(handler);
    types.add(handler.getType());
  }

  public List<GlobType> getTypes() {
    return types;
  }

  public int getNotificationCount(GlobRepository repository) {
    int count = 0;
    for (NotificationHandler handler : handlers) {
      count += handler.getNotificationCount(repository);
    }
    return count;
  }

  public List<Notification> getNotifications(GlobRepository repository, Directory directory) {
    List<Notification> notifications = new ArrayList<Notification>();
    for (NotificationHandler handler : handlers) {
      notifications.addAll(handler.getNotifications(repository, directory));
    }
    return notifications;
  }
}
