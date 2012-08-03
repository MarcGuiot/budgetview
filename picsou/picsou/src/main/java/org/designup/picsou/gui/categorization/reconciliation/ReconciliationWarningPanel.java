package org.designup.picsou.gui.categorization.reconciliation;

import org.designup.picsou.gui.categorization.CategorizationView;
import org.designup.picsou.gui.categorization.components.CategorizationFilteringMode;
import org.designup.picsou.gui.utils.Matchers;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.model.UserPreferences;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.TypeChangeSetListener;
import org.globsframework.utils.Utils;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ReconciliationWarningPanel {

  private CategorizationView categorizationView;
  private GlobRepository repository;
  private Directory directory;
  private JPanel panel;
  private JLabel message;

  public ReconciliationWarningPanel(CategorizationView categorizationView, GlobRepository repository, Directory directory) {
    this.categorizationView = categorizationView;
    this.repository = repository;
    this.directory = directory;
    repository.addChangeListener(new TypeChangeSetListener(Transaction.TYPE, UserPreferences.TYPE) {
      protected void update(GlobRepository repository) {
        doUpdate();
      }
    });
  }

  private void createPanel() {
    GlobsPanelBuilder builder =
      new GlobsPanelBuilder(getClass(), "/layout/categorization/reconciliationWarningPanel.splits",
                            repository, directory);

    message = new JLabel();
    builder.add("message", message);
    builder.add("activateReconciliationFilter", new ActivateReconciliationFilterAction());

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

  private class ActivateReconciliationFilterAction extends AbstractAction {

    private ActivateReconciliationFilterAction() {
      super(Lang.get("reconciliation.warning.filter"));
    }

    public void actionPerformed(ActionEvent actionEvent) {
      categorizationView.setFilteringMode(CategorizationFilteringMode.TO_RECONCILE);
    }
  }
}
