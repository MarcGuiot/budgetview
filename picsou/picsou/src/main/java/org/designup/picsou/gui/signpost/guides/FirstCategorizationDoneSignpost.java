package org.designup.picsou.gui.signpost.guides;

import net.java.balloontip.BalloonTip;
import net.java.balloontip.TablecellBalloonTip;
import org.designup.picsou.gui.signpost.Signpost;
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
import java.util.Set;
import java.awt.*;

public class FirstCategorizationDoneSignpost extends Signpost implements ChangeSetListener, GlobSelectionListener {
  private boolean operationCategorized = false;
  private JTable table;

  public FirstCategorizationDoneSignpost(GlobRepository repository, Directory directory) {
    super(SignpostStatus.FIRST_CATEGORIZATION_DONE_SHOWN, repository, directory);
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
    return super.canShow() && operationCategorized;
  }

  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    if (!isCompleted() && changeSet.containsChanges(Transaction.TYPE)) {
      Set<Key> keySet = changeSet.getUpdated(Transaction.SERIES);
      if (!keySet.isEmpty()) {
        operationCategorized = true;
        update();
      }
    }
  }

  protected void update() {
    if (SignpostStatus.isCompleted(SignpostStatus.CATEGORIZATION_AREA_SHOWN, repository)) {
      if (canShow()) {
        show(Lang.get("signpost.firstCategorizationDone"));
      }
    }
    else {
      if (isShowing()) {
        hide();
      }
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
  }

  protected BalloonTip createBalloonTip(JComponent component, String text) {
    int selectedRow = table.getSelectedRow();
    if (selectedRow == -1){
      selectedRow = 0;
    }
    return new TablecellBalloonTip(table, text,
                                   selectedRow, 1,
                                   BALLOON_STYLE,
                                   BalloonTip.Orientation.RIGHT_ABOVE,
                                   BalloonTip.AttachLocation.CENTER,
                                   20, 20, false);
  }

  public void selectionUpdated(GlobSelection selection) {
    if (!isCompleted() && operationCategorized && !selection.getAll(Transaction.TYPE).isEmpty()) {
      SignpostStatus.setCompleted(SignpostStatus.FIRST_CATEGORIZATION_DONE_SHOWN, repository);
      update();
    }
  }
}