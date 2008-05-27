package org.crossbowlabs.splits.splitters;

import org.crossbowlabs.splits.SplitProperties;
import org.crossbowlabs.splits.SplitsContext;
import org.crossbowlabs.splits.Splitter;
import org.crossbowlabs.splits.exceptions.SplitsException;
import org.crossbowlabs.splits.layout.ComponentStretch;

import java.awt.*;

public class CardSplitter extends AbstractSplitter {
  private String cardName;

  public CardSplitter(SplitsContext context, SplitProperties properties, Splitter[] subSplitters) {
    super(properties, subSplitters, context);
    this.cardName = properties.get("name");
    if (cardName == null) {
      throw new SplitsException("Card items must have a 'name' attribute");
    }
    if (subSplitters.length != 1) {
      throw new SplitsException("Card component '" + cardName + "' must have exactly one subcomponent");
    }
  }

  public String getName() {
    return "card";
  }

  public String getCardName() {
    return cardName;
  }

  protected String[] getExcludedParameters() {
    return new String[]{"name"};
  }

  protected ComponentStretch createRawStretch() {
    Splitter splitter = getSubSplitters()[0];
    return splitter.getComponentStretch(true);
  }

  public Component getComponent() {
    return getComponentStretch(true).getComponent();
  }
}
