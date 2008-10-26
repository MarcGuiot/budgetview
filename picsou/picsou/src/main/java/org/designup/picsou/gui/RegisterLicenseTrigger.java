package org.designup.picsou.gui;

import org.designup.picsou.client.ServerAccess;
import org.designup.picsou.gui.utils.KeyService;
import org.designup.picsou.model.User;
import org.designup.picsou.model.UserPreferences;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;

import java.util.Set;

public class RegisterLicenseTrigger implements ChangeSetListener {
  private ServerAccess serverAccess;

  public RegisterLicenseTrigger(ServerAccess serverAccess) {
    this.serverAccess = serverAccess;
  }

  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {
    if (changeSet.containsChanges(User.TYPE)) {
      changeSet.safeVisit(User.TYPE, new ChangeSetVisitor() {
        public void visitCreation(Key key, FieldValues values) throws Exception {
        }

        public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
          byte[] signature = null;
          String mail = null;
          String activationCode = null;
          if (values.contains(User.SIGNATURE)) {
            signature = values.get(User.SIGNATURE);
          }
          if (values.contains(User.MAIL)) {
            mail = values.get(User.MAIL);
          }
          if (values.contains(User.ACTIVATION_CODE)) {
            activationCode = values.get(User.ACTIVATION_CODE);
          }
          if (mail != null || signature != null || activationCode != null) {
            Glob user = repository.get(User.KEY);
            mail = user.get(User.MAIL);
            signature = user.get(User.SIGNATURE);
            activationCode = user.get(User.ACTIVATION_CODE);
            if (mail != null && signature != null && activationCode != null) {
              byte[] mailAsByte = mail.getBytes();
              if (KeyService.checkSignature(mailAsByte, signature)) {
                serverAccess.localRegister(mailAsByte, signature, activationCode);
                repository.update(User.KEY, User.ACTIVATION_STATE, User.ACTIVATION_OK);
                repository.update(UserPreferences.KEY, UserPreferences.REGISTERED_USER, true);
              }
              else {
                repository.update(User.KEY, User.ACTIVATION_STATE, User.ACTIVATION_FAIL_BAD_SIGNATURE);
              }
            }
          }
        }

        public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
        }
      });
    }
    if (changeSet.containsChanges(UserPreferences.KEY)) {
      changeSet.safeVisit(UserPreferences.KEY, new ChangeSetVisitor() {
        public void visitCreation(Key key, FieldValues values) throws Exception {
        }

        public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
          if (values.contains(UserPreferences.REGISTERED_USER) &&
              !values.get(UserPreferences.REGISTERED_USER)) {
            serverAccess.localRegister(null, null, null);
          }
        }

        public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
        }
      });
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
  }
}
