package org.designup.picsou.gui.utils;

import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.GlobPrinter;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class DumpDataAction extends AbstractAction {
  private final GlobRepository repository;

  public DumpDataAction(GlobRepository repository) {
    super("Dump data");
    this.repository = repository;
  }

  public void actionPerformed(ActionEvent e) {

    try {
      File file = File.createTempFile("dump", ".txt");
      GlobPrinter.init(repository).run(new FileWriter(file));
      System.out.println("Dump in : " + file.getAbsolutePath());
    }
    catch (IOException e1) {
    }
  }
}
