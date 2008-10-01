package org.globsframework.gui.splits;

import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.gui.splits.repeat.RepeatHandler;
import org.globsframework.gui.splits.styles.StyleContext;

import javax.swing.*;
import javax.swing.event.HyperlinkListener;
import java.awt.*;

public interface SplitsContext {

  void addComponent(String id, Component component);

  Component findComponent(String id);

  <T extends Component> T findOrCreateComponent(String ref, String name, Class<T> componentClass, String splitterName);

  RepeatHandler getRepeat(String name);

  void add(String name, Action action);

  Action getAction(String id);

  void add(String name, HyperlinkListener listener);

  HyperlinkListener getHyperlinkListener(String name);

  <T> T getService(Class<T> serviceClass);

  Class getReferenceClass();

  void addOrReplaceComponent(String id, Component component);

  void addAutoHide(Component targetComponent, String sourceComponentName);

  void addLabelFor(JLabel label, String componentName);

  StyleContext getStyles();

  void cleanUp();

  void dispose();

  String dump();

  void addDisposable(Disposable disposable);
}
