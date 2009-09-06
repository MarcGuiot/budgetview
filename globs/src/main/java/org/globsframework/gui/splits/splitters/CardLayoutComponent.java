package org.globsframework.gui.splits.splitters;

import org.globsframework.gui.splits.SplitProperties;
import org.globsframework.gui.splits.SplitsContext;
import org.globsframework.gui.splits.Splitter;
import org.globsframework.gui.splits.SplitsNode;
import org.globsframework.gui.splits.exceptions.SplitsException;
import org.globsframework.gui.splits.layout.ComponentConstraints;
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
    SplitsNode<JPanel> splitsNode = context.findOrCreateComponent(ref, null, JPanel.class, getName());
    JPanel panel = splitsNode.getComponent();
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
      weightX = DoubleOperation.MAX.get(splitComponent.componentConstraints.getWeightX(), weightX);
      weightY = DoubleOperation.MAX.get(splitComponent.componentConstraints.getWeightY(), weightY);
      panel.add(splitComponent.componentConstraints.getComponent(), cardName);
    }
    return new SplitComponent(new ComponentConstraints(panel, Fill.BOTH, Anchor.CENTER, weightX, weightY), splitsNode);
  }

  public String getName() {
    return "cards";
  }
}
