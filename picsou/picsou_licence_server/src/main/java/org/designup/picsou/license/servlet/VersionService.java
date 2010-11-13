package org.designup.picsou.license.servlet;

import org.globsframework.streams.accessors.utils.ValueLongAccessor;

public class VersionService {
  private Long jarVersion = 0L;
  private Long configVersion = 0L;

  synchronized public void getVersion(String mail, ValueLongAccessor jarVersion, ValueLongAccessor configVersion){
    if (jarVersion != null){
      jarVersion.setValue(this.jarVersion);
    }
    if (configVersion != null){
      configVersion.setValue(this.configVersion);
    }
  }

  synchronized public void setVersion(Long jarVersion, Long configVersion) {
    if (!jarVersion.equals(this.jarVersion)) {
      this.jarVersion = jarVersion;
    }
    if (!configVersion.equals(this.configVersion)) {
      this.configVersion = configVersion;
    }
  }
}
