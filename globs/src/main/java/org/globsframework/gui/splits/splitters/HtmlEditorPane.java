package org.globsframework.gui.splits.splitters;

import org.globsframework.gui.splits.SplitProperties;
import org.globsframework.gui.splits.SplitsContext;
import org.globsframework.gui.splits.Splitter;
import org.globsframework.gui.splits.utils.GuiUtils;

import javax.swing.*;
import javax.swing.event.HyperlinkListener;

/**
 * Manages a JEditorPane configured to display HTML with a standard font and opacity disabled.
 */
public class HtmlEditorPane extends DefaultComponent<JEditorPane> {
  private String listenerName;

  public HtmlEditorPane(SplitProperties properties,
                        Splitter[] subSplitters,
                        boolean acceptsSubSplitters) {
    super(JEditorPane.class, "htmlEditorPane", properties, subSplitters, acceptsSubSplitters);

    listenerName = properties.get("hyperlinkListener");
  }

  protected String[] getExcludedParameters() {
    return new String[]{"hyperlinkListener"};
  }

  protected void postCreateComponent(JEditorPane component, SplitsContext context) {
    GuiUtils.initReadOnlyHtmlComponent(component);
    if (listenerName != null) {
      HyperlinkListener hyperlinkListener = context.getHyperlinkListener(listenerName);
      component.addHyperlinkListener(hyperlinkListener);
    }
  }
}
