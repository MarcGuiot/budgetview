package org.crossbowlabs.globs.sqlstreams;

import org.crossbowlabs.globs.model.Glob;
import org.crossbowlabs.globs.model.GlobList;
import org.crossbowlabs.globs.streams.GlobStream;
import org.crossbowlabs.globs.utils.exceptions.ItemNotFound;
import org.crossbowlabs.globs.utils.exceptions.TooManyItems;

public interface SelectQuery {
  GlobStream execute();

  GlobList executeAsGlobs();

  Glob executeUnique() throws ItemNotFound, TooManyItems;
}
