package org.designup.picsou.gui.transactions;

import org.designup.picsou.gui.View;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.color.ColorLocator;
import org.globsframework.gui.views.GlobLabelView;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.ChangeSet;
import org.globsframework.model.ChangeSetListener;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.utils.GlobListStringFieldStringifier;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.util.List;

public class TransactionDetailsView extends View implements GlobSelectionListener, ChangeSetListener {
  public TransactionDetailsView(GlobRepository repository, Directory directory) {
    super(repository, directory);
  }

  public void registerComponents(GlobsPanelBuilder builder) {
    builder.add("transactionDetails", createPanel());
  }

  private JPanel createPanel() {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(repository, directory);
    builder.add("label",
                GlobLabelView.init(Transaction.TYPE, repository, directory,
                                   new GlobListStringFieldStringifier(Transaction.LABEL,
                                                                      Lang.get("transaction.details.multilabel"))));
    return (JPanel)builder.parse(TransactionDetailsView.class, "/layout/transactionDetails.splits");
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
