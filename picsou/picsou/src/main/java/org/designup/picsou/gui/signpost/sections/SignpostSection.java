package org.designup.picsou.gui.signpost.sections;

import org.designup.picsou.gui.model.Card;
import org.designup.picsou.model.SignpostSectionType;
import org.designup.picsou.utils.Lang;

public enum  SignpostSection {

  IMPORT(SignpostSectionType.IMPORT, Card.DATA, "import", "header_import.png"),
  CATEGORIZATION(SignpostSectionType.CATEGORIZATION, Card.CATEGORIZATION, "categorization", "cards/categorization.png"),
  BUDGET(SignpostSectionType.BUDGET, Card.BUDGET, "budget", "cards/budget.png");

  private SignpostSectionType type;
  private Card card;
  private String key;
  private String iconPath;

  private SignpostSection(SignpostSectionType type, Card card, String key, String iconPath) {
    this.type = type;
    this.card = card;
    this.key = key;
    this.iconPath = iconPath;
  }

  public String getLabel() {
    return getText("name");
  }

  public String getDescription() {
    return getText("description");
  }

  public String getCompletionTitle() {
    return getText("completion.title");
  }

  public String getCompletionMessage() {
    return getText("completion.message");
  }

  private String getText(String postfix) {
    return Lang.get("signpostSection." + key + "." + postfix);
  }

  public SignpostSectionType getType() {
    return type;
  }

  public Card getCard() {
    return card;
  }

  public String getIconPath() {
    return iconPath;
  }

  public String toString() {
    return key;
  }
}
