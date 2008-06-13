package org.designup.picsou.importer.analyzer;

import org.crossbowlabs.globs.model.Glob;
import org.crossbowlabs.globs.model.GlobRepository;

import java.text.SimpleDateFormat;

public interface TransactionTypeFinalizer {
  boolean processTransaction(Glob transaction, GlobRepository globRepository, SimpleDateFormat format);
}
