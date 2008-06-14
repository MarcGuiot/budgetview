package org.globsframework.gui.splits.splitters;

import org.globsframework.gui.splits.SplitProperties;
import org.globsframework.gui.splits.SplitsContext;
import org.globsframework.gui.splits.Splitter;
import org.globsframework.gui.splits.exceptions.SplitsException;
import org.globsframework.gui.splits.layout.ComponentStretch;

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
