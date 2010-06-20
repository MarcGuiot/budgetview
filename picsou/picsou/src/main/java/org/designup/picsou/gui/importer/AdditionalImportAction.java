package org.designup.picsou.gui.importer;

import javax.swing.*;

public interface AdditionalImportAction {

  boolean shouldApplyAction();

  String getMessage();

  String getButtonMessage();

  Action getAction();
}
