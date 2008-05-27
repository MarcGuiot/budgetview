package org.crossbowlabs.globs.wicket.component;

import org.crossbowlabs.globs.utils.Strings;
import wicket.ajax.AjaxRequestTarget;
import wicket.ajax.markup.html.AjaxLink;
import wicket.markup.ComponentTag;
import wicket.markup.MarkupStream;
import wicket.markup.html.basic.Label;
import wicket.markup.html.panel.Panel;
import wicket.model.Model;

public class CollapsibleTextPanel extends Panel {

  private static final String COLLAPSED_LINK = "[...]";
  private static final String EXPANDED_LINK = "[<<<]";

  public CollapsibleTextPanel(String id, String text, int size) {
    super(id);
    String expandedText = text;
    String collapsedText = getCollapsedText(text, size);

    final Label labelText = new Label("labelText", new Model(collapsedText)) {
      protected void onComponentTagBody(final MarkupStream markupStream, final ComponentTag openTag) {
        final CharSequence body = getModelObjectAsString().replaceAll("\n", "<br/>");
        replaceComponentTagBody(markupStream, openTag, body);
      }
    };
    labelText.setOutputMarkupId(true);
    add(labelText);

    final Label linkText = new Label("linkText", new Model(COLLAPSED_LINK));
    linkText.setOutputMarkupId(true);

    AjaxLink link = new CollapseLink("link", expandedText, collapsedText, labelText, linkText);
    link.add(linkText);
    add(link);
  }

  private String getCollapsedText(String text, int size) {
    int newLineIndex = text.indexOf("\n");
    if ((newLineIndex >= 0) && (newLineIndex < size)) {
      return text.substring(0, newLineIndex);
    }
    int lineSeparatorIndex = text.indexOf(Strings.LINE_SEPARATOR);
    if ((lineSeparatorIndex >= 0) && (lineSeparatorIndex < size)) {
      return text.substring(0, lineSeparatorIndex);
    }
    String firstPart = text.substring(0, size);
    return firstPart.replaceAll("\n", " ");
  }

  private class CollapseLink extends AjaxLink {
    private boolean collapsed = true;
    private String expandedText;
    private String collapsedText;
    private final Label labelText;
    private final Label linkText;

    public CollapseLink(String id,
                        String expandedText,
                        String collapsedText,
                        Label labelText,
                        Label linkText) {
      super(id);
      this.labelText = labelText;
      this.linkText = linkText;
      this.expandedText = expandedText;
      this.collapsedText = collapsedText;
    }

    public void onClick(final AjaxRequestTarget target) {
      collapsed = !collapsed;

      labelText.setModelObject(collapsed ? collapsedText : expandedText);
      linkText.setModelObject(collapsed ? COLLAPSED_LINK : EXPANDED_LINK);

      if (target != null) {
        target.addComponent(labelText);
        target.addComponent(linkText);
      }
    }
  }
}
