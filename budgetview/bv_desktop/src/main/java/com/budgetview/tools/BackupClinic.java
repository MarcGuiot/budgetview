package com.budgetview.tools;

import com.budgetview.desktop.AppCore;
import com.budgetview.desktop.backup.BackupService;
import com.budgetview.desktop.utils.datacheck.DataCheckExceptionHandler;
import com.budgetview.desktop.utils.datacheck.DataCheckReport;
import com.budgetview.desktop.utils.datacheck.DataCheckingService;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.exceptions.ExceptionHandler;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class BackupClinic {
  public static void main(String[] args) throws Exception {

    if (args.length < 1) {
      System.out.println("Args: <backup path>");
      System.exit(-1);
    }

    File file = new File(args[0]);
    if (!file.exists()) {
      System.out.println("File not found: " + file.getAbsolutePath());
      System.exit(-1);
    }
    if (!file.isFile()) {
      System.out.println("Not a file: " + file.getAbsolutePath());
      System.exit(-1);
    }

    DataCheckReport report = new DataCheckReport(System.out);
    AppCore core =
      AppCore.init("user", "pwd", null)
      .set(ExceptionHandler.class, new DataCheckExceptionHandler(report))
      .complete();

    InputStream input = new BufferedInputStream(new FileInputStream(file));
    core.getDirectory().get(BackupService.class).restore(input, null);

    GlobRepository repository = core.getRepository();

    DataCheckingService checker = new DataCheckingService(repository, core.getDirectory());

//    System.out.println("\n\n============ First Check =============\n");
    checker.doCheck(report);
    System.out.println(report.errorCount() + " errors");
    report.reset();

//    System.out.println("\n\n=========== Second Check =============\n");
//    checker.doCheck(report);
//    System.out.println(report.errorCount() + " errors");
  }
}
