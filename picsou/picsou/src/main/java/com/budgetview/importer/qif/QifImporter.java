package com.budgetview.importer.qif;

import com.budgetview.gui.components.dialogs.PicsouDialog;
import com.budgetview.importer.AccountFileImporter;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;

import java.io.Reader;

public class QifImporter implements AccountFileImporter {
  public GlobList loadTransactions(Reader reader, GlobRepository initialRepository, GlobRepository targetRepository, PicsouDialog current) {
    return QifParser.read(reader, initialRepository, targetRepository);
  }
}
