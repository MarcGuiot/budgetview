package org.designup.picsou.gui.undo;

import org.designup.picsou.utils.Lang;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class RedoAction extends AbstractAction implements UndoRedoService.Listener {
  private UndoRedoService service;

  public RedoAction(Directory directory) {
    super(Lang.get("redo"));
    this.service = directory.get(UndoRedoService.class);
    this.service.addListener(this);
    update();
  }

  public void actionPerformed(ActionEvent e) {
    service.redo();
  }

  public void update() {
    setEnabled(service.isRedoAvailable());
  }
}
