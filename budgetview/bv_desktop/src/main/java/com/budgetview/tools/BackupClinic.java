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
      System.out.println("Args: <backup path> [<fixed backup path>]");
      System.exit(-1);
    }

    File inputFile = new File(args[0]);
    if (!inputFile.exists()) {
      System.out.println("File not found: " + inputFile.getAbsolutePath());
      System.exit(-1);
    }
    if (!inputFile.isFile()) {
      System.out.println("Not a file: " + inputFile.getAbsolutePath());
      System.exit(-1);
    }

    DataCheckReport report = new DataCheckReport(System.out);
    AppCore core =
      AppCore.init("user", "pwd", null)
        .set(ExceptionHandler.class, new DataCheckExceptionHandler(report))
        .complete();

    InputStream input = new BufferedInputStream(new FileInputStream(inputFile));
    core.getDirectory().get(BackupService.class).restore(input, null);

    GlobRepository repository = core.getRepository();

    DataCheckingService checker = new DataCheckingService(repository, core.getDirectory());
    checker.doCheck(report);
    System.out.println("Check completed - errors: " + report.errorCount() + " fixes: " + report.fixCount());

    if (report.hasFixes() && args.length == 2) {
      File outputFile = new File(args[1]);
      core.getDirectory().get(BackupService.class).generate(outputFile);
      System.out.println("Fixed backup generated in " + outputFile.getAbsolutePath());
    }
  }
}
