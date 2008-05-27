package org.designup.picsou.importer;

import org.crossbowlabs.globs.model.GlobList;
import org.crossbowlabs.globs.model.GlobRepository;

import java.io.Reader;

public interface AccountFileImporter {
  GlobList loadTransactions(Reader reader, GlobRepository globRepository);
}
