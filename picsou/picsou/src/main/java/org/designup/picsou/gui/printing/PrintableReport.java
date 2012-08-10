package org.designup.picsou.gui.printing;

import java.awt.print.PageFormat;
import java.awt.print.Pageable;

public interface PrintableReport extends Pageable {
  void init(PageFormat format);
}
