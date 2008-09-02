package org.globsframework.gui;

import org.globsframework.gui.splits.IconLocator;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.gui.splits.ui.UIService;
import org.globsframework.model.GlobChecker;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.model.format.Formats;
import org.globsframework.model.format.utils.DefaultDescriptionService;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;
import org.uispec4j.UISpecTestCase;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Locale;

public abstract class GuiTestCase extends UISpecTestCase {
  protected final Directory directory = new DefaultDirectory();
  protected final GlobChecker checker = new GlobChecker();
  protected final SelectionService selectionService = new SelectionService();

  protected void setUp() throws Exception {
    super.setUp();
    Locale.setDefault(Locale.ENGLISH);
    directory.add(DescriptionService.class,
                  new DefaultDescriptionService(
                    new Formats(new SimpleDateFormat("yyyy/MM/dd"),
                                new SimpleDateFormat("yyyy/MM/dd hh:mm:ss"),
                                new DecimalFormat("#.##", new DecimalFormatSymbols(Locale.US)),
                                "yes", "no")));
    directory.add(SelectionService.class, selectionService);
    directory.add(new ColorService());
    directory.add(new UIService());
    directory.add(IconLocator.class, IconLocator.NULL);
  }
}
