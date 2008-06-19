package org.designup.picsou.gui.transactions;

import org.designup.picsou.gui.View;
import org.designup.picsou.model.Transaction;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.color.ColorLocator;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.ChangeSet;
import org.globsframework.model.ChangeSetListener;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import java.util.List;

public class TransactionDetailView extends View implements GlobSelectionListener, ChangeSetListener {
  protected TransactionDetailView(GlobRepository repository, Directory directory) {
    super(repository, directory);
  }

  public void registerComponents(GlobsPanelBuilder builder) {
//    Field field = Transaction.
    builder.addEditor(Transaction.LABEL);
    builder.addEditor(Transaction.LABEL);
  }

  public void selectionUpdated(GlobSelection selection) {
  }

  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
  }

  public void globsReset(GlobRepository repository, List<GlobType> changedTypes) {
  }

  public void colorsChanged(ColorLocator colorLocator) {
  }
}
