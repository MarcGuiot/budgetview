package org.designup.picsou.gui.utils;

import org.designup.picsou.gui.undo.UndoRedoService;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.GlobPrinter;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class DumpUndoStackAction extends AbstractAction {
  public static final String LABEL = "Dump undo stack";
  private final GlobRepository repository;
  private Directory directory;

  public DumpUndoStackAction(GlobRepository repository, Directory directory) {
    super(LABEL);
    this.repository = repository;
    this.directory = directory;
  }

  public void actionPerformed(ActionEvent e) {

    UndoRedoService undoRedoService = directory.get(UndoRedoService.class);
    undoRedoService.dump(System.out);
  }
}