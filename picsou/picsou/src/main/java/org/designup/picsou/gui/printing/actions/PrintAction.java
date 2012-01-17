package org.designup.picsou.gui.printing.actions;

import org.designup.picsou.gui.printing.BudgetReport;
import org.designup.picsou.model.util.MonthRange;
import org.designup.picsou.utils.Lang;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

public class PrintAction extends AbstractAction {

  private GlobRepository repository;
  private Directory directory;

  public PrintAction(GlobRepository repository, Directory directory) {
    super(Lang.get("print.menu"));
    this.repository = repository;
    this.directory = directory;
  }

  public void actionPerformed(ActionEvent actionEvent) {
    PrinterJob printJob = PrinterJob.getPrinterJob();
    printJob.setJobName(Lang.get("application"));

    printJob.setPageable(new BudgetReport(new MonthRange(201101, 201112), repository, directory, printJob.defaultPage()));

    if (printJob.printDialog()) {
      try {
        printJob.print();
      }
      catch (PrinterException exc) {
        System.out.println(exc);
      }
    }
  }
}
