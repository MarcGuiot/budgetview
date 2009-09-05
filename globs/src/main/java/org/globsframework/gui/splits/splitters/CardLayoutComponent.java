package org.globsframework.gui.splits.splitters;

import org.globsframework.gui.splits.SplitProperties;
import org.globsframework.gui.splits.SplitsContext;
import org.globsframework.gui.splits.Splitter;
import org.globsframework.gui.splits.SplitHandler;
import org.globsframework.gui.splits.impl.DefaultSplitHandler;
import org.globsframework.gui.splits.exceptions.SplitsException;
import org.globsframework.gui.splits.layout.ComponentStretch;
import org.globsframework.gui.splits.layout.Fill;
import org.globsframework.gui.splits.layout.Anchor;
import org.globsframework.gui.splits.utils.DoubleOperation;

import javax.swing.*;
import java.awt.*;

public class CardLayoutComponent extends AbstractSplitter {

  public CardLayoutComponent(SplitProperties properties, Splitter[] subSplitters) {
    super(properties, subSplitters);
  }

  protected SplitComponent createRawStretch(SplitsContext context) {
    String ref = getProperties().getString("ref");
    if (ref == null) {
      throw new SplitsException("cards components must reference a registered panel (use ref='xxx')");
    }
    SplitHandler<JPanel> splitHandler = context.findOrCreateComponent(ref, null, JPanel.class, getName());
    JPanel panel = splitHandler.getComponent();
    if (!CardLayout.class.isInstance(panel.getLayout())) {
      throw new SplitsException("Panel '" + ref + "' must use a CardLayout, preferably through a CardHandler");
    }

    double weightX = 0;
    double weightY = 0;
    for (Splitter splitter : getSubSplitters()) {
      if (!(splitter instanceof CardSplitter)) {
        throw new SplitsException("CardLayout tags can only contain <card> elements");
      }
      CardSplitter card = (CardSplitter)splitter;
      String cardName = card.getCardName();
      SplitComponent splitComponent = card.createComponentStretch(context, false);
      weightX = DoubleOperation.MAX.get(splitComponent.componentStretch.getWeightX(), weightX);
      weightY = DoubleOperation.MAX.get(splitComponent.componentStretch.getWeightY(), weightY);
      panel.add(splitComponent.componentStretch.getComponent(), cardName);
    }
    return new SplitComponent(new ComponentStretch(panel, Fill.BOTH, Anchor.CENTER, weightX, weightY), splitHandler);
  }

  public String getName() {
    return "cards";
  }
}
