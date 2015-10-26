package org.designup.picsou.license.servlet;

import org.globsframework.streams.accessors.utils.ValueLongAccessor;
import org.globsframework.utils.Strings;
import org.apache.log4j.Logger;

import java.util.Map;
import java.util.HashMap;

public class VersionService {
  static Logger logger = Logger.getLogger("VersionService");
  public JarInfos jarInfos = new JarInfos();
  public JarInfos tmp;

  public void getVersion(String mail, Integer group, ValueLongAccessor jarVersion, ValueLongAccessor configVersion){
    JarInfo jarInfo = jarInfos.getInfo(mail, group);
    if (jarVersion != null){
      jarVersion.setValue(jarInfo.jarVersion);
    }
    if (configVersion != null){
      configVersion.setValue(jarInfo.configVersion);
    }
  }

  public void start() {
    tmp = new JarInfos();
  }

  public void setVersion(String mail, Integer group, long jarVersion, long configVersion) {
    JarInfo jarInfo = jarInfos.getInfo(mail, group);
    if ((jarInfo.getJarVersion() != jarVersion) || (jarInfo.getConfigVersion() != configVersion)){
      logger.info("new jar version mail = '" + mail + "' group = '"  + group + "' : jarversion = "
                  + jarVersion +  " configVersion = " + configVersion);
    }
    tmp.add(mail, group, jarVersion, configVersion);
  }

  public void complete() {
    jarInfos = tmp;
  }

  static class JarInfos {
    private Map<String, JarInfo> mailToJarInfo = new HashMap<String, JarInfo>();
    private Map<Integer, JarInfo> groupToJarInfo = new HashMap<Integer, JarInfo>();
    private JarInfo defaultJarInfo = new JarInfo(0L, 0L);

    void add(String mail, Integer group, long jarVersion, long configVersion){
      if (Strings.isNotEmpty(mail)){
        mailToJarInfo.put(mail, new JarInfo(jarVersion, configVersion));
      }
      if (group != null){
        groupToJarInfo.put(group, new JarInfo(jarVersion, configVersion));
      }
      if (mail == null && group == null){
        defaultJarInfo = new JarInfo(jarVersion, configVersion);
      }
    }

    public JarInfo getInfo(String mail, Integer group){
      JarInfo jarInfo = null;
      if (Strings.isNotEmpty(mail)){
        jarInfo = mailToJarInfo.get(mail);
      }
      if (jarInfo == null && group != null){
        jarInfo = groupToJarInfo.get(group);
      }
      if (jarInfo == null){
        jarInfo = defaultJarInfo;
      }
      return jarInfo;
    }
  }

  static public class JarInfo {
    private long jarVersion;
    private long configVersion;

    JarInfo(long jarVersion, long configVersion) {
      this.jarVersion = jarVersion;
      this.configVersion = configVersion;
    }

    public long getJarVersion() {
      return jarVersion;
    }

    public long getConfigVersion() {
      return configVersion;
    }

    public String toString() {
      return Long.toString(jarVersion);
    }
  }
}
