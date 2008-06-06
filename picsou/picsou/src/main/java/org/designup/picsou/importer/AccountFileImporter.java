package org.designup.picsou.importer;

import org.crossbowlabs.globs.model.GlobList;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.model.ReadOnlyGlobRepository;

import java.io.Reader;

public interface AccountFileImporter {
  GlobList loadTransactions(Reader reader, ReadOnlyGlobRepository initialRepository, GlobRepository targetRepository);
}
