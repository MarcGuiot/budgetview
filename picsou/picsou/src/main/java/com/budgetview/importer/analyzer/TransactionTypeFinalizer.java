package com.budgetview.importer.analyzer;

import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;

public interface TransactionTypeFinalizer {
  boolean processTransaction(Glob transaction, GlobRepository globRepository);
}
