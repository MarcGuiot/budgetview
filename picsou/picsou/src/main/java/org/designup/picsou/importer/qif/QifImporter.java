package org.designup.picsou.importer.qif;

import org.designup.picsou.importer.AccountFileImporter;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;

import java.io.Reader;

public class QifImporter implements AccountFileImporter {
  public GlobList loadTransactions(Reader reader, GlobRepository initialRepository, GlobRepository targetRepository) {
    return QifParser.read(reader, initialRepository, targetRepository);
  }
}
