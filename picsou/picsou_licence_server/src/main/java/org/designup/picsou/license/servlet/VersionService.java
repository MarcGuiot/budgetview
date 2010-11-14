package org.designup.picsou.license.servlet;

import org.globsframework.streams.accessors.utils.ValueLongAccessor;
import org.apache.log4j.Logger;

public class VersionService {
  static Logger logger = Logger.getLogger("VersionService");
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
      logger.info("new jar version : " + this.jarVersion);
    }
    if (!configVersion.equals(this.configVersion)) {
      this.configVersion = configVersion;
      logger.info("new config version : " + this.configVersion);
    }
  }
}
