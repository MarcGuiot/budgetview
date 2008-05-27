package org.designup.picsou.functests.checkers;

import junit.framework.Assert;
import org.crossbowlabs.globs.model.Glob;
import org.crossbowlabs.globs.model.GlobList;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.model.utils.GlobMatcher;
import org.crossbowlabs.globs.utils.Dates;
import org.designup.picsou.model.TransactionImport;

import java.util.Date;

public class ImportChecker extends DataChecker {
  private GlobRepository repository;

  public ImportChecker(GlobRepository repository) {
    this.repository = repository;
  }

  public void check(String yyyymmdd, Double balance) {
    final Date date = Dates.parse(yyyymmdd);
    GlobList imports = repository.getAll(TransactionImport.TYPE, new GlobMatcher() {
      public boolean matches(Glob item, GlobRepository repository) {
        return item.get(TransactionImport.LAST_TRANSACTION_DATE).equals(date);
      }
    });
    Assert.assertEquals(1, imports.size());
    Assert.assertEquals(imports.get(0).get(TransactionImport.BALANCE), balance);
  }
}
