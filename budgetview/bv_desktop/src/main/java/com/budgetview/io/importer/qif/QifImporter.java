package com.budgetview.io.importer.qif;

import com.budgetview.desktop.components.dialogs.PicsouDialog;
import com.budgetview.io.importer.AccountFileImporter;
import com.budgetview.io.importer.utils.TypedInputStream;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;

public class QifImporter implements AccountFileImporter {
  public GlobList loadTransactions(TypedInputStream inputStream, GlobRepository initialRepository, GlobRepository targetRepository, PicsouDialog current) {
    return QifParser.read(inputStream, initialRepository, targetRepository);
  }
}
