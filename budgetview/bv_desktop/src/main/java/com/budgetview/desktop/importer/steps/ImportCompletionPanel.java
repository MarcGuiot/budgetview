package com.budgetview.desktop.importer.steps;

import com.budgetview.desktop.components.dialogs.PicsouDialog;
import com.budgetview.desktop.help.HyperlinkHandler;
import com.budgetview.desktop.importer.ImportController;
import com.budgetview.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.model.repository.LocalGlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Set;

public class ImportCompletionPanel extends AbstractImportStepPanel {
  private LocalGlobRepository localRepository;

  private Block importedBlock = new Block("imported");
  private Block ignoredBlock = new Block("ignored");
  private Block categorizedBlock = new Block("categorized");
  private JEditorPane categorizationHelp;
  private Set<Integer> months;

  public ImportCompletionPanel(PicsouDialog dialog,
                               String textForCloseButton,
                               ImportController controller,
                               LocalGlobRepository localRepository,
                               Directory directory) {
    super(dialog, textForCloseButton, controller, directory);
    this.localRepository = localRepository;
  }

  public GlobsPanelBuilder createPanelBuilder() {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/importexport/importsteps/importCompletionPanel.splits", localRepository, localDirectory);

    importedBlock.register(builder);
    ignoredBlock.register(builder);
    categorizedBlock.register(builder);

    categorizationHelp = GuiUtils.createReadOnlyHtmlComponent(Lang.get("import.completion.categorized.help"));
    builder.add("categorizationHelp", categorizationHelp);

    builder.add("ok", new CommitAction());
    builder.add("close", new AbstractAction(textForCloseButton) {
      public void actionPerformed(ActionEvent e) {
        controller.complete();
        controller.closeDialog();
      }
    });

    final HyperlinkHandler hyperlinkHandler = new HyperlinkHandler(localDirectory);
    builder.add("hyperlinkHandler", hyperlinkHandler);

    return builder;
  }

  public void requestFocus() {
  }

  public void update(Set<Integer> months, int importedTransactionCount, int ignoredTransactionCount, int autocategorizedTransactionCount) {
    createPanelIfNeeded();
    this.months = months;
    importedBlock.update(importedTransactionCount);
    ignoredBlock.update(ignoredTransactionCount);
    categorizedBlock.update(autocategorizedTransactionCount);
    categorizationHelp.setVisible(importedTransactionCount > autocategorizedTransactionCount);
    GuiUtils.revalidate(categorizationHelp);
  }

  private class Block {
    private String prefix;
    private final JLabel countLabel = new JLabel();
    private final JLabel message = new JLabel();

    private Block(String prefix) {
      this.prefix = prefix;
    }

    public void register(GlobsPanelBuilder builder) {
      builder.add(prefix + "Message", message);
      builder.add(prefix + "Count", countLabel);
    }

    public void update(int count) {
      countLabel.setText(Integer.toString(count));
      message.setText(Lang.get("import.completion." + prefix + ".message." + normalize(count)));
    }

    private String normalize(int count) {
      if (count == 0) {
        return "none";
      }
      if (count == 1) {
        return "one";
      }
      return "many";
    }
  }

  private class CommitAction extends AbstractAction {
    private CommitAction() {
      super(Lang.get("import.completion.button"));
    }

    public void actionPerformed(ActionEvent actionEvent) {
      controller.commitAndClose(months);
    }
  }
}
