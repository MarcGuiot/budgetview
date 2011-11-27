package org.designup.picsou.gui.importer;

import org.designup.picsou.gui.components.dialogs.PicsouDialog;
import org.designup.picsou.gui.help.HyperlinkHandler;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.model.repository.LocalGlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Set;

public class ImportCompletionPanel {
  private ImportController controller;
  private LocalGlobRepository localRepository;
  private Directory directory;

  private GlobsPanelBuilder builder;
  private JPanel panel;
  private JEditorPane message;
  private Set<Integer> months;

  public ImportCompletionPanel(ImportController controller,
                               LocalGlobRepository localRepository,
                               Directory directory) {
    this.controller = controller;
    this.localRepository = localRepository;
    this.directory = directory;
  }

  public void init(final PicsouDialog dialog, String textForCloseButton) {

    builder = new GlobsPanelBuilder(getClass(), "/layout/importexport/importCompletionPanel.splits", localRepository, directory);

    message = GuiUtils.createReadOnlyHtmlComponent();

    builder.add("message", message);
    builder.add("ok", new CommitAction());
    builder.add("close", new AbstractAction(textForCloseButton) {
      public void actionPerformed(ActionEvent e) {
        controller.complete();
        controller.closeDialog();
      }
    });

    final HyperlinkHandler hyperlinkHandler = new HyperlinkHandler(directory, dialog);
    builder.add("hyperlinkHandler", hyperlinkHandler);

    panel = builder.load();
  }

  public void dispose() {
    builder.dispose();
  }

  public void update(Set<Integer> months, int importedTransactionCount, int autocategorizedTransaction, int transactionCount) {
    this.months = months;
    String content =
      Lang.get(getEndOfImportMessageKey(importedTransactionCount, transactionCount, autocategorizedTransaction),
               Integer.toString(transactionCount),
               Integer.toString(autocategorizedTransaction),
               Integer.toString(importedTransactionCount));
    message.setText(content);
    GuiUtils.revalidate(message);
  }

  public static String getEndOfImportMessageKey(int importedTransactionCount, int loadedTransactionCount, int autocategorizedTransactions) {
    if (loadedTransactionCount == 0) {
      if (autocategorizedTransactions > 0){
        return "import.end.info.operations.none.none." + normalize(importedTransactionCount);
      }
      else {
        return "import.end.info.operations.none.none." + normalize(importedTransactionCount);
      }
    }
    else {
      if ((loadedTransactionCount > 1) && (loadedTransactionCount == autocategorizedTransactions)) {
        return "import.end.info.operations.many.all";
      }
      return "import.end.info.operations." + normalize(loadedTransactionCount) + "." + normalize(autocategorizedTransactions);
    }
  }

  private static String normalize(int count) {
    if (count == 0) {
      return "none";
    }
    if (count == 1) {
      return "one";
    }
    return "many";
  }

  public JPanel getPanel() {
    return panel;
  }

  private class CommitAction extends AbstractAction {
    private CommitAction() {
      super(Lang.get("import.end.button"));
    }

    public void actionPerformed(ActionEvent actionEvent) {
      controller.commitAndClose(months);
    }
  }
}
