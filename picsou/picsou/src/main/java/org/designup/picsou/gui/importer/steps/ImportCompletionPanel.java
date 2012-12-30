package org.designup.picsou.gui.importer.steps;

import org.designup.picsou.gui.components.dialogs.PicsouDialog;
import org.designup.picsou.gui.help.HyperlinkHandler;
import org.designup.picsou.gui.importer.ImportController;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.model.repository.LocalGlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Set;

public class ImportCompletionPanel extends AbstractImportStepPanel {
  private LocalGlobRepository localRepository;

  private GlobsPanelBuilder builder;
  private JPanel panel;
  private JEditorPane message;
  private Set<Integer> months;

  public ImportCompletionPanel(PicsouDialog dialog,
                               String textForCloseButton,
                               ImportController controller,
                               LocalGlobRepository localRepository,
                               Directory directory) {
    super(dialog, textForCloseButton, controller, directory);
    this.localRepository = localRepository;
  }

  public void createPanelIfNeeded() {
    if (builder != null) {
      return;
    }

    builder = new GlobsPanelBuilder(getClass(), "/layout/importexport/importCompletionPanel.splits", localRepository, localDirectory);

    message = GuiUtils.createReadOnlyHtmlComponent();

    builder.add("message", message);
    builder.add("ok", new CommitAction());
    builder.add("close", new AbstractAction(textForCloseButton) {
      public void actionPerformed(ActionEvent e) {
        controller.complete();
        controller.closeDialog();
      }
    });

    final HyperlinkHandler hyperlinkHandler = new HyperlinkHandler(localDirectory, dialog);
    builder.add("hyperlinkHandler", hyperlinkHandler);

    panel = builder.load();
  }

  public JPanel getPanel() {
    createPanelIfNeeded();
    return panel;
  }

  public void requestFocus() {
  }

  public void dispose() {
    if (builder != null) {
      builder.dispose();
    }
  }

  public void update(Set<Integer> months, int importedTransactionCount, int autocategorizedTransaction, int transactionCount) {
    createPanelIfNeeded();
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
      if (autocategorizedTransactions > 0) {
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

  private class CommitAction extends AbstractAction {
    private CommitAction() {
      super(Lang.get("import.end.button"));
    }

    public void actionPerformed(ActionEvent actionEvent) {
      controller.commitAndClose(months);
    }
  }
}
