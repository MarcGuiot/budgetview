package org.crossbowlabs.webdemo.pages;
import wicket.Component;
import wicket.ajax.AjaxRequestTarget;
import wicket.ajax.markup.html.AjaxFallbackLink;
import wicket.markup.html.basic.Label;
import wicket.markup.html.panel.Panel;

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
