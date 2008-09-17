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
  public HtmlEditorPane(SplitProperties properties,
                        Splitter[] subSplitters,
                        boolean acceptsSubSplitters) {
    super(JEditorPane.class, "htmlEditorPane", properties, subSplitters, acceptsSubSplitters);
  }

  protected void postCreateComponent(JEditorPane component, SplitsContext context) {
    GuiUtils.initReadOnlyHtmlComponent(component);
  }
}
