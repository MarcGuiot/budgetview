package org.designup.picsou.gui.components;

import junit.framework.TestCase;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.impl.DefaultGlobRepository;
import org.globsframework.model.impl.DefaultGlobIdGenerator;
import org.globsframework.gui.SelectionService;

public class BalanceGraphTest extends TestCase {

  public static class Stats {
    public static GlobType TYPE;

    @Key
    public static IntegerField ID;

    public static DoubleField RECEIVED;
    public static DoubleField SPENT;

    static {
      GlobTypeLoader.init(Stats.class);
    }
  }

  public void test() throws Exception {
    SelectionService selectionService = new SelectionService();
    Directory directory = new DefaultDirectory();
    directory.add(selectionService);

    GlobRepository repository = new DefaultGlobRepository(new DefaultGlobIdGenerator());
    final Glob stat1 = repository.create(Stats.TYPE,
                                         value(Stats.RECEIVED, 2.0),
                                         value(Stats.SPENT, 4.0));

    final Glob stat2 = repository.create(Stats.TYPE,
                                         value(Stats.RECEIVED, 4.0),
                                         value(Stats.SPENT, 2.0));

    BalanceGraph graph = new BalanceGraph(Stats.TYPE, Stats.RECEIVED, Stats.SPENT, directory);

    checkBalance(graph, 0.0, 0.0);

    selectionService.select(stat1);
    checkBalance(graph, 0.5, 1.0);

    selectionService.select(new GlobList(stat1, stat2), Stats.TYPE);
    checkBalance(graph, 1.0, 1.0);

    selectionService.clear(Stats.TYPE);
    checkBalance(graph, 0.0, 0.0);
  }

  private void checkBalance(BalanceGraph graph, double receivedPercent, double spentPercent) {
    assertEquals(receivedPercent, graph.getReceivedPercent());
    assertEquals(spentPercent, graph.getSpentPercent());
  }
}
