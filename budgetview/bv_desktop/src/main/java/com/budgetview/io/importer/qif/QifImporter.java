package com.budgetview.io.importer.qif;

import com.budgetview.io.importer.AccountFileImporter;
import com.budgetview.io.importer.utils.TypedInputStream;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;

import java.awt.*;

public class QifImporter implements AccountFileImporter {
  public GlobList loadTransactions(TypedInputStream inputStream, GlobRepository initialRepository, GlobRepository targetRepository, Window parent) {
    return QifParser.read(inputStream, initialRepository, targetRepository);
  }
}
