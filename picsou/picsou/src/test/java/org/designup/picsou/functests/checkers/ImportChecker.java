package org.designup.picsou.functests.checkers;

import junit.framework.Assert;
import org.designup.picsou.model.TransactionImport;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.utils.Dates;

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
    Assert.assertEquals(balance, imports.get(0).get(TransactionImport.BALANCE));
  }
}
