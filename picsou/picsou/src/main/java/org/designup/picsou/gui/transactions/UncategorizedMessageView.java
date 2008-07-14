package org.designup.picsou.gui.transactions;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.categorization.CategorizationDialog;
import org.designup.picsou.model.Transaction;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.ChangeSet;
import org.globsframework.model.ChangeSetListener;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import static org.globsframework.model.utils.GlobMatchers.*;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class UncategorizedMessageView extends View implements ChangeSetListener {
  private JTextArea textArea = new JTextArea();
  private GlobList uncategorizedTransactions = GlobList.EMPTY;
  private String textAreaName;
  private String buttonName;

  public UncategorizedMessageView(String textAreaName, String buttonName,
                                  GlobRepository repository, Directory directory) {
    super(repository, directory);
    repository.addChangeListener(this);
    this.textAreaName = textAreaName;
    this.buttonName = buttonName;
    update();
  }

  public void registerComponents(GlobsPanelBuilder builder) {
    builder.add(textAreaName, textArea);
    builder.add(buttonName, new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        final CategorizationDialog dialog = directory.get(CategorizationDialog.class);
        dialog.show(uncategorizedTransactions, false);
      }
    });
  }

  public void globsChanged(ChangeSet changeSet, GlobRepository globRepository) {
    if (changeSet.containsChanges(Transaction.TYPE)) {
      update();
    }
  }

  public void globsReset(GlobRepository globRepository, List<GlobType> changedTypes) {
    update();
  }

  private void update() {
    uncategorizedTransactions =
      repository.getAll(Transaction.TYPE, or(isNull(Transaction.SERIES), isNull(Transaction.CATEGORY)));
    textArea.setVisible(uncategorizedTransactions.size() > 0);
  }
}
