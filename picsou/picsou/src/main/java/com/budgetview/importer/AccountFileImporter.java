package com.budgetview.importer;

import com.budgetview.gui.components.dialogs.PicsouDialog;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.exceptions.InvalidFormat;
import org.globsframework.utils.exceptions.OperationCancelled;

import java.io.IOException;
import java.io.Reader;

public interface AccountFileImporter {
  GlobList loadTransactions(Reader reader,
                            GlobRepository initialRepository,
                            GlobRepository targetRepository, PicsouDialog current)
    throws InvalidFormat, OperationCancelled, IOException;
}
