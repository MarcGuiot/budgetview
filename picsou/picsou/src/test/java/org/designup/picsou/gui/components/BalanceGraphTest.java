package org.designup.picsou.gui.components;

import junit.framework.TestCase;
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

    BalanceGraph graph = new BalanceGraph(repository, directory);

    checkBalance(graph, 0.0, 0.0);

    selectionService.select(month10);
    checkBalance(graph, 0.5, 1.0);

    selectionService.select(new GlobList(month10, month11), Month.TYPE);
    checkBalance(graph, 1.0, 1.0);

    selectionService.clear(Month.TYPE);
    checkBalance(graph, 0.0, 0.0);
  }

  private void checkBalance(BalanceGraph graph, double receivedPercent, double spentPercent) {
    assertEquals(receivedPercent, graph.getIncomePercent());
    assertEquals(spentPercent, graph.getExpensesPercent());
  }
}
