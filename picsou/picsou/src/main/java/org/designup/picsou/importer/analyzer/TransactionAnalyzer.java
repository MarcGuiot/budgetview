package org.designup.picsou.importer.analyzer;

import org.crossbowlabs.globs.model.GlobList;
import org.crossbowlabs.globs.model.GlobRepository;

public interface TransactionAnalyzer {
  void processTransactions(GlobList transactions, GlobRepository globRepository);
}
