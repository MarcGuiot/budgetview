package org.globsframework.gui.splits.splitters;

import org.globsframework.gui.splits.SplitProperties;
import org.globsframework.gui.splits.SplitsContext;
import org.globsframework.gui.splits.Splitter;
import org.globsframework.gui.splits.exceptions.SplitsException;
import org.globsframework.gui.splits.layout.ComponentStretch;
import org.globsframework.gui.splits.utils.DoubleOperation;

import javax.swing.*;
import java.awt.*;

public class CardLayoutComponent extends AbstractSplitter {

  public CardLayoutComponent(SplitProperties properties, Splitter[] subSplitters) {
    super(properties, subSplitters);
  }

  protected ComponentStretch createRawStretch(SplitsContext context) {
    String ref = getProperties().getString("ref");
    if (ref == null) {
      throw new SplitsException("cards components must reference a registered panel (use ref='xxx')");
    }
    JPanel panel = context.findOrCreateComponent(ref, null, JPanel.class);
    if (!CardLayout.class.isInstance(panel.getLayout())) {
      throw new SplitsException("Panel '" + ref + "' must use a CardLayout, preferably through a CardHandler");
    }

    Splitter[] subSplitters = getSubSplitters();
    for (Splitter splitter : subSplitters) {
      if (!(splitter instanceof CardSplitter)) {
        throw new SplitsException("CardLayout tags can only contain <card> elements");
      }
      CardSplitter card = (CardSplitter)splitter;
      String cardName = card.getCardName();
      panel.add(card.getComponentStretch(context, false).getComponent(), cardName);
    }
    return createContainerStretch(panel, DoubleOperation.MAX, context);
  }

  public String getName() {
    return "cards";
  }
}
