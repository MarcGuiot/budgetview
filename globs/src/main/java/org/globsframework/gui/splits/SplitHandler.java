package org.globsframework.gui.splits;

import java.awt.*;

public interface SplitHandler<T extends Component> {

  T getComponent();

  void applyStyle(String ...styleName);
}
