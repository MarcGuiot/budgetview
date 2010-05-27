package org.designup.picsou.gui.card.widgets;

import org.designup.picsou.gui.actions.ImportFileAction;
import org.designup.picsou.gui.signpost.Signpost;
import org.designup.picsou.gui.signpost.guides.ImportSignpost;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.SplitsNode;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.TypeChangeSetListener;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public class ImportWidget extends AbstractNavigationWidget {

  private JEditorPane editor = GuiUtils.createReadOnlyHtmlComponent();
  private JButton importButton;
  private SplitsNode<JEditorPane> editorNode;
  private JPanel panel;

  public ImportWidget(GlobRepository repository, Directory directory) {
    super("importFile",
          Lang.get("importWidget.title"),
          "home/folder.png",
          "home/folder_rollover.png",
          ImportFileAction.init(Lang.get("importWidget.title"), repository, directory, null),
          repository, directory);

    importButton = new JButton(action);
    importButton.setText(Lang.get("importWidget.button"));

    createPanel();

    repository.addChangeListener(new TypeChangeSetListener(Transaction.TYPE) {
      protected void update(GlobRepository repository) {
        doUpdate();
      }
    });

    doUpdate();
  }

  private void createPanel() {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/home/importWidget.splits",
                                                      repository, directory);
    editorNode = builder.add("editor", editor);
    builder.add("importButton", importButton);
    panel = builder.load();
  }

  public boolean isNavigation() {
    return false;
  }

  public JComponent getComponent() {
    return panel;
  }

  public Signpost getSignpost() {
    return new ImportSignpost(repository, directory);
  }

  private void doUpdate() {
    boolean hasTransactions = repository.contains(Transaction.TYPE);
    editor.setText(Lang.get(hasTransactions ? "importWidget.text" : "importWidget.initial"));
    editorNode.applyStyle(hasTransactions ? "normalText" : "highlightedText");
    importButton.setVisible(!hasTransactions);
    GuiUtils.revalidate(importButton);
  }
}
