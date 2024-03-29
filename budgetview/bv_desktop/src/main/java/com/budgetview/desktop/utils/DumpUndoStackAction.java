package com.budgetview.desktop.utils;

import com.budgetview.desktop.undo.UndoRedoService;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class DumpUndoStackAction extends AbstractAction {
  public static final String LABEL = "Dump undo stack";
  private Directory directory;

  public DumpUndoStackAction(GlobRepository repository, Directory directory) {
    super(LABEL);
    this.directory = directory;
  }

  public void actionPerformed(ActionEvent e) {

    UndoRedoService undoRedoService = directory.get(UndoRedoService.class);
    undoRedoService.dump(System.out);
  }
}
