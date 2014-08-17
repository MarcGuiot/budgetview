package org.designup.picsou.triggers;

import org.designup.picsou.model.PremiumEvolutionState;
import org.designup.picsou.model.User;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.utils.DefaultChangeSetVisitor;

import java.util.Set;

public class UserRegistrationTrigger implements ChangeSetListener {
  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {
    if (changeSet.containsChanges(User.KEY)) {
      changeSet.safeVisit(new DefaultChangeSetVisitor() {

        public void visitCreation(Key key, FieldValues values) throws Exception {
          updateState(repository);
        }

        public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
          if (values.contains(User.IS_REGISTERED_USER)) {
            updateState(repository);
          }
        }
      });
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    if (changedTypes.contains(User.TYPE)) {
      updateState(repository);
    }
  }

  private void updateState(GlobRepository repository) {
    Glob user = repository.find(User.KEY);
    if ((user != null) && (user.isTrue(User.IS_REGISTERED_USER))) {
      PremiumEvolutionState state = User.getPremiumEvolutionState(user);
      if (!state.isRegistered()) {
        repository.update(User.KEY, User.PREMIUM_EVOLUTION_STATE, PremiumEvolutionState.REGISTERED.getId());
      }
    }
  }
}
