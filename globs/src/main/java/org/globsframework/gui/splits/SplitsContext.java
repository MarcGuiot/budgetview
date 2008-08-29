package org.globsframework.gui.splits;

import org.globsframework.gui.splits.repeat.RepeatHandler;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;

public interface SplitsContext {

  void addComponent(String id, Component component);

  Component findComponent(String id);

  <T extends Component> T findOrCreateComponent(String ref, String name, Class<T> componentClass, String splitterName);

  RepeatHandler getRepeat(String name);

  void add(String name, Action action);

  Action getAction(String id);

  <T> T getService(Class<T> serviceClass);

  Class getReferenceClass();

  void addOrReplaceComponent(String id, Component component);

  void addAutoHide(Component targetComponent, String sourceComponentName);

  void addLabelFor(JLabel label, String componentName);

  void cleanUp();

  void dispose();

  String dump();
}
