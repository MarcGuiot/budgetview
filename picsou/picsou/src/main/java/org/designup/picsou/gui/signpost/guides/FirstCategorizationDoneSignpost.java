package org.designup.picsou.gui.signpost.guides;

import net.java.balloontip.BalloonTip;
import net.java.balloontip.TableCellBalloonTip;
import org.designup.picsou.gui.signpost.PersistentSignpost;
import org.designup.picsou.gui.transactions.utils.TransactionMatchers;
import org.designup.picsou.model.SignpostSectionType;
import org.designup.picsou.model.SignpostStatus;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.ChangeSet;
import org.globsframework.model.ChangeSetListener;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import javax.swing.event.AncestorListener;
import java.awt.event.ComponentListener;
import java.util.Set;

public class FirstCategorizationDoneSignpost extends PersistentSignpost implements ChangeSetListener, GlobSelectionListener {

  private JTable table;

  public FirstCategorizationDoneSignpost(GlobRepository repository, Directory directory) {
    super(SignpostStatus.FIRST_CATEGORIZATION_DONE, repository, directory);
  }

  protected void init() {
    repository.addChangeListener(this);
    selectionService.addListener(this, Transaction.TYPE);
  }

  public void attach(JComponent table) {
    this.table = (JTable)table;
    super.attach(table);
  }

  protected boolean canShow() {
    return super.canShow() && hasCategorizedOperations();
  }

  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    if (!isCompleted() && changeSet.containsChanges(SignpostStatus.TYPE)) {
      if (SignpostSectionType.isCurrentTypeAfter(SignpostSectionType.BUDGET, repository)) {
        dispose();
        return;
      }
    }

    if (!isCompleted() && changeSet.containsChanges(Transaction.TYPE)) {
      Set<Key> keySet = changeSet.getUpdated(Transaction.SERIES);
      if (!keySet.isEmpty()) {
        update();
      }
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    if (changedTypes.contains(SignpostStatus.TYPE) || changedTypes.contains(Transaction.TYPE)) {
      update();
    }
  }

  protected void update() {
    if (SignpostStatus.isCompleted(SignpostStatus.FIRST_CATEGORIZATION_DONE, repository)) {
      return;
    }
    if (SignpostStatus.isCompleted(SignpostStatus.CATEGORIZATION_AREA_SELECTION_DONE, repository)) {
      if (canShow()) {
        if (noRemainingOperationsToCategorize()) {
          SignpostStatus.setCompleted(SignpostStatus.FIRST_CATEGORIZATION_DONE, repository);
        }
        else {
          show(Lang.get("signpost.firstCategorizationDone"));
        }
      }
    }
    else {
      if (isShowing()) {
        hide();
      }
    }
  }

  protected BalloonTip createBalloonTip(JComponent component, String text) {
    int selectedRow = table.getSelectedRow();
    if (selectedRow == -1) {
      selectedRow = 0;
    }
    return new TableCellBalloonTip(table, new JLabel(text),
                                   selectedRow, 1,
                                   balloonTipStyle,
                                   BalloonTip.Orientation.RIGHT_ABOVE,
                                   BalloonTip.AttachLocation.CENTER,
                                   20, 20, false) {
      public void closeBalloon() {
        super.closeBalloon();
        if (topLevelContainer != null) {
          for (ComponentListener listener : topLevelContainer.getComponentListeners()) {
            if (listener.getClass().getName().startsWith("net.java.balloontip")) {
              topLevelContainer.removeComponentListener(listener);
            }
          }
        }
        if (attachedComponent != null) {
          for (AncestorListener listener : attachedComponent.getAncestorListeners()) {
            if (listener.getClass().getName().startsWith("net.java.balloontip")) {
              attachedComponent.removeAncestorListener(listener);
            }
          }
        }
      }
    };
  }

  public void selectionUpdated(GlobSelection selection) {
    if (!selection.getAll(Transaction.TYPE).isEmpty() && hasCategorizedOperations() && isShowing()) {
      SignpostStatus.setCompleted(SignpostStatus.FIRST_CATEGORIZATION_DONE, repository);
    }
  }

  private boolean noRemainingOperationsToCategorize() {
    return repository.contains(Transaction.TYPE) &&
           !repository.contains(Transaction.TYPE, TransactionMatchers.uncategorizedTransactions());
  }

  private boolean hasCategorizedOperations() {
    return repository.contains(Transaction.TYPE, TransactionMatchers.categorizedTransactions());
  }
}