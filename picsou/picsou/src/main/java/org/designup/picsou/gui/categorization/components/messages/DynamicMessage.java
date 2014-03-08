package org.designup.picsou.gui.categorization.components.messages;

import org.designup.picsou.gui.help.HyperlinkHandler;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public abstract class DynamicMessage {
  private JEditorPane htmlEditor;
  protected final GlobRepository repository;
  protected final Directory directory;

  public DynamicMessage(GlobRepository repository, Directory directory) {
    this.htmlEditor = GuiUtils.createReadOnlyHtmlComponent();
    this.repository = repository;
    this.directory = directory;

    htmlEditor.addHyperlinkListener(new HyperlinkHandler(directory) {
      protected void processCustomLink(String href) {
        DynamicMessage.this.processHyperlinkClick(href);
      }
    });
  }

  public JEditorPane getComponent() {
    return htmlEditor;
  }

  protected void setText(String text) {
    htmlEditor.setText(text);
  }

  protected void processHyperlinkClick(String href) {
  }

  protected void setDefaultText() {
  }

  protected void updateVisibility() {
    htmlEditor.setVisible(isVisible());
    GuiUtils.revalidate(htmlEditor);
  }

  protected abstract boolean isVisible();
}
