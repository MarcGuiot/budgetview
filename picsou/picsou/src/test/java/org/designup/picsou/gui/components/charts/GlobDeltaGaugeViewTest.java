package org.designup.picsou.gui.components.charts;

import org.designup.picsou.model.BudgetArea;
import org.globsframework.gui.GuiTestCase;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.GlobRepositoryBuilder;

import static org.globsframework.model.FieldValue.value;

public class GlobDeltaGaugeViewTest extends GuiTestCase {
  private GlobRepository repository;
  private DeltaGauge gauge;
  private org.globsframework.model.Key key;

  protected void setUp() throws Exception {
    super.setUp();
    repository = GlobRepositoryBuilder.createEmpty();
    key = repository.create(MyObject.TYPE).getKey();

    GlobDeltaGaugeView view = new GlobDeltaGaugeView(key, BudgetArea.VARIABLE,
                                                     MyObject.PREVIOUS_VALUE, MyObject.NEW_VALUE,
                                                     MyObject.PREVIOUS_MONTH, MyObject.NEW_MONTH,
                                                     repository, directory);
    gauge = view.getComponent();
  }

  public void test() throws Exception {

    checkGauge(null, null, null, null,
               0.0, "");

    checkGauge(null, null, 201107, 201108,
               0.0, "The amount is zero in july 2011 and august 2011");

    // Nothing before
    checkGauge(null, 100.0, 201107, 201108,
               1.0, "This envelope was not used in july 2011");
    checkGauge(0.0, 100.0, 201107, 201108,
               1.0, "This envelope was not used in july 2011");

    // Flat
    checkGauge(100.0, 100.0, 201107, 201108,
               0.0, "The amount is the same as in july 2011");

    // Sign change
    checkGauge(-100.0, 150.0, 201107, 201108,
               1.0, "The amount was negative (100.00) in july 2011");
    checkGauge(100.0, -150.0, 201107, 201108,
               -1.0, "The amount was positive (+100.00) in july 2011");

    // Same-sign increase
    checkGauge(100.0, 150.0, 201107, 201108,
               0.5, "The amount is increasing - it was +100.00 in july 2011");
    checkGauge(-100.0, -150.0, 201107, 201108,
               0.5, "The amount is increasing - it was 100.00 in july 2011");

    // Same-sign decrease
    checkGauge(100.0, 50.0, 201107, 201108,
               -0.5, "The amount is decreasing - it was +100.00 in july 2011");
    checkGauge(-100.0, -50.0, 201107, 201108,
               -0.5, "The amount is decreasing - it was 100.00 in july 2011");

    // To zero
    checkGauge(100.0, 0.0, 201107, 201108,
               -1.0, "The amount was +100.00 in july 2011, and it is set to zero in august 2011");
    checkGauge(-100.0, 0.0, 201107, 201108,
               -1.0, "The amount was 100.00 in july 2011, and it is set to zero in august 2011");
    checkGauge(0.0, 0.0, 201107, 201108,
               0.0, "The amount is zero in july 2011 and august 2011");
  }

  private void checkGauge(Double previousValue, Double newValue,
                          Integer previousMonthId, Integer newMonthId,
                          double ratio, String tooltip) {
    repository.update(key,
                      value(MyObject.PREVIOUS_VALUE, previousValue),
                      value(MyObject.NEW_VALUE, newValue),
                      value(MyObject.PREVIOUS_MONTH, previousMonthId),
                      value(MyObject.NEW_MONTH, newMonthId));
    assertEquals(ratio, gauge.getRatio());
    assertEquals(tooltip, org.uispec4j.utils.Utils.cleanupHtml(gauge.getToolTipText()));
  }

  public static class MyObject {
    public static GlobType TYPE;

    @Key
    public static IntegerField ID;

    public static DoubleField PREVIOUS_VALUE;
    public static DoubleField NEW_VALUE;

    public static IntegerField PREVIOUS_MONTH;
    public static IntegerField NEW_MONTH;

    static {
      GlobTypeLoader.init(MyObject.class);
    }
  }
}
