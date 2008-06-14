package org.globsframework.rooms.web;

import org.apache.wicket.Request;
import org.apache.wicket.Response;
import org.apache.wicket.Session;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.security.hive.HiveMind;
import org.apache.wicket.security.hive.config.PolicyFileHiveFactory;
import org.apache.wicket.security.swarm.SwarmWebApplication;
import org.globsframework.rooms.web.login.ActionFactory;
import org.globsframework.rooms.web.pages.LoginPage;
import org.globsframework.rooms.web.pages.RoomsPage;

import java.net.MalformedURLException;

public class RoomsApplication extends SwarmWebApplication {

  public RoomsApplication() {
  }

  protected void init() {
    super.init();
    this.mountBookmarkablePage("/login", LoginPage.class);
    getResourceSettings().setAddLastModifiedTimeToResourceReferenceUrl(true);
  }

  protected void setUpHive() {
    PolicyFileHiveFactory factory = new PolicyFileHiveFactory();
    try {
      factory.addPolicyFile(getServletContext().getResource("/login/login.hive"));
    }
    catch (MalformedURLException e) {
      throw new WicketRuntimeException(e);
    }
    HiveMind.registerHive(getHiveKey(), factory);
  }

  protected Object getHiveKey() {
    return getServletContext().getContextPath();
  }

  public Class getHomePage() {
    return RoomsPage.class;
  }

  public Class getLoginPage() {
    return LoginPage.class;
  }

  protected void setupActionFactory() {
    setActionFactory(new ActionFactory());
  }

  public Session newSession(Request request, Response response) {
    return new RoomSession(this, request);
  }

}
