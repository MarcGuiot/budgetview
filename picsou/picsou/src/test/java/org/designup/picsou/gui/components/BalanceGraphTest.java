package org.designup.picsou.gui.components;

import junit.framework.TestCase;
import org.designup.picsou.functests.checkers.BalanceGraphChecker;
import org.designup.picsou.gui.model.BalanceStat;
import org.designup.picsou.model.Month;
import org.globsframework.gui.SelectionService;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.impl.DefaultGlobIdGenerator;
import org.globsframework.model.impl.DefaultGlobRepository;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

public class BalanceGraphTest extends TestCase {

  public void test() throws Exception {
    SelectionService selectionService = new SelectionService();
    Directory directory = new DefaultDirectory();
    directory.add(selectionService);

    GlobRepository repository = new DefaultGlobRepository(new DefaultGlobIdGenerator());
    Glob month10 = repository.create(Month.TYPE, value(Month.ID, 200810));
    Glob month11 = repository.create(Month.TYPE, value(Month.ID, 200811));
    repository.create(BalanceStat.TYPE,
                      value(BalanceStat.MONTH, 200810),
                      value(BalanceStat.INCOME, 2.0),
                      value(BalanceStat.EXPENSE, 4.0));

    repository.create(BalanceStat.TYPE,
                      value(BalanceStat.MONTH, 200811),
                      value(BalanceStat.INCOME, 4.0),
                      value(BalanceStat.EXPENSE, 2.0));

    BalanceGraphChecker graph = new BalanceGraphChecker(new BalanceGraph(repository, directory));

    graph.checkBalance(0.0, 0.0);

    selectionService.select(month10);
    graph.checkBalance(0.5, 1.0);

    selectionService.select(new GlobList(month10, month11), Month.TYPE);
    graph.checkBalance(1.0, 1.0);

    selectionService.clear(Month.TYPE);
    graph.checkBalance(0.0, 0.0);
  }
}
