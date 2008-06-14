package org.designup.picsou.importer.qif;

import org.designup.picsou.importer.AccountFileImporter;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.ReadOnlyGlobRepository;

import java.io.Reader;

public class QifImporter implements AccountFileImporter {
  public QifImporter() {
  }

  public GlobList loadTransactions(Reader reader, ReadOnlyGlobRepository initialRepository, GlobRepository targetRepository) {
    return QifParser.read(reader, targetRepository);
  }
}
