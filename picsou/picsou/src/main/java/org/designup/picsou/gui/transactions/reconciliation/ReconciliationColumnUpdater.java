package org.designup.picsou.gui.transactions.reconciliation;

import org.designup.picsou.gui.categorization.CategorizationView;
import org.designup.picsou.gui.utils.Gui;
import org.designup.picsou.model.UserPreferences;
import org.globsframework.gui.views.GlobTableView;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.TypeChangeSetListener;
import org.globsframework.utils.directory.Directory;

public class ReconciliationColumnUpdater {
  private GlobTableView tableView;
  private GlobRepository repository;
  private ReconciliationColumn column;

  public static final int[] COLUMN_SIZES = {5, 10, 12, 24, 10};

  private boolean isShowing = false;

  public static void install(GlobTableView tableView, GlobRepository repository, Directory directory) {
    new ReconciliationColumnUpdater(tableView, repository, directory);
  }

  private ReconciliationColumnUpdater(GlobTableView tableView, GlobRepository repository, Directory directory) {
    this.tableView = tableView;
    this.repository = repository;
    this.column = new ReconciliationColumn(tableView, repository, directory);
    repository.addChangeListener(new TypeChangeSetListener(UserPreferences.TYPE) {
      protected void update(GlobRepository repository) {
        toggleColumn();
      }
    });
    toggleColumn();
  }

  private void toggleColumn() {
    Glob preferences = repository.find(UserPreferences.KEY);
    if (preferences == null) {
      return;
    }
    boolean show = preferences.isTrue(UserPreferences.SHOW_RECONCILIATION);
    if (show && !isShowing) {
      tableView.insertColumn(0, column);
      Gui.setColumnSizes(tableView.getComponent(), COLUMN_SIZES);
      isShowing = true;
    }
    else if (!show && isShowing) {
      tableView.removeColumn(0);
      Gui.setColumnSizes(tableView.getComponent(), CategorizationView.COLUMN_SIZES);
      isShowing = false;
    }
  }
}
