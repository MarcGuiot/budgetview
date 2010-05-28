package org.designup.picsou.gui.signpost.guides;

import net.java.balloontip.BalloonTip;
import net.java.balloontip.TablecellBalloonTip;
import org.designup.picsou.gui.signpost.Signpost;
import org.designup.picsou.model.SignpostStatus;
import org.designup.picsou.model.Transaction;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import javax.swing.event.TableModelListener;
import javax.swing.event.TableModelEvent;

public class CategorizationSelectionSignpost extends Signpost {
  private JTable table;
  private TableModelListener tableListener;
  private GlobSelectionListener selectionListener;

  public CategorizationSelectionSignpost(JTable table, GlobRepository repository, Directory directory) {
    super(table, SignpostStatus.CATEGORIZATION_SELECTION_SHOWN, repository, directory);
    this.table = table;
    tableListener = new TableModelListener() {
      public void tableChanged(TableModelEvent e) {
        update();
      }
    };
    selectionListener = new GlobSelectionListener() {
      public void selectionUpdated(GlobSelection selection) {
        update();
      }
    };
  }

  protected void init() {
    table.getModel().addTableModelListener(tableListener);
    selectionService.addListener(selectionListener, Transaction.TYPE);
  }

  public void update() {
    int rowCount = table.getModel().getRowCount();
    if (rowCount == 0) {
      return;
    }

    boolean selection = table.getSelectedRows().length > 0;
    if (!selection && canShow()) {
      show("signpost.categorizationSelection");
    }
    else if (selection && isShowing()) {
      hide();
    }
  }

  protected BalloonTip createBalloonTip(JComponent component, String text) {
    return new TablecellBalloonTip(table, text,
                                   0, 2,
                                   BALLOON_STYLE,
                                   BalloonTip.Orientation.RIGHT_BELOW,
                                   BalloonTip.AttachLocation.SOUTH,
                                   20, 20, false);
  }
}
