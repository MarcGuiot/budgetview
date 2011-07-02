package org.designup.picsou.gui.config;

import org.designup.picsou.model.User;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;

import java.util.Set;

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
        if (values.contains(User.ACTIVATION_CODE) || values.contains(User.EMAIL)) {
          Glob user = repository.get(User.KEY);
          final String mail = user.get(User.EMAIL);
          final String code = user.get(User.ACTIVATION_CODE);
          if (Strings.isNotEmpty(mail) && Strings.isNotEmpty(code)) {
            Thread thread = new RegistrationThread(mail, code, repository);
            thread.setDaemon(true);
            thread.start();
          }
        }
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
      }
    });
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
  }

  private class RegistrationThread extends Thread {
    private final String mail;
    private final String code;
    private final GlobRepository repository;

    public RegistrationThread(String mail, String code, GlobRepository repository) {
      this.mail = mail;
      this.code = code;
      this.repository = repository;
    }

    public void run() {
      directory.get(ConfigService.class).sendRegister(mail, code, repository);
    }
  }
}
