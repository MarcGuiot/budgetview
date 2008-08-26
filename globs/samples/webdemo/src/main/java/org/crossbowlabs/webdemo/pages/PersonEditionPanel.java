package org.crossbowlabs.webdemo.pages;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;


public class PersonEditionPanel extends Panel {
  public PersonEditionPanel(String editorId, final Component tr) {
    super(editorId);
    add(new Label("details", "pipotron"));
    add(new AjaxFallbackLink("close") {
      public void onClick(final AjaxRequestTarget target) {
        PersonEditionPanel.this.setVisible(false);
        target.addComponent(tr);
      }
    });
  }
}
