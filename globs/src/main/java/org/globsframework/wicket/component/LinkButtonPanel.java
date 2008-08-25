package org.globsframework.wicket.component;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

public class LinkButtonPanel extends Panel {
  public static final String ID = "linkButton";

  public LinkButtonPanel(String panelId, String linkText, Link link) {
    super(panelId);
    add(link);
    link.add(new AttributeModifier("value", new Model(linkText)));
  }
}
