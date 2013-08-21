package org.globsframework.gui.splits;

import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.gui.splits.repeat.RepeatHandler;
import org.globsframework.gui.splits.styles.StyleContext;
import org.globsframework.utils.Functor;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import javax.swing.event.HyperlinkListener;
import java.awt.*;

public interface SplitsContext {

  void addComponent(String id, SplitsNode<Component> component);

  SplitsNode findComponent(String id);

  <T extends Component> SplitsNode<T> findOrCreateComponent(String ref, String name, Class<T> componentClass, String splitterName);

  RepeatHandler getRepeat(String name);

  void add(String name, Action action);

  Action getAction(String id);

  void add(String name, HyperlinkListener listener);

  HyperlinkListener getHyperlinkListener(String name);

  <T> T getService(Class<T> serviceClass);

  Class getReferenceClass();

  void addOrReplaceComponent(String id, SplitsNode<Component> component);

  void addAutoHide(Component targetComponent, String sourceComponentName);

  void addLabelFor(JLabel label, String componentName);

  StyleContext getStyles();

  void cleanUp();

  void dispose();

  String dump();

  void addDisposable(Disposable disposable);

  Directory getDirectory();
}
