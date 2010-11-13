package org.designup.picsou.importer;

import org.designup.picsou.importer.ofx.OfxImporter;
import org.designup.picsou.importer.qif.QifImporter;
import org.designup.picsou.importer.utils.TypedInputStream;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.ReadOnlyGlobRepository;
import org.globsframework.utils.exceptions.ItemNotFound;
import org.globsframework.utils.exceptions.TruncatedFile;

import java.io.IOException;

public class ImportService {

  public void run(TypedInputStream fileStream, ReadOnlyGlobRepository initialRepository,
                  GlobRepository targetRepository) throws IOException, ItemNotFound, TruncatedFile {
    AccountFileImporter importer = getImporter(fileStream.getType());
    importer.loadTransactions(fileStream.getBestProbableReader(), initialRepository, targetRepository);
  }

  private AccountFileImporter getImporter(BankFileType type) throws ItemNotFound {
    if (type == BankFileType.OFX) {
      return new OfxImporter();
    }
    if (type == BankFileType.QIF) {
      return new QifImporter();
    }
   throw new ItemNotFound("Unknown file extension for " + type);
  }
}
