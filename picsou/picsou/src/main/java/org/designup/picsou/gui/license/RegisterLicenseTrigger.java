package org.designup.picsou.gui.license;

import org.designup.picsou.client.ServerAccess;
import org.designup.picsou.gui.PicsouApplication;
import org.designup.picsou.gui.utils.KeyService;
import org.designup.picsou.model.User;
import org.designup.picsou.model.UserVersionInformation;
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
                serverAccess.localRegister(mailAsByte, signature, activationCode, PicsouApplication.JAR_VERSION);
                repository.update(User.KEY, User.ACTIVATION_STATE, User.ACTIVATION_OK);
                repository.update(User.KEY, User.IS_REGISTERED_USER, true);
              }
              else {
                repository.update(User.KEY, User.ACTIVATION_STATE, User.ACTIVATION_FAILED_BAD_SIGNATURE);
              }
            }
          }
        }

        public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
        }
      });
    }
    if (changeSet.containsChanges(User.KEY)) {
      changeSet.safeVisit(User.KEY, new ChangeSetVisitor() {
        public void visitCreation(Key key, FieldValues values) throws Exception {
        }

        public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
          if (values.contains(User.IS_REGISTERED_USER) && !values.isTrue(User.IS_REGISTERED_USER)) {
            serverAccess.localRegister(null, null, null, -1);
          }
        }

        public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
        }
      });
    }
    if (changeSet.containsChanges(UserVersionInformation.KEY)){
      changeSet.safeVisit(UserVersionInformation.KEY, new ChangeSetVisitor() {
        public void visitCreation(Key key, FieldValues values) throws Exception {
        }

        public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
          if (values.contains(UserVersionInformation.CURRENT_JAR_VERSION)){
            serverAccess.downloadedVersion(values.get(UserVersionInformation.CURRENT_JAR_VERSION));
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