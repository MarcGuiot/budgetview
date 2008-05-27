package org.crossbowlabs.rooms.web.pages;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.security.WaspSession;
import org.apache.wicket.security.components.SecureWebPage;
import org.crossbowlabs.rooms.web.RoomSession;
import org.crossbowlabs.rooms.web.login.RoomContext;

import java.util.ResourceBundle;

public class Skin extends SecureWebPage{

  public Skin() {

    add(new Index("indexPanel"));

    ResourceBundle resourceBundle = ResourceBundle.getBundle("translation/rooms");

    Link logoutLink = new Link("logoutLink") {
      public void onClick() {
        ((WaspSession) getSession()).logoff(new RoomContext());
      }
    };
    logoutLink.add(new Label("logout", resourceBundle.getString("logoutLink")));
    add(logoutLink);

    String login = ((RoomSession)this.getSession()).getContext().getUser().getLogin();
    add(new Label("who", resourceBundle.getString("welcome") + ", " + login));
  }
}
