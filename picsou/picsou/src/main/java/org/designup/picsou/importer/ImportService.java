package org.designup.picsou.importer;

import org.designup.picsou.importer.csv.CsvImporter;
import org.designup.picsou.importer.ofx.OfxImporter;
import org.designup.picsou.importer.qif.QifImporter;
import org.designup.picsou.importer.utils.TypedInputStream;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.InvalidFormat;
import org.globsframework.utils.exceptions.ItemNotFound;
import org.globsframework.utils.exceptions.OperationCancelled;
import org.globsframework.utils.exceptions.TruncatedFile;

import java.io.IOException;

public class ImportService {

  public void run(TypedInputStream fileStream, GlobRepository initialRepository,
                  GlobRepository targetRepository, Directory directory)
    throws IOException, ItemNotFound, InvalidFormat, OperationCancelled, TruncatedFile {

    AccountFileImporter importer = getImporter(fileStream, directory);
    importer.loadTransactions(fileStream.getBestProbableReader(), initialRepository, targetRepository);
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
    throw new ItemNotFound("Unknown file extension for " + type);
  }
}
