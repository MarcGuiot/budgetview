package org.designup.picsou.gui.config;

import org.designup.picsou.model.User;
import org.designup.picsou.model.UserPreferences;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.utils.directory.Directory;

import java.util.List;

public class RegistrationTrigger implements ChangeSetListener {
  private final Directory directory;

  public RegistrationTrigger(Directory directory) {
    this.directory = directory;
  }

  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {
    if (!changeSet.containsChanges(User.TYPE)) {
      return;
    }
    changeSet.safeVisit(User.TYPE, new ChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
        if (values.contains(User.ACTIVATION_CODE)) {
          Glob user = repository.get(User.KEY);
          String mail = user.get(User.MAIL);
          String code = values.get(User.ACTIVATION_CODE);
          directory.get(ConfigService.class).sendRegister(mail, code, repository);
        }
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
      }
    });

    changeSet.safeVisit(UserPreferences.key, new ChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
        if (values.contains(UserPreferences.FUTURE_MONTH_COUNT)) {
          ConfigService.check(directory, repository);
        }
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
      }
    });
  }

  public void globsReset(GlobRepository repository, List<GlobType> changedTypes) {
  }
}
