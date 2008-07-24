package org.designup.picsou.gui;

import org.designup.picsou.client.ServerAccess;
import org.designup.picsou.gui.utils.KeyChecker;
import org.designup.picsou.model.User;
import org.designup.picsou.model.UserPreferences;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.remote.SerializedRemoteAccess;

import java.util.List;

public class RegisterLicenseTrigger implements ChangeSetListener {
  private ServerAccess serverAccess;

  public RegisterLicenseTrigger(ServerAccess serverAccess) {
    this.serverAccess = serverAccess;
  }

  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {
    if (changeSet.containsChanges(User.TYPE)) {
      changeSet.safeVisit(User.TYPE, new SerializedRemoteAccess.ChangeVisitor() {
        public void complete() {
        }

        public void visitCreation(Key key, FieldValues values) throws Exception {
        }

        public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
          byte[] signature = null;
          String mail = null;
          if (values.contains(User.SIGNATURE)) {
            signature = values.get(User.SIGNATURE);
          }
          if (values.contains(User.MAIL)) {
            mail = values.get(User.MAIL);
          }
          if (mail != null || signature != null) {
            Glob user = repository.get(User.KEY);
            if (mail == null) {
              mail = user.get(User.MAIL);
            }
            if (signature == null) {
              signature = user.get(User.SIGNATURE);
            }
            byte[] mailAsByte = mail.getBytes();
            if (KeyChecker.checkSignature(mailAsByte, signature)) {
              serverAccess.register(mailAsByte, signature);
              repository.update(UserPreferences.key, UserPreferences.FUTURE_MONTH_COUNT, 24);
            }
          }
        }

        public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
        }
      });
    }
  }

  public void globsReset(GlobRepository repository, List<GlobType> changedTypes) {
  }
}
