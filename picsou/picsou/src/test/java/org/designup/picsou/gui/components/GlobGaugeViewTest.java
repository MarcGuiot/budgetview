package org.designup.picsou.gui.components;

import org.designup.picsou.gui.components.charts.Gauge;
import org.designup.picsou.gui.components.charts.GlobGaugeView;
import org.designup.picsou.model.BudgetArea;
import org.globsframework.gui.GuiTestCase;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.DefaultBoolean;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.fields.BooleanField;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.GlobRepositoryBuilder;
import org.globsframework.model.format.GlobPrinter;
import org.globsframework.model.utils.GlobBuilder;
import org.globsframework.model.utils.GlobMatchers;

import java.util.Arrays;

import static org.globsframework.model.FieldValue.value;
import static org.globsframework.model.utils.GlobMatchers.fieldEquals;

public class GlobGaugeViewTest extends GuiTestCase {
  private GlobRepository repository;
  private Gauge gauge;
  private Glob obj1;
  private Glob obj2;
  private Glob obj3;
  private Glob obj4;

  protected void setUp() throws Exception {
    super.setUp();
    repository = GlobRepositoryBuilder.createEmpty();
    obj1 = create(1, 15.0, 20.0);
    obj2 = create(2, 5.0, 10.0);
    obj3 = create(3, 10.0, 10.0);
    obj4 = create(4, null, null);

    GlobGaugeView view = new GlobGaugeView(MyObject.TYPE, BudgetArea.VARIABLE,
                                           MyObject.ACTUAL, MyObject.TARGET, MyObject.PAST_REMAINING, MyObject.FUTURE_REMAINING,
                                           MyObject.PAST_OVERRUN, MyObject.FUTURE_OVERRUN,
                                           MyObject.ACTIVE,
                                           GlobMatchers.not(fieldEquals(MyObject.ID, 3)),
                                           repository, directory);
    gauge = view.getComponent();
  }

  public void testSelection() throws Exception {
    checkGauge(0d, 0d, false);

    selectionService.select(obj1);
    checkGauge(15.0d, 20.0d);

    selectionService.select(Arrays.asList(obj1, obj2), MyObject.TYPE);
    checkGauge(20.0d, 30.0d);

    selectionService.select(Arrays.asList(obj1, obj2, obj3), MyObject.TYPE);
    checkGauge(20.0d, 30.0d);

    selectionService.select(obj3); // Excluded by matcher
    checkGauge(0d, 0d, false);
  }

  public void testNullsAreIgnored() throws Exception {
    selectionService.select(Arrays.asList(obj1, obj4), MyObject.TYPE);
    checkGauge(15.0d, 20.0d);
  }

  public void testModification() throws Exception {
    selectionService.select(Arrays.asList(obj1, obj2), MyObject.TYPE);
    checkGauge(20.0d, 30.0d);

    repository.update(obj1.getKey(),
                      value(MyObject.ACTUAL, 25.0),
                      value(MyObject.TARGET, 50.0));
    checkGauge(30.0d, 60.0d);

    repository.delete(obj2.getKey());
    checkGauge(25.0d, 50.0d);

    Glob obj5 = create(5, 20d, 20d);
    checkGauge(25.0d, 50.0d);

    selectionService.select(Arrays.asList(obj1, obj5), MyObject.TYPE);
    checkGauge(45.0d, 70.0d);
  }

  public void testActive() throws Exception {
    selectionService.select(obj1);
    checkGauge(15.0d, 20.0d);

    repository.update(obj1.getKey(),
                      value(MyObject.ACTIVE, false));
    checkGauge(15.0d, 20.0d, false);
  }

  public void testReset() throws Exception {
    Glob obj6 =
      GlobBuilder.init(MyObject.TYPE)
        .set(MyObject.ID, 6)
        .set(MyObject.ACTUAL, 4.0)
        .set(MyObject.TARGET, 7.0)
        .get();

    selectionService.select(obj1);
    checkGauge(15.0d, 20.0d);

    repository.reset(new GlobList(obj6), MyObject.TYPE);
    checkGauge(0.0d, 0.0d, false);

    selectionService.select(obj6);
    checkGauge(4.0d, 7.0d, true);
  }

  private void checkGauge(double actual, double target) {
    checkGauge(actual, target, true);
  }

  private void checkGauge(double actual, double target, boolean active) {
    assertEquals(actual, gauge.getActualValue());
    assertEquals(target, gauge.getTargetValue());
    assertEquals(active, gauge.isActive());
  }

  public static class MyObject {
    public static GlobType TYPE;

    @Key
    public static IntegerField ID;

    public static DoubleField ACTUAL;
    public static DoubleField TARGET;

    public static DoubleField PAST_OVERRUN;
    public static DoubleField FUTURE_OVERRUN;

    public static DoubleField PAST_REMAINING;
    public static DoubleField FUTURE_REMAINING;

    @DefaultBoolean(true)
    public static BooleanField ACTIVE;

    static {
      GlobTypeLoader.init(MyObject.class);
    }
  }

  private Glob create(int id, Double actual, Double target) {
    return repository.create(MyObject.TYPE,
                             value(MyObject.ID, id),
                             value(MyObject.ACTUAL, actual),
                             value(MyObject.TARGET, target));
  }
}
