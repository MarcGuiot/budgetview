package org.designup.picsou.gui.components;

import javax.swing.*;
import java.awt.*;

public interface TextDisplay {

  public abstract void setToolTipText(String text);

  public abstract void setForeground(Color color);

  public abstract void setVisible(boolean visible);

  public abstract void setText(String text);

}
