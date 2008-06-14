package org.globsframework.wicket.component;

import wicket.markup.html.WebMarkupContainer;
import wicket.markup.html.basic.Label;
import wicket.markup.html.panel.Panel;

/**
 * Container for links with markup <a>, for instance {@link wicket.markup.html.link.Link}, {@link
 * wicket.ajax.markup.html.AjaxLink} or {@link wicket.markup.html.link.ExternalLink}
 */
public class LinkPanel extends Panel {
  public static final String ID = "link";

  public LinkPanel(String panelId, String linkText, WebMarkupContainer link) {
    super(panelId);
    add(link);
    link.add(new Label("label", linkText));
  }
}
