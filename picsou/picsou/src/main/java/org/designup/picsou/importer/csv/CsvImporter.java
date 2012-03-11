package org.designup.picsou.importer.csv;

import org.designup.picsou.gui.components.dialogs.PicsouDialog;
import org.designup.picsou.gui.importer.csv.CsvImporterDialog;
import org.designup.picsou.importer.AccountFileImporter;
import org.designup.picsou.importer.utils.TypedInputStream;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.OperationCancelled;

import java.io.IOException;
import java.io.Reader;

public class CsvImporter implements AccountFileImporter {
  private TypedInputStream fileStream;
  private Directory directory;

  public CsvImporter(TypedInputStream fileStream, Directory directory) {
    this.fileStream = fileStream;
    this.directory = directory;
  }

  public GlobList loadTransactions(Reader reader, GlobRepository initialRepository, GlobRepository targetRepository, PicsouDialog current) throws OperationCancelled, IOException {
    CsvImporterDialog dialog = new CsvImporterDialog(current, fileStream, initialRepository, targetRepository, directory);
    return dialog.show();
  }
}
