package org.designup.picsou.gui.signpost.guides;

import net.java.balloontip.BalloonTip;
import net.java.balloontip.TablecellBalloonTip;
import org.designup.picsou.gui.signpost.Signpost;
import org.designup.picsou.model.SignpostStatus;
import org.designup.picsou.model.Transaction;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.metamodel.fields.BooleanField;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.TypeChangeSetListener;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

public abstract class AbstractTableSignpost extends Signpost {
  private JTable table;
  private TableModelListener tableListener;
  protected GlobSelectionListener selectionListener;
  protected TypeChangeSetListener changeSetListener;

  public AbstractTableSignpost(BooleanField completionField, GlobRepository repository, Directory directory) {
    super(completionField, repository, directory);
    this.changeSetListener = new TypeChangeSetListener(SignpostStatus.TYPE) {
      protected void update(GlobRepository repository) {
        AbstractTableSignpost.this.update(table);
      }
    };
    this.selectionListener = new GlobSelectionListener() {
      public void selectionUpdated(GlobSelection selection) {
        update(table);
      }
    };
  }

  public void attach(JComponent table) {
    this.table = (JTable)table;
    tableListener = new TableModelListener() {
      public void tableChanged(TableModelEvent e) {
        update(AbstractTableSignpost.this.table);
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

  protected BalloonTip createBalloonTip(JComponent component, String text) {
    return new TablecellBalloonTip(table, text,
                                   getRow(), getColumn(),
                                   getBalloonStyle(),
                                   BalloonTip.Orientation.RIGHT_BELOW,
                                   BalloonTip.AttachLocation.CENTER,
                                   20, 20, false);
  }

  protected int getRow() {
    return 0;
  }

  protected int getColumn() {
    return 2;
  }

  protected abstract void update(JTable table);
}
