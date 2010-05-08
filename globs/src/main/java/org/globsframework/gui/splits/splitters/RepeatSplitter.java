package org.globsframework.gui.splits.splitters;

import org.globsframework.gui.splits.SplitProperties;
import org.globsframework.gui.splits.SplitsContext;
import org.globsframework.gui.splits.Splitter;
import org.globsframework.gui.splits.exceptions.SplitsException;
import org.globsframework.gui.splits.layout.WrappedColumnLayout;
import org.globsframework.gui.splits.repeat.*;
import org.globsframework.utils.Strings;
import org.globsframework.utils.exceptions.ItemNotFound;
import org.globsframework.utils.exceptions.InvalidParameter;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class RepeatSplitter extends AbstractSplitter {
  private Splitter[] headerSplitters;
  private Splitter[] contentSplitters;
  private Splitter[] footerSplitters;
  private String ref;
  private RepeatLayout layout;

  public RepeatSplitter(SplitProperties properties, Splitter[] subSplitters) {
    super(properties, null);
    ref = properties.get("ref");
    if (ref == null) {
      throw new SplitsException("Repeat items must have a 'ref' attribute");
    }

    initSplitters(subSplitters);

    layout = getLayout(properties.get("layout"), ref);
    layout.checkHeader(headerSplitters, ref);
    layout.checkContent(contentSplitters, ref);
  }

  private void initSplitters(Splitter[] subSplitters) {
    java.util.List<Splitter> splitters = new ArrayList<Splitter>();
    for (Splitter subSplitter : subSplitters) {
      if (subSplitter instanceof Header) {
        if (headerSplitters != null) {
          throw new InvalidParameter("Only one Header is accepted for a given repeat");
        }
        this.headerSplitters  = ((Header)subSplitter).getHeaderSplitters();
      }
      else if (subSplitter instanceof Footer){
        if (footerSplitters != null) {
          throw new InvalidParameter("Only one Footer is accepted for a given repeat");
        }
        this.footerSplitters  = ((Footer)subSplitter).getFooterSplitters();
      }
      else {
        splitters.add(subSplitter);
      }
    }
    this.contentSplitters = splitters.toArray(new Splitter[splitters.size()]);
  }

  protected SplitComponent createRawStretch(SplitsContext context) {

    boolean autoHideIfEmpty = Boolean.TRUE.equals(properties.getBoolean("autoHideIfEmpty"));

    RepeatHandler repeatHandler = context.getRepeat(ref);
    if (repeatHandler == null) {
      throw new ItemNotFound("Repeat '" + ref + "' not declared");
    }
    RepeatPanel repeatPanel =
      new RepeatPanel(ref, repeatHandler, layout, autoHideIfEmpty, headerSplitters, contentSplitters, footerSplitters, context);
    return repeatPanel.getSplitComponent();
  }

  private RepeatLayout getLayout(String layoutProperty, String ref) {

    if (Strings.isNullOrEmpty(layoutProperty) || "column".equalsIgnoreCase(layoutProperty)) {
      return new ColumnRepeatLayout() {
        protected LayoutManager getLayout(JPanel panel) {
          return new BoxLayout(panel, BoxLayout.Y_AXIS);
        }
      };
    }
    else if ("row".equalsIgnoreCase(layoutProperty)) {
      return new ColumnRepeatLayout() {
        protected LayoutManager getLayout(JPanel panel) {
          return new BoxLayout(panel, BoxLayout.X_AXIS);
        }
      };
    }
    else if ("wrappedColumn".equalsIgnoreCase(layoutProperty)) {
      return new ColumnRepeatLayout() {
        protected LayoutManager getLayout(JPanel panel) {
          return new WrappedColumnLayout();
        }
      };
    }
    else if ("wrappedRow".equalsIgnoreCase(layoutProperty)) {
      return new ColumnRepeatLayout() {
        protected LayoutManager getLayout(JPanel panel) {
          return new FlowLayout(FlowLayout.LEFT, 10, 0);
        }
      };
    }
    else if ("verticalGrid".equalsIgnoreCase(layoutProperty)) {
      return new GridRepeatLayout(GridRepeatLayout.Direction.VERTICAL, properties.getInt("gridWrapLimit"));
    }
    else if ("horizontalGrid".equalsIgnoreCase(layoutProperty)) {
      return new GridRepeatLayout(GridRepeatLayout.Direction.HORIZONTAL, properties.getInt("gridWrapLimit"));
    }
    throw new SplitsException("Unknown layout type '" + layoutProperty + "' for repeat '" + ref +
                              "' - use one of [column|verticalGrid]");
  }

  public String getName() {
    return ref;
  }

  protected String[] getExcludedParameters() {
    return new String[]{"layout", "gridWrapLimit", "autoHideIfEmpty"};
  }
}
