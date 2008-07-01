package org.globsframework.gui.splits;

public interface SplitterFactory {
  Splitter getSplitter(String name,
                       Splitter[] subSplitters,
                       SplitProperties properties
  );
}
