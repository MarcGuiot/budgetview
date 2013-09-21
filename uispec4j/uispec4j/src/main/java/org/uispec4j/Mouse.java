package org.uispec4j;

import static org.uispec4j.Key.Modifier;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

/**
 * Utility for simulating mouse inputs.
 */
public class Mouse {

  private Mouse() {
  }

  /**
   * Clicks in the center of a UIComponent.
   */
  public static void click(UIComponent uiComponent) {
    doClick(uiComponent.getAwtComponent(), 1);
  }

  /**
   * Trigger that clicks in the center of a UIComponent.
   */
  public static Trigger triggerClick(final UIComponent uiComponent) {
    return new Trigger() {
      public void run() throws Exception {
        Mouse.click(uiComponent);
      }
    };
  }

  /**
   * Right-clicks in the center of a UIComponent.
   */
  public static void rightClick(UIComponent uiComponent) {
    rightClick(uiComponent, Modifier.NONE);
  }

  /**
   * Right-clicks in the center of a UIComponent.
   */
  public static void rightClick(UIComponent uiComponent, Key.Modifier keyModifier) {
    Mouse.doClickInRectangle(uiComponent, uiComponent.getAwtComponent().getBounds(), true, keyModifier);
  }

  /**
   * Clicks in the center of a UIComponent.
   */
  public static void click(UIComponent uiComponent, Key.Modifier keyModifier) {
    Component awtComponent = uiComponent.getAwtComponent();
    doClickInRectangle(awtComponent, awtComponent.getBounds(), false, keyModifier);
  }

  /**
   * Double clicks in the center of a UIComponent.
   */
  public static void doubleClick(UIComponent uiComponent) {
    doClick(uiComponent.getAwtComponent(), 2);
  }

  /**
   * Clicks in a given area of a UIComponent.
   */
  public static void doClickInRectangle(UIComponent uiComponent,
                                        Rectangle rect,
                                        boolean useRightClick,
                                        Key.Modifier keyModifier) {
    doClickInRectangle(uiComponent.getAwtComponent(), rect, useRightClick, keyModifier);
  }

  /**
   * Clicks in a given area of a Swing component.
   */
  public static void doClickInRectangle(Component component,
                                        Rectangle rect,
                                        boolean useRightClick,
                                        Key.Modifier keyModifier) {
    doClickInRectangle(component, rect, useRightClick, keyModifier, 1);
  }

  /**
   * Double clicks in a given area of a Swing component.
   */
  public static void doDoubleClickInRectangle(Component component, Rectangle rect) {
    doClickInRectangle(component, rect, false, Modifier.NONE, 2);
  }

  private static void doClickInRectangle(Component component, Rectangle rect, boolean useRightClick, Key.Modifier keyModifier, int nbClicks) {
    final int x = rect.x + (rect.width / 2);
    final int y = rect.y + (rect.height / 2);
    int modifiers = useRightClick ? MouseEvent.BUTTON3_MASK : MouseEvent.BUTTON1_MASK;
    modifiers |= keyModifier.getCode();
    component.dispatchEvent(new MouseEvent(component, MouseEvent.MOUSE_PRESSED, 1, modifiers, x, y, nbClicks, false));
    component.dispatchEvent(new MouseEvent(component, MouseEvent.MOUSE_RELEASED, 1, modifiers, x, y, nbClicks, useRightClick));
    component.dispatchEvent(new MouseEvent(component, MouseEvent.MOUSE_CLICKED, 1, modifiers, x, y, nbClicks, false));
  }

  public static void wheel(UIComponent component, int clicks){
    wheel(component, 0, 0, clicks);
  }


  public static void wheel(UIComponent component, int x, int y, int clicks){
    Component awtComponent = component.getAwtComponent();
    awtComponent.dispatchEvent(
      new MouseWheelEvent(awtComponent, MouseEvent.MOUSE_WHEEL, 1, Modifier.NONE.getCode(), x, y,1, false,
        MouseWheelEvent.WHEEL_UNIT_SCROLL, 1, clicks));
  }

  public static void pressed(UIComponent component, int x, int y) {
    sendEvent(component.getAwtComponent(), false, Modifier.NONE, x, y, MouseEvent.MOUSE_PRESSED);
  }

  public static void released(UIComponent component, int x, int y) {
    sendEvent(component.getAwtComponent(), false, Modifier.NONE, x, y, MouseEvent.MOUSE_RELEASED);
  }

  public static void drag(UIComponent component, int x, int y) {
    sendEvent(component.getAwtComponent(), false, Modifier.NONE, x, y, MouseEvent.MOUSE_DRAGGED);
  }

  public static void pressed(UIComponent component, boolean useRightClick, Modifier keyModifier, int x, int y) {
    sendEvent(component.getAwtComponent(), useRightClick, keyModifier, x, y, MouseEvent.MOUSE_PRESSED);
  }

  public static void released(UIComponent component, boolean useRightClick, Modifier keyModifier, int x, int y) {
    sendEvent(component.getAwtComponent(), useRightClick, keyModifier, x, y, MouseEvent.MOUSE_RELEASED);
  }

  public static void drag(UIComponent component, boolean useRightClick, Modifier keyModifier, int x, int y) {
    sendEvent(component.getAwtComponent(), useRightClick, keyModifier, x, y, MouseEvent.MOUSE_DRAGGED);
  }

  public static void enter(Component component, int x, int y) {
    Mouse.sendEvent(component, x, y, MouseEvent.MOUSE_ENTERED);
  }

  public static void move(Component component, int x, int y) {
    Mouse.sendEvent(component, x, y, MouseEvent.MOUSE_MOVED);
  }

  public static void exit(Component component, int x, int y) {
    sendEvent(component, x, y, MouseEvent.MOUSE_EXITED);
  }

  public static void pressed(Component component, int x, int y) {
    sendEvent(component, false, Modifier.NONE, x, y, MouseEvent.MOUSE_PRESSED);
  }

  public static void released(Component component, int x, int y) {
    sendEvent(component, false, Modifier.NONE, x, y, MouseEvent.MOUSE_RELEASED);
  }

  public static void drag(Component component, int x, int y) {
    sendEvent(component, false, Modifier.NONE, x, y, MouseEvent.MOUSE_DRAGGED);
  }

  public static void pressed(Component component, boolean useRightClick, Modifier keyModifier, int x, int y) {
    sendEvent(component, useRightClick, keyModifier, x, y, MouseEvent.MOUSE_PRESSED);
  }

  public static void released(Component component, boolean useRightClick, Modifier keyModifier, int x, int y) {
    sendEvent(component, useRightClick, keyModifier, x, y, MouseEvent.MOUSE_RELEASED);
  }

  public static void drag(Component component, boolean useRightClick, Modifier keyModifier, int x, int y) {
    sendEvent(component, useRightClick, keyModifier, x, y, MouseEvent.MOUSE_DRAGGED);
  }

  private static void sendEvent(Component component, int x, int y, int eventType) {
    component.dispatchEvent(new MouseEvent(component, eventType, 1, 0, x, y, 0, false));
  }

  private static void sendEvent(Component component, boolean useRightClick, Modifier keyModifier, int x, int y, int eventType) {
    int modifiers = useRightClick ? MouseEvent.BUTTON3_MASK : MouseEvent.BUTTON1_MASK;
    modifiers |= keyModifier.getCode();
    component.dispatchEvent(new MouseEvent(component, eventType, 1, modifiers, x, y, 1, false));
  }

  private static void doClick(Component component, int clickCount) {
    doClickInRectangle(component, new Rectangle(), false, Modifier.NONE, clickCount);
  }
}
