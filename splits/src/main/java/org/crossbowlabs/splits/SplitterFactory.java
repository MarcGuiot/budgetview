package org.crossbowlabs.splits;

public interface SplitterFactory {
  Splitter getSplitter(String name,
                       Splitter[] subSplitters,
                       SplitProperties properties,
                       SplitsContext repository);
}
