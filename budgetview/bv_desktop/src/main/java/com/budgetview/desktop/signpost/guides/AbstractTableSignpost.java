package com.budgetview.desktop.signpost.guides;

import com.budgetview.desktop.signpost.PersistentSignpost;
import com.budgetview.model.SignpostStatus;
import com.budgetview.model.Transaction;
import net.java.balloontip.BalloonTip;
import net.java.balloontip.TableCellBalloonTip;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.metamodel.fields.BooleanField;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.TypeChangeSetListener;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import javax.swing.event.AncestorListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.awt.event.ComponentListener;

public abstract class AbstractTableSignpost extends PersistentSignpost {
  private JTable table;
  private TableModelListener tableListener;
  protected GlobSelectionListener selectionListener;
  protected TypeChangeSetListener changeSetListener;

  public AbstractTableSignpost(BooleanField completionField, GlobRepository repository, Directory directory) {
    super(completionField, repository, directory);
    this.changeSetListener = new TypeChangeSetListener(SignpostStatus.TYPE) {
      public void update(GlobRepository repository) {
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

  protected BalloonTip createBalloonTip(JComponent component, String text) {
    return new TableCellBalloonTip(table, new JLabel(text),
                                   getRow(), getColumn(),
                                   balloonTipStyle,
                                   BalloonTip.Orientation.RIGHT_BELOW,
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
          if (attachedComponent != null) {
            for (AncestorListener listener : attachedComponent.getAncestorListeners()) {
              if (listener.getClass().getName().startsWith("net.java.balloontip")) {
                attachedComponent.removeAncestorListener(listener);
              }
            }
          }
        }
      }

    };
  }

  protected int getRow() {
    return 0;
  }

  protected int getColumn() {
    return 2;
  }

  protected abstract void update(JTable table);
}
