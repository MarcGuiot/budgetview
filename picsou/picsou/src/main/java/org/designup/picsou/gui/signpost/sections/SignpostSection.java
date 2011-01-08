package org.designup.picsou.gui.signpost.sections;

import org.designup.picsou.model.SignpostSectionType;
import org.designup.picsou.utils.Lang;

public enum SignpostSection {

  IMPORT(SignpostSectionType.IMPORT, "import"),
  CATEGORIZATION(SignpostSectionType.CATEGORIZATION, "categorization"),
  BUDGET(SignpostSectionType.BUDGET, "budget");

  private SignpostSectionType type;
  private String key;

  private SignpostSection(SignpostSectionType type, String key) {
    this.type = type;
    this.key = key;
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

  public String toString() {
    return key;
  }
}
