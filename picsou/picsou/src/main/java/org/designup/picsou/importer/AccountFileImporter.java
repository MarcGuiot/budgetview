package org.designup.picsou.importer;

import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.ReadOnlyGlobRepository;
import org.globsframework.utils.exceptions.InvalidFormat;
import org.globsframework.utils.exceptions.OperationCancelled;

import java.io.IOException;
import java.io.Reader;

public interface AccountFileImporter {
  GlobList loadTransactions(Reader reader,
                            GlobRepository initialRepository,
                            GlobRepository targetRepository)
    throws InvalidFormat, OperationCancelled, IOException;
}
