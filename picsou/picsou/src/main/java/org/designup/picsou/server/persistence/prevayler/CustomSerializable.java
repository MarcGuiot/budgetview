package org.designup.picsou.server.persistence.prevayler;

import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedOutput;

public interface CustomSerializable {

  String getSerializationName();

  void read(SerializedInput input, Directory directory);

  void write(SerializedOutput output, Directory directory);

}
