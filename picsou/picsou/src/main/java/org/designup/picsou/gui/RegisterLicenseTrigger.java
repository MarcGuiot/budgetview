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
            if (mail == null) {
              mail = user.get(User.MAIL);
            }
            if (signature == null) {
              signature = user.get(User.SIGNATURE);
            }
            if (activationCode == null) {
              activationCode = user.get(User.ACTIVATION_CODE);
            }
            byte[] mailAsByte = mail.getBytes();
            if (KeyChecker.checkSignature(mailAsByte, signature)) {
              serverAccess.register(mailAsByte, signature, activationCode);
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
