package com.budgetview.io.importer.csv;

import com.budgetview.desktop.components.dialogs.PicsouDialog;
import com.budgetview.io.importer.AccountFileImporter;
import com.budgetview.io.importer.utils.TypedInputStream;
import com.budgetview.desktop.importer.csv.CsvImporterDialog;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.OperationCancelled;

import java.io.IOException;

public class CsvImporter implements AccountFileImporter {
  private TypedInputStream fileStream;
  private Directory directory;

  public CsvImporter(TypedInputStream fileStream, Directory directory) {
    this.fileStream = fileStream;
    this.directory = directory;
  }

  public GlobList loadTransactions(TypedInputStream inputStream, GlobRepository initialRepository, GlobRepository targetRepository, PicsouDialog current) throws OperationCancelled, IOException {
    CsvImporterDialog dialog = new CsvImporterDialog(current, fileStream, initialRepository, targetRepository, directory);
    return dialog.show();
  }
}
