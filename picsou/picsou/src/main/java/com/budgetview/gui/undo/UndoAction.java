package com.budgetview.gui.undo;

import com.budgetview.utils.Lang;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class UndoAction extends AbstractAction implements UndoRedoService.Listener {
  private UndoRedoService service;

  public UndoAction(Directory directory) {
    super(Lang.get("undo"));
    this.service = directory.get(UndoRedoService.class);
    this.service.addListener(this);
    update();
  }

  public void actionPerformed(ActionEvent e) {
    service.undo();
  }

  public void update() {
    setEnabled(service.isUndoAvailable());
  }
}
