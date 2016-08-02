package com.budgetview.desktop.printing;

import java.awt.print.PageFormat;
import java.awt.print.Pageable;

public interface PrintableReport extends Pageable {
  void init(PageFormat format);
}
