package org.globsframework.gui.splits.splitters;

import org.globsframework.gui.splits.SplitProperties;
import org.globsframework.gui.splits.SplitsContext;
import org.globsframework.gui.splits.Splitter;
import org.globsframework.gui.splits.utils.GuiUtils;

import javax.swing.*;

/**
 * Manages a JEditorPane configured to display HTML with a standard font and opacity disabled.
 */
public class HtmlEditorPane extends DefaultComponent<JEditorPane> {
  public HtmlEditorPane(SplitsContext context,
                        SplitProperties properties,
                        Splitter[] subSplitters,
                        boolean acceptsSubSplitters) {
    super(JEditorPane.class, "htmlEditorPane", context, properties, subSplitters, acceptsSubSplitters);
    component.setContentType("text/html");
    component.setEditable(false);
    component.setOpaque(false);
    GuiUtils.initHtmlComponent(component);
  }
}
