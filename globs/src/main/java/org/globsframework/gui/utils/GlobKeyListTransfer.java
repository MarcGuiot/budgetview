package org.globsframework.gui.utils;

import org.globsframework.model.Key;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.List;

public class GlobKeyListTransfer implements Transferable {
  public static final DataFlavor FLAVOR = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType, "GlobKeyArray");
  private List<Key> globKeys;

  public GlobKeyListTransfer(List<Key> globKeys) {
    this.globKeys = globKeys;
  }

  public DataFlavor[] getTransferDataFlavors() {
    return new DataFlavor[]{FLAVOR};
  }

  public boolean isDataFlavorSupported(DataFlavor flavor) {
    return FLAVOR.equals(flavor);
  }

  public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
    if (!isDataFlavorSupported(flavor)) {
      throw new UnsupportedFlavorException(flavor);
    }
    return this;
  }

  public List<Key> getGlobKeys() {
    return globKeys;
  }
}
