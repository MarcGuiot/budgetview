package org.globsframework.model.utils;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.Link;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.utils.exceptions.InvalidReference;

public class GlobRepositoryValidator {
  public static void run(GlobRepository repository) {
    for (Glob glob : repository.getAll()) {
      for (Link link : glob.getType().getOutboundLinks()) {

        GlobType targetType = link.getTargetType();

        Key targetKey = glob.getTargetKey(link);

        if ((targetKey != null) && !repository.contains(targetKey)) {
          System.out.println("Link " + glob.getKey() + "." + link.getName() + " refers to an object " + targetKey + " that does not exist");
        }
      }
    }
  }
}
