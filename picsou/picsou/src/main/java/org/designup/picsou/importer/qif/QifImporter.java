package org.designup.picsou.importer.qif;

import org.crossbowlabs.globs.model.GlobList;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.model.ReadOnlyGlobRepository;
import org.designup.picsou.importer.AccountFileImporter;

import java.io.Reader;

public class QifImporter implements AccountFileImporter {
  public QifImporter() {
  }

  public GlobList loadTransactions(Reader reader, GlobRepository targetRepository, ReadOnlyGlobRepository initialRepository) {
    return QifParser.read(reader, targetRepository);
  }
}
