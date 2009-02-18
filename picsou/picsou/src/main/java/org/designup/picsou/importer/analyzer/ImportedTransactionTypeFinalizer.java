package org.designup.picsou.importer.analyzer;

import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;

public interface ImportedTransactionTypeFinalizer {
  boolean processTransaction(Glob transaction, GlobRepository repository);
}
