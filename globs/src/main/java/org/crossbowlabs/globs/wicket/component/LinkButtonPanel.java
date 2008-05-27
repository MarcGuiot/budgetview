package org.crossbowlabs.globs.wicket.component;
import wicket.markup.html.link.Link;
import wicket.markup.html.panel.Panel;
import wicket.AttributeModifier;
import wicket.model.Model;

public class LinkButtonPanel extends Panel {
  public static final String ID = "linkButton";

  public LinkButtonPanel(String panelId, String linkText, Link link) {
    super(panelId);
    add(link);
    link.add(new AttributeModifier("value", new Model(linkText)));
  }
}
