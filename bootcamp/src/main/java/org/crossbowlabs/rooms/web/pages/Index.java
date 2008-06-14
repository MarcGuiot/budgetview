package org.globsframework.rooms.web.pages;

import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.security.components.markup.html.links.SecurePageLink;

public class Index extends Panel {
  public Index(String id) {
    super(id);
    add(new SecurePageLink("MainPage", RoomsPage.class));
    add(new SecurePageLink("UsersPage", UserAdminPage.class));
    add(new SecurePageLink("RoomsPage", RoomsAdminPage.class));
    add(new SecurePageLink("BarcosPage", BarcoAdminPage.class));
  }
}
