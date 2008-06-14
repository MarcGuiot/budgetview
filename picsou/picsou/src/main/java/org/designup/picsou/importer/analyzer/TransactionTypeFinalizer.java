package org.designup.picsou.importer.analyzer;

import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;

import java.text.SimpleDateFormat;

public interface TransactionTypeFinalizer {
  boolean processTransaction(Glob transaction, GlobRepository globRepository, SimpleDateFormat format);
}
