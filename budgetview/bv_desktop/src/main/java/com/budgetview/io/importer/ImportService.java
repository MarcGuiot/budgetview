package com.budgetview.io.importer;

import com.budgetview.io.importer.csv.CsvImporter;
import com.budgetview.io.importer.json.JsonImporter;
import com.budgetview.io.importer.ofx.OfxImporter;
import com.budgetview.io.importer.qif.QifImporter;
import com.budgetview.io.importer.utils.TypedInputStream;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.InvalidFormat;
import org.globsframework.utils.exceptions.ItemNotFound;
import org.globsframework.utils.exceptions.OperationCancelled;
import org.globsframework.utils.exceptions.TruncatedFile;

import java.awt.*;
import java.io.IOException;

public class ImportService {

  public void run(TypedInputStream fileStream, GlobRepository initialRepository,
                  GlobRepository targetRepository, Directory directory, Window parentWindow)
    throws IOException, ItemNotFound, InvalidFormat, OperationCancelled, TruncatedFile {

    AccountFileImporter importer = getImporter(fileStream, directory);
    importer.loadTransactions(fileStream, initialRepository, targetRepository, parentWindow);
  }

  private AccountFileImporter getImporter(TypedInputStream fileStream, Directory directory) throws ItemNotFound {
    BankFileType type = fileStream.getType();
    if (type == BankFileType.OFX) {
      return new OfxImporter();
    }
    else if (type == BankFileType.QIF) {
      return new QifImporter();
    }
    else if (type == BankFileType.CSV) {
      return new CsvImporter(fileStream, directory);
    }
    else if (type == BankFileType.JSON) {
      return new JsonImporter();
    }
    throw new ItemNotFound("Unknown file extension for " + type);
  }
}
