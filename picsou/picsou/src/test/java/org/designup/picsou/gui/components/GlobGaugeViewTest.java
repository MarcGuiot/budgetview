package org.designup.picsou.gui.components;

import org.globsframework.gui.GuiTestCase;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.GlobRepositoryBuilder;
import org.globsframework.model.utils.GlobBuilder;
import org.globsframework.model.utils.GlobMatchers;
import static org.globsframework.model.utils.GlobMatchers.fieldEquals;

import java.util.Arrays;

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

    GlobGaugeView view = new GlobGaugeView(MyObject.TYPE, MyObject.ACTUAL, MyObject.TARGET,
                                           GlobMatchers.not(fieldEquals(MyObject.ID, 3)),
                                           repository, directory);
    gauge = view.getComponent();
  }

  public void testSelection() throws Exception {
    checkGauge(0d, 0d);

    selectionService.select(obj1);
    checkGauge(15.0d, 20.0d);

    selectionService.select(Arrays.asList(obj1, obj2), MyObject.TYPE);
    checkGauge(20.0d, 30.0d);

    selectionService.select(Arrays.asList(obj1, obj2, obj3), MyObject.TYPE);
    checkGauge(20.0d, 30.0d);

    selectionService.select(obj3);
    checkGauge(0d, 0d);
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
    checkGauge(0.0d, 0.0d);

    selectionService.select(obj6);
    checkGauge(4.0d, 7.0d);
  }

  private void checkGauge(double actual, double target) {
    assertEquals(actual, gauge.getActualValue());
    assertEquals(target, gauge.getTargetValue());
  }

  public static class MyObject {
    public static GlobType TYPE;

    @Key
    public static IntegerField ID;

    public static DoubleField ACTUAL;
    public static DoubleField TARGET;

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
