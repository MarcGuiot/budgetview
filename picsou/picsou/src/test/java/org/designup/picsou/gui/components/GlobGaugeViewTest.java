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
import org.globsframework.model.utils.GlobBuilder;

import java.util.Arrays;

import static org.globsframework.model.FieldValue.value;
import static org.globsframework.model.utils.GlobMatchers.fieldEquals;

public class GlobGaugeViewTest extends GuiTestCase {
  private GlobRepository repository;
  private Gauge gauge;
  private Glob obj1;
  private Glob obj2;
  private org.globsframework.model.Key key1;

  protected void setUp() throws Exception {
    super.setUp();
    repository = GlobRepositoryBuilder.createEmpty();
    key1 = org.globsframework.model.Key.create(MyObject.TYPE, 1);
    obj1 = create(1, 15.0, 20.0);
    obj2 = create(2, 5.0, 10.0);

    GlobGaugeView view = new GlobGaugeView(key1,
                                           BudgetArea.VARIABLE,
                                           MyObject.ACTUAL, MyObject.TARGET, MyObject.PAST_REMAINING, MyObject.FUTURE_REMAINING,
                                           MyObject.PAST_OVERRUN, MyObject.FUTURE_OVERRUN,
                                           MyObject.ACTIVE,
                                           repository, directory);
    gauge = view.getComponent();
  }

  public void testNullsAreIgnored() throws Exception {
    repository.update(key1, MyObject.ACTUAL, null);
    checkGauge(0.0, 20.0);
  }

  public void testModification() throws Exception {
    repository.update(obj1.getKey(),
                      value(MyObject.ACTUAL, 25.0),
                      value(MyObject.TARGET, 50.0));
    checkGauge(25.0d, 50.0d);

    repository.delete(obj2.getKey());
    checkGauge(25.0d, 50.0d);

    repository.delete(obj1.getKey());
    checkGauge(0.0d, 0.0d, false);

    repository.create(obj1.getKey(),
                      value(MyObject.ACTUAL, 15.0),
                      value(MyObject.TARGET, 40.0));
    checkGauge(15.0d, 40.0d);

    create(5, 20d, 20d);
    checkGauge(15.0d, 40.0d);
  }

  public void testReset() throws Exception {
    Glob newObj1 =
      GlobBuilder.init(MyObject.TYPE)
        .set(MyObject.ID, 1)
        .set(MyObject.ACTUAL, 10.0)
        .set(MyObject.TARGET, 20.0)
        .get();
    Glob newObj2 =
      GlobBuilder.init(MyObject.TYPE)
        .set(MyObject.ID, 2)
        .set(MyObject.ACTUAL, 20.0)
        .set(MyObject.TARGET, 40.0)
        .get();

    repository.reset(new GlobList(newObj1, newObj2), MyObject.TYPE);
    checkGauge(10.0d, 20.0d);

    repository.reset(new GlobList(newObj2), MyObject.TYPE);
    checkGauge(0.0d, 0.0d, false);
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
