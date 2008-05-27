package org.crossbowlabs.rooms.web.login;

import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.security.actions.RegistrationException;
import org.apache.wicket.security.swarm.actions.SwarmActionFactory;

public class ActionFactory extends SwarmActionFactory {
  public ActionFactory() {
    try {
      register(Admin.class, "admin");
    } catch (RegistrationException e) {
      throw new WicketRuntimeException("fail to register admin action", e);
    }
  }
}
