package org.designup.picsou.server.persistence.prevayler;

import org.crossbowlabs.globs.utils.directory.Directory;
import org.crossbowlabs.globs.utils.serialization.SerializedInput;
import org.crossbowlabs.globs.utils.serialization.SerializedOutput;

public interface CustomSerializable {

  String getSerializationName();

  void read(SerializedInput input, Directory directory);

  void write(SerializedOutput output, Directory directory);

}
