package org.designup.picsou.importer;

import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.ReadOnlyGlobRepository;

import java.io.Reader;

public interface AccountFileImporter {
  GlobList loadTransactions(Reader reader, ReadOnlyGlobRepository initialRepository, GlobRepository targetRepository);
}
