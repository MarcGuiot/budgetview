package com.budgetview.io.importer.analyzer;

import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;

import java.util.List;

public interface TransactionAnalyzer {
  void processTransactions(Integer bankId, List<Glob> transactions, GlobRepository globRepository);
}
