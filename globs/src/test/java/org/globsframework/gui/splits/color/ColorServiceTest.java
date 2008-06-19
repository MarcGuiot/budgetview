package org.globsframework.gui.splits.color;

import org.globsframework.utils.exceptions.InvalidParameter;
import org.uispec4j.UISpecTestCase;

import javax.swing.*;
import java.awt.*;

public class ColorServiceTest extends UISpecTestCase {
  private ColorService service;

  protected void setUp() throws Exception {
    service = new ColorService();
  }

  public void testColorManagement() throws Exception {
    service.set("color.key", Color.BLUE);
    assertEquals(Color.BLUE, service.get("color.key"));
  }

  public void testColorNotFound() throws Exception {
    assertEquals(Color.RED, service.get("unknown"));
  }

  public void testNullKeysAreNotAllowed() throws Exception {
    try {
      service.get(null);
      fail();
    }
    catch (InvalidParameter e) {
      assertEquals("null key is not allowed", e.getMessage());
    }
  }

  public void testListenerIsCalledOnInitAndThenOnEachColorChange() throws Exception {
    DummyColorListener listener = new DummyColorListener();
    assertEquals(0, listener.getCallCount());

    service.addListener(listener);
    assertEquals(1, listener.getCallCount());

    service.set("key1", Color.RED);
    assertEquals(2, listener.getCallCount());
    assertEquals(Color.RED, service.get("key1"));
  }

  public void testUpdatingComponents() throws Exception {
    service.set("key1", Color.RED);
    service.set("key2", Color.BLUE);

    JButton button = new JButton();
    service.install("key1", new ForegroundColorUpdater(button));
    service.install("key2", new BackgroundColorUpdater(button));

    assertEquals(Color.RED, button.getForeground());
    assertEquals(Color.BLUE, button.getBackground());

    service.set("key1", Color.YELLOW);
    assertEquals(Color.YELLOW, button.getForeground());

    service.set("key2", Color.PINK);
    assertEquals(Color.PINK, button.getBackground());
  }

  public void testDefaultColorSet() throws Exception {
    System.setProperty(ColorService.DEFAULT_COLOR_SET, "set2.colors");
    checkDefaultColorSet("set2.colors");

    System.setProperty(ColorService.DEFAULT_COLOR_SET, "unknown");
    checkDefaultColorSet("set1.colors");

    System.clearProperty(ColorService.DEFAULT_COLOR_SET);
    checkDefaultColorSet("set1.colors");
  }

  private void checkDefaultColorSet(String name) {
    service = new ColorService(getClass(), "set1.colors", "set2.colors");
    assertEquals(name, service.getCurrentColorSetName());
  }

  private static class DummyColorListener implements ColorChangeListener {
    private int callCount;

    public int getCallCount() {
      return callCount;
    }

    public void colorsChanged(ColorLocator colorLocator) {
      callCount++;
    }
  }
}
