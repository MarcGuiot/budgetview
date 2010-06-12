package org.designup.picsou.gui.importer;

import javax.swing.*;

public interface AdditionalImportPanel {

  boolean shouldBeDisplayed(boolean showErrors);

  JPanel getPanel();
}
