package org.crossbowlabs.splits.splitters;

import org.crossbowlabs.splits.SplitProperties;
import org.crossbowlabs.splits.SplitsContext;
import org.crossbowlabs.splits.Splitter;
import org.crossbowlabs.splits.exceptions.SplitsException;
import org.crossbowlabs.splits.layout.ComponentStretch;
import org.crossbowlabs.splits.utils.DoubleOperation;

import javax.swing.*;
import java.awt.*;

public class CardLayoutComponent extends AbstractSplitter {

  public CardLayoutComponent(SplitProperties properties, Splitter[] subSplitters, SplitsContext context) {
    super(properties, subSplitters, context);
  }

  protected ComponentStretch createRawStretch() {
    String ref = getProperties().getString("ref");
    if (ref == null) {
      throw new SplitsException("cards components must reference a registered panel (use ref='xxx')");
    }
    JPanel panel = getContext().findOrCreateComponent(ref, null, JPanel.class);
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
      panel.add(card.getComponentStretch(false).getComponent(), cardName);
    }
    ComponentStretch containerStretch = createContainerStretch(panel, subSplitters, DoubleOperation.MAX);
    return containerStretch;
  }

  public String getName() {
    return "cards";
  }
}
