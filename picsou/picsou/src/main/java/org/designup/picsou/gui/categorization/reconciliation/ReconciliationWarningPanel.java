package org.designup.picsou.gui.categorization.reconciliation;

import org.designup.picsou.gui.categorization.CategorizationView;
import org.designup.picsou.gui.categorization.components.CategorizationFilteringMode;
import org.designup.picsou.gui.help.HyperlinkHandler;
import org.designup.picsou.gui.utils.Matchers;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.model.UserPreferences;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.TypeChangeSetListener;
import org.globsframework.utils.Utils;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public class ReconciliationWarningPanel {

  private CategorizationView categorizationView;
  private GlobRepository repository;
  private Directory directory;
  private JPanel panel;
  private JEditorPane message;

  public ReconciliationWarningPanel(CategorizationView categorizationView, GlobRepository repository, Directory directory) {
    this.categorizationView = categorizationView;
    this.repository = repository;
    this.directory = directory;
    repository.addChangeListener(new TypeChangeSetListener(Transaction.TYPE, UserPreferences.TYPE) {
      public void update(GlobRepository repository) {
        doUpdate();
      }
    });
  }

  private void createPanel() {
    GlobsPanelBuilder builder =
      new GlobsPanelBuilder(getClass(), "/layout/categorization/reconciliationWarningPanel.splits",
                            repository, directory);

    HyperlinkHandler handler = new HyperlinkHandler(directory);
    builder.add("handler", handler);
    handler.registerLinkAction("filter", new Runnable() {
      public void run() {
        categorizationView.setFilteringMode(CategorizationFilteringMode.TO_RECONCILE);
      }
    });
    message = GuiUtils.createReadOnlyHtmlComponent();
    builder.add("message", message);

    panel = builder.load();

    doUpdate();
  }

  public JPanel getPanel() {
    if (panel == null) {
      createPanel();
    }
    return panel;
  }

  private void doUpdate() {
    if (panel == null) {
      return;
    }

    Glob prefs = repository.find(UserPreferences.KEY);
    if ((prefs != null) &&
        Utils.equal(CategorizationFilteringMode.TO_RECONCILE.getId(),
                    prefs.get(UserPreferences.CATEGORIZATION_FILTERING_MODE))) {
      panel.setVisible(false);
      return;
    }

    if (repository.contains(Transaction.TYPE, Matchers.transactionsToReconcile())) {
      panel.setVisible(true);
      GlobList transactions =
        repository.getAll(Transaction.TYPE, Matchers.transactionsToReconcile());
      message.setText(transactions.size() == 1 ?
                      Lang.get("reconciliation.warning.single") :
                      Lang.get("reconciliation.warning.multi", transactions.size()));
    }
    else {
      panel.setVisible(false);
    }
  }
}
