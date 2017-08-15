package com.budgetview.triggers;

import com.budgetview.client.DataAccess;
import com.budgetview.desktop.Application;
import com.budgetview.desktop.utils.KeyService;
import com.budgetview.model.LicenseActivationState;
import com.budgetview.model.User;
import org.globsframework.model.*;

import static org.globsframework.model.FieldValue.value;

public class LicenseRegistrationTrigger extends AbstractChangeSetListener {
  private DataAccess dataAccess;

  public LicenseRegistrationTrigger(DataAccess dataAccess) {
    this.dataAccess = dataAccess;
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
          if (values.contains(User.EMAIL)) {
            mail = values.get(User.EMAIL);
          }
          if (values.contains(User.ACTIVATION_CODE)) {
            activationCode = values.get(User.ACTIVATION_CODE);
          }
          if (mail != null || signature != null || activationCode != null) {
            Glob user = repository.get(User.KEY);
            mail = user.get(User.EMAIL);
            signature = user.get(User.SIGNATURE);
            activationCode = user.get(User.ACTIVATION_CODE);
            if (mail != null && signature != null && activationCode != null) {
              byte[] mailAsByte = mail.getBytes();
              if (KeyService.checkSignature(mailAsByte, signature)) {
                dataAccess.localRegister(mailAsByte, signature, activationCode, Application.JAR_VERSION);
                repository.update(User.KEY,
                                  value(User.LICENSE_ACTIVATION_STATE, LicenseActivationState.ACTIVATION_OK.getId()),
                                  value(User.IS_REGISTERED_USER, true));
              }
              else {
                repository.update(User.KEY, User.LICENSE_ACTIVATION_STATE, LicenseActivationState.ACTIVATION_FAILED_BAD_SIGNATURE.getId());
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
            dataAccess.localRegister(null, null, null, -1);
          }
        }

        public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
        }
      });
    }
  }
}
