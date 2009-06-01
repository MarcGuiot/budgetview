package org.designup.picsou.gui.series.edition;

import org.designup.picsou.model.SubSeries;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.SelectionService;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class DeleteSubSeriesAction extends AbstractAction implements GlobSelectionListener {
  private JDialog owner;

  public DeleteSubSeriesAction(GlobRepository repository, Directory directory, JDialog owner) {
    super(Lang.get("delete"));
    this.owner = owner;
    directory.get(SelectionService.class).addListener(this, SubSeries.TYPE);
    setEnabled(false);
  }

  protected String getTitle() {
    return Lang.get("subseries.rename.title");
  }

  protected String getInputLabel() {
    return Lang.get("subseries.rename.inputlabel");
  }

  protected String getOkLabel() {
    return Lang.get("ok");
  }

  protected String getCancelLabel() {
    return Lang.get("close");
  }

  public void actionPerformed(ActionEvent e) {
  }

  public void selectionUpdated(GlobSelection selection) {
  }
}