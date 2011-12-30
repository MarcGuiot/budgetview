package com.budgetview.analytics.functors;

import com.budgetview.analytics.model.LogEntry;
import com.budgetview.analytics.model.LogEntryType;
import com.budgetview.analytics.model.User;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobFunctor;
import org.globsframework.utils.Strings;
import org.globsframework.utils.exceptions.InvalidParameter;
import org.globsframework.utils.exceptions.InvalidState;

import java.util.Date;

public class UserEntriesFunctor implements GlobFunctor {

  public void run(Glob entry, GlobRepository repository) throws Exception {
    Glob user = repository.findLinkTarget(entry, LogEntry.USER);
    if (user == null) {
      throw new InvalidParameter("Entry without user: " + entry);
    }

    String email = entry.get(LogEntry.EMAIL);
    if (Strings.isNotEmpty(email)) {
      repository.update(user.getKey(), User.EMAIL, email);
    }
    
    Date date = entry.get(LogEntry.DATE);
    LogEntryType entryType = LogEntryType.get(entry.get(LogEntry.ENTRY_TYPE));
    switch (entryType) {
      case NEW_USER:
      case KNOWN_USER:
      case LICENCE_CHECK:
      case DIFFERENT_CODE:
        updateDates(user, date, email, repository);
        break;
      case PURCHASE:
        if (user.get(User.PURCHASE_DATE) != null) {
          throw new InvalidState("Purchase date already exists for " + user);
        }
        repository.update(user.getKey(), User.PURCHASE_DATE, date);
        updateDates(user, date, email, repository);
        break;
      default:
    }

    switch (entryType) {
      case NEW_USER:
      case KNOWN_USER:
      case LICENCE_CHECK:
        Integer pingCount = user.get(User.PING_COUNT);
        repository.update(user.getKey(), User.PING_COUNT, pingCount != null ? pingCount + 1 : 1);
      default:
    }
  }

  private void updateDates(Glob user, Date date, String email, GlobRepository repository) {
    Date firstDate = user.get(User.FIRST_DATE);
    if ((firstDate == null) || firstDate.after(date)) {
      repository.update(user.getKey(), User.FIRST_DATE, date);
    }

    Date lastDate = user.get(User.LAST_DATE);
    if ((lastDate == null) || lastDate.before(date)) {
      repository.update(user.getKey(), User.LAST_DATE, date);
    }
  }
}
