package com.budgetview.session.states;

import org.globsframework.utils.serialization.SerializedInput;

public interface CreatingUserState extends SessionState {

  void createUser(SerializedInput input);

  Boolean getIsRegisteredUser();
}
