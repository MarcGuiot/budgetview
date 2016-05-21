package com.budgetview.gui.notifications;

import org.globsframework.metamodel.GlobType;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import java.util.List;

public interface NotificationHandler {

  GlobType getType();

  int getNotificationCount(GlobRepository repository);

  List<Notification> getNotifications(GlobRepository repository, Directory directory);
}
