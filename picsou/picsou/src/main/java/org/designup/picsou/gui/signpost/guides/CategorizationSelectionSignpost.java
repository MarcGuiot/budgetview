package org.designup.picsou.gui.signpost.guides;

import net.java.balloontip.BalloonTip;
import net.java.balloontip.TablecellBalloonTip;
import org.designup.picsou.gui.signpost.Signpost;
import org.designup.picsou.model.SignpostStatus;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.TypeChangeSetListener;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

public class CategorizationSelectionSignpost extends Signpost {
  private JTable table;
  private TableModelListener tableListener;
  private GlobSelectionListener selectionListener;
  private TypeChangeSetListener changeSetListener;

  public CategorizationSelectionSignpost(GlobRepository repository, Directory directory) {
    super(SignpostStatus.CATEGORIZATION_SELECTION_SHOWN, repository, directory);
    this.selectionListener = new GlobSelectionListener() {
      public void selectionUpdated(GlobSelection selection) {
        update();
      }
    };
    this.changeSetListener = new TypeChangeSetListener(SignpostStatus.TYPE) {
      protected void update(GlobRepository repository) {
        CategorizationSelectionSignpost.this.update();
      }
    };
  }

  public void attach(JComponent table) {
    this.table = (JTable)table;
    tableListener = new TableModelListener() {
      public void tableChanged(TableModelEvent e) {
        update();
      }
    };
    super.attach(table);
  }

  protected void init() {
    table.getModel().addTableModelListener(tableListener);
    selectionService.addListener(selectionListener, Transaction.TYPE);
    repository.addChangeListener(changeSetListener);
  }

  public void dispose() {
    table.getModel().removeTableModelListener(tableListener);
    selectionService.removeListener(selectionListener);
    repository.removeChangeListener(changeSetListener);
    super.dispose();
  }

  public void update() {
    if (!SignpostStatus.isCompleted(SignpostStatus.GOTO_CATEGORIZATION_SHOWN, repository)) {
      return;
    }

    int rowCount = table.getModel().getRowCount();
    if (rowCount == 0) {
      return;
    }

    boolean selection = table.getSelectedRows().length > 0;
    if (!selection && canShow()) {
      show(Lang.get("signpost.categorizationSelection"));
    }
    else if (selection && isShowing()) {
      dispose();
    }
  }

  protected BalloonTip createBalloonTip(JComponent component, String text) {
    return new TablecellBalloonTip(table, text,
                                   0, 2,
                                   BALLOON_STYLE,
                                   BalloonTip.Orientation.RIGHT_BELOW,
                                   BalloonTip.AttachLocation.CENTER,
                                   20, 20, false);
  }
}
