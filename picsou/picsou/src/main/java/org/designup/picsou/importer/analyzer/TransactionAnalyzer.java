package org.designup.picsou.importer.analyzer;

import org.crossbowlabs.globs.model.Glob;
import org.crossbowlabs.globs.model.GlobRepository;

import java.util.List;

public interface TransactionAnalyzer {
  void processTransactions(Integer bankId, List<Glob> transactions, GlobRepository globRepository, String selectedDateFormat);
}
