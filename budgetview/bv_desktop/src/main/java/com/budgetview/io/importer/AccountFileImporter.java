package com.budgetview.io.importer;

import com.budgetview.desktop.components.dialogs.PicsouDialog;
import com.budgetview.io.importer.utils.TypedInputStream;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.exceptions.InvalidFormat;
import org.globsframework.utils.exceptions.OperationCancelled;

import java.io.IOException;

public interface AccountFileImporter {
  GlobList loadTransactions(TypedInputStream inputStream,
                            GlobRepository initialRepository,
                            GlobRepository targetRepository, PicsouDialog current)
    throws InvalidFormat, OperationCancelled, IOException;
}
