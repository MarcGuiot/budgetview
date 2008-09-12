package org.designup.picsou.gui.transactions;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.card.NavigationService;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import static org.globsframework.model.utils.GlobMatchers.*;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.Set;

public class UncategorizedMessageView extends View implements ChangeSetListener {
  private JTextArea textArea = new JTextArea();
  private String textAreaName;
  private String buttonName;
  private String buttonText;
  private Set<Integer> currentMonthIds = Collections.emptySet();

  public UncategorizedMessageView(String textAreaName, String buttonName, String buttonText,
                                  GlobRepository repository, Directory directory) {
    super(repository, directory);
    this.buttonText = buttonText;
    repository.addChangeListener(this);
    this.textAreaName = textAreaName;
    this.buttonName = buttonName;
    update();
  }

  public void registerComponents(GlobsPanelBuilder builder) {
    builder.add(textAreaName, textArea);
    builder.add(buttonName, new EditUncategorizedTransactionsAction());
  }

  public void globsChanged(ChangeSet changeSet, GlobRepository globRepository) {
    if (changeSet.containsChanges(Transaction.TYPE)) {
      update();
    }
  }

  public void globsReset(GlobRepository globRepository, Set<GlobType> changedTypes) {
    if (changedTypes.contains(Transaction.TYPE)) {
      update();
    }
  }

  private void update() {
    GlobList uncategorizedTransactions =
      repository.getAll(Transaction.TYPE, or(fieldEquals(Transaction.SERIES, Series.UNCATEGORIZED_SERIES_ID),
                                             isNull(Transaction.CATEGORY)));
    final int count = uncategorizedTransactions.size();
    textArea.setVisible(count > 0);
    if (count > 0) {
      textArea.setText(Lang.get("transaction.allocation.warning", count));
      currentMonthIds = uncategorizedTransactions.getValueSet(Transaction.MONTH);
    }
  }

  private class EditUncategorizedTransactionsAction extends AbstractAction {
    public EditUncategorizedTransactionsAction() {
      super(buttonText);
    }

    public void actionPerformed(ActionEvent e) {
      directory.get(NavigationService.class).gotoCategorization();

      GlobList months = new GlobList();
      for (Integer monthId : currentMonthIds) {
        months.add(repository.get(Key.create(Month.TYPE, monthId)));
      }
      selectionService.select(months, Month.TYPE);
    }
  }
}
