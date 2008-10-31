package org.designup.picsou.license.servlet;

public class VersionService {
  private Long jarVersion = 0L;
  private Long configVersion = 0L;

  public Long getJarVersion() {
    return jarVersion;
  }

  public Long getConfigVersion() {
    return configVersion;
  }

  public void setVersion(Long jarVersion, Long configVersion) {
    if (!jarVersion.equals(this.jarVersion)) {
      this.jarVersion = jarVersion;
    }
    if (!configVersion.equals(this.configVersion)) {
      this.configVersion = configVersion;
    }
  }
}
