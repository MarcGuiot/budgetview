package org.designup.picsou.importer;

import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.ReadOnlyGlobRepository;
import org.globsframework.utils.exceptions.OperationCancelled;

import java.io.Reader;

public interface AccountFileImporter {
  GlobList loadTransactions(Reader reader,
                            GlobRepository initialRepository,
                            GlobRepository targetRepository) throws OperationCancelled;
}
