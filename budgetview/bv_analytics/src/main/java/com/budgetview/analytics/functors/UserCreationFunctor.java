package com.budgetview.analytics.functors;

import com.budgetview.analytics.model.LogEntry;
import com.budgetview.analytics.model.User;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.utils.GlobFunctor;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.Strings;

import java.util.HashMap;
import java.util.Map;

public class UserCreationFunctor implements GlobFunctor {

  private Map<String, Integer> userIdByRepoId = new HashMap<String, Integer>();
  private Map<String, Integer> userIdByEmail = new HashMap<String, Integer>();

  public void run(Glob entry, GlobRepository repository) throws Exception {
    String repoId = entry.get(LogEntry.REPO_ID);
    String email = entry.get(LogEntry.EMAIL);

    Integer userIdFromEmail = userIdByEmail.get(email);
    Integer userIdFromRepo = userIdByRepoId.get(repoId);

    if ((userIdFromEmail != null) && (userIdFromRepo != null) && !userIdFromEmail.equals(userIdFromRepo)) {
      GlobList previousEntriesWithRepo = 
        repository.getAll(LogEntry.TYPE, GlobMatchers.fieldEquals(LogEntry.USER, userIdFromRepo));
      for (Glob previousEntry : previousEntriesWithRepo) {
        repository.update(previousEntry.getKey(), LogEntry.USER, userIdFromEmail);
      }
      repository.delete(Key.create(User.TYPE, userIdFromRepo));
      repository.update(Key.create(User.TYPE, userIdFromEmail), User.EMAIL, email);
    }
    
    Integer userId = userIdFromEmail != null ? userIdFromEmail : userIdFromRepo;
    if (userId == null) {
      Glob user = repository.create(User.TYPE);
      userId = user.get(User.ID);
    }

    repository.update(entry.getKey(), LogEntry.USER, userId);
    if (Strings.isNotEmpty(repoId)) {
      userIdByRepoId.put(repoId, userId);
    }
    if (Strings.isNotEmpty(email)) {
      userIdByEmail.put(email, userId);
    }
  }
}
