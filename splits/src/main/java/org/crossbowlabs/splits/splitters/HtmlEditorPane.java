package org.crossbowlabs.splits.splitters;

import org.crossbowlabs.splits.SplitProperties;
import org.crossbowlabs.splits.SplitsContext;
import org.crossbowlabs.splits.Splitter;
import org.crossbowlabs.splits.utils.GuiUtils;

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
