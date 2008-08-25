package org.crossbowlabs.rooms.web;

import org.apache.wicket.security.WaspSession;
import org.apache.wicket.security.WaspApplication;
import org.apache.wicket.security.strategies.LoginException;
import org.apache.wicket.Request;
import org.crossbowlabs.rooms.web.login.RoomContext;

public class RoomSession extends WaspSession {
  private RoomContext context;

  public RoomSession(WaspApplication application, Request request) {
    super(application, request);
  }

  public void login(Object context) throws LoginException {
    super.login(context);
    this.context = (RoomContext) context;
  }

  public boolean logoff(Object context) {
    this.context = (RoomContext) context;
    return super.logoff(context);
  }

  public RoomContext getContext() {
    return context;
  }
}
