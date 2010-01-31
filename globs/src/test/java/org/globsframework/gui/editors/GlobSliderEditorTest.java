package org.globsframework.gui.editors;

import org.globsframework.gui.utils.GuiComponentTestCase;
import org.globsframework.metamodel.DummyObject;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepositoryBuilder;
import org.globsframework.model.utils.GlobBuilder;

import javax.swing.*;

public class GlobSliderEditorTest extends GuiComponentTestCase {
  private JSlider slider;

  protected void setUp() throws Exception {
    super.setUp();
    repository = GlobRepositoryBuilder.createEmpty();
  }

  private void init(DummySliderAdapter adjuster) {
    GlobSliderEditor editor = GlobSliderEditor.init(DummyObject.VALUE, repository, directory, adjuster);
    slider = editor.getComponent();
  }

  public void test() throws Exception {

    init(new DummySliderAdapter());

    assertFalse(slider.isEnabled());
    assertEquals(-200, slider.getMinimum());
    assertEquals(0, slider.getValue());
    assertEquals(200, slider.getMaximum());

    Glob glob1 = repository.create(key1);
    selectionService.select(glob1);
    assertTrue(slider.isEnabled());
    assertEquals(0, slider.getValue());
    assertNull(glob1.get(DummyObject.VALUE));
    assertEquals(-200, slider.getMinimum());
    assertEquals(200, slider.getMaximum());

    slider.setValue(60);
    assertTrue(slider.isEnabled());
    assertEquals(-200, slider.getMinimum());
    assertEquals(200, slider.getMaximum());
    assertEquals(60.0, glob1.get(DummyObject.VALUE));

    repository.update(key1, DummyObject.VALUE, 40.0);
    assertTrue(slider.isEnabled());
    assertEquals(0, slider.getMinimum());
    assertEquals(40, slider.getValue());
    assertEquals(80, slider.getMaximum());

    selectionService.clear(DummyObject.TYPE);
    assertFalse(slider.isEnabled());
    assertEquals(0, slider.getMinimum());
    assertEquals(0, slider.getValue());
    assertEquals(80, slider.getMaximum());

    repository.update(key1, DummyObject.VALUE, 10.0);

    selectionService.select(glob1);
    assertTrue(slider.isEnabled());
    assertEquals(0, slider.getMinimum());
    assertEquals(10, slider.getValue());
    assertEquals(20, slider.getMaximum());

    repository.update(key1, DummyObject.VALUE, -30.0);
    assertTrue(slider.isEnabled());
    assertEquals(-60, slider.getMinimum());
    assertEquals(-30, slider.getValue());
    assertEquals(0, slider.getMaximum());
  }

  public void testValueConversion() throws Exception {
    init(new DummySliderAdapter(true));

    Glob glob1 = repository.create(key1, value(DummyObject.VALUE, -40.0));
    selectionService.select(glob1);
    assertTrue(slider.isEnabled());
    assertEquals(0, slider.getMinimum());
    assertEquals(40, slider.getValue());
    assertEquals(80, slider.getMaximum());
    
    slider.setValue(60);
    assertEquals(60.0, glob1.get(DummyObject.VALUE));
  }

  public void testMultiSelection() throws Exception {
    init(new DummySliderAdapter());

    Glob glob1 = repository.create(key1, value(DummyObject.VALUE, 40.0));
    Glob glob2 = repository.create(key2, value(DummyObject.VALUE, 40.0));

    selectionService.select(new GlobList(glob1, glob2), DummyObject.TYPE);
    assertTrue(slider.isEnabled());
    assertEquals(0, slider.getMinimum());
    assertEquals(40, slider.getValue());
    assertEquals(80, slider.getMaximum());

    slider.setValue(30);
    assertEquals(30.0, glob1.get(DummyObject.VALUE));
    assertEquals(30.0, glob2.get(DummyObject.VALUE));
  }

  public void testDeletingTheSelection() throws Exception {
    init(new DummySliderAdapter());

    Glob glob1 = repository.create(key1, value(DummyObject.VALUE, 50.0));
    Glob glob2 = repository.create(key2, value(DummyObject.VALUE, 50.0));

    selectionService.select(new GlobList(glob1, glob2), DummyObject.TYPE);
    assertEquals(50, slider.getValue());

    repository.delete(key1);
    assertEquals(50, slider.getValue());

    slider.setValue(60);
    assertEquals(60.0, glob2.get(DummyObject.VALUE));

    repository.delete(key2);
    assertEquals(0, slider.getValue());
    assertFalse(slider.isEnabled());
  }

  public void testReset() throws Exception {
    init(new DummySliderAdapter());

    Glob glob1 = repository.create(key1, value(DummyObject.VALUE, 50.0));
    Glob glob2 = repository.create(key2, value(DummyObject.VALUE, 50.0));

    selectionService.select(new GlobList(glob1, glob2), DummyObject.TYPE);
    assertEquals(50, slider.getValue());

    Glob newGlob1 = GlobBuilder.create(DummyObject.TYPE,
                                       value(DummyObject.ID, key1.get(DummyObject.ID)),
                                       value(DummyObject.VALUE, 60.0));

    repository.reset(new GlobList(newGlob1), DummyObject.TYPE);
    assertFalse(slider.isEnabled());
    assertEquals(0, slider.getValue());

    selectionService.select(newGlob1);
    assertTrue(slider.isEnabled());
    assertEquals(60, slider.getValue());
  }

  private static class DummySliderAdapter implements GlobSliderAdapter {

    private boolean absoluteValue;

    private DummySliderAdapter() {
      this(false);
    }

    private DummySliderAdapter(boolean absoluteValue) {
      this.absoluteValue = absoluteValue;
    }

    public void init(JSlider slider) {
      slider.setMinimum(-200);
      slider.setValue(0);
      slider.setMaximum(200);
    }

    public void setSliderValue(Double value, JSlider slider, GlobList selection) {
      if (value == null) {
        slider.setValue(0);
        return;
      }

      int intValue = value.intValue();
      if (absoluteValue) {
        intValue = Math.abs(intValue);
      }
      if (intValue < 0) {
        slider.setMinimum(intValue * 2);
        slider.setValue(intValue);
        slider.setMaximum(0);
      }
      else {
        slider.setMinimum(0);
        slider.setValue(intValue);
        slider.setMaximum(intValue * 2);
      }
    }

    public Double convertToGlobsValue(int sliderValue) {
      return (double)Math.abs(sliderValue);
    }
  }
}
