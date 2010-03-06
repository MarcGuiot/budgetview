package org.designup.picsou.gui.components.wizard;

import org.designup.picsou.gui.help.HelpDialog;
import org.designup.picsou.gui.help.HyperlinkHandler;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public class HelpWizardPage extends AbstractWizardPage {

  private String title;
  private String htmlContent;
  private HyperlinkHandler hyperlinkHandler;
  private String id;
  private GlobRepository repository;
  private Directory directory;
  private JEditorPane editor;
  private JComponent panel;

  public HelpWizardPage(String id, String titleKey, String helpFilePath, HyperlinkHandler hyperlinkHandler,
                        GlobRepository repository, Directory directory) {
    this.id = id;
    this.repository = repository;
    this.directory = directory;
    this.title = Lang.get(titleKey);
    this.htmlContent = Lang.getHelpFile(helpFilePath);
    this.hyperlinkHandler = hyperlinkHandler;
  }

  public String getId() {
    return id;
  }

  public String getTitle() {
    return title;
  }

  public JComponent getPanel() {
    return panel;
  }

  public void init() {
    GlobsPanelBuilder builder =
      new GlobsPanelBuilder(getClass(), "/layout/helpWizardPage.splits", repository, directory);

    builder.add("handler", hyperlinkHandler);

    editor = new JEditorPane();
    GuiUtils.initReadOnlyHtmlComponent(editor);
    GuiUtils.loadCssResource("/help/help.css", editor, HelpDialog.class);
    builder.add("editor", editor);

    panel = builder.load();
  }

  public void updateBeforeDisplay() {
    editor.setText(htmlContent);
    editor.setCaretPosition(0);
  }
}
