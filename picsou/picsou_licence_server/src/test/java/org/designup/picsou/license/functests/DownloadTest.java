package org.designup.picsou.license.functests;

import org.designup.picsou.functests.checkers.*;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.gui.PicsouApplication;
import org.designup.picsou.gui.TimeService;
import org.designup.picsou.gui.config.ConfigService;
import org.designup.picsou.license.LicenseTestCase;
import org.designup.picsou.license.model.SoftwareInfo;
import org.designup.picsou.model.TransactionType;
import org.globsframework.sqlstreams.SqlConnection;
import org.globsframework.utils.Dates;
import org.objectweb.asm.*;
import org.objectweb.asm.util.ASMifierClassVisitor;
import org.uispec4j.Trigger;
import org.uispec4j.Window;
import org.uispec4j.interception.WindowInterceptor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

public class DownloadTest extends LicenseTestCase {
  private Window window;
  private PicsouApplication picsouApplication;

  protected void setUp() throws Exception {
    super.setUp();
    System.setProperty(PicsouApplication.IS_DATA_IN_MEMORY, "false");
    TimeService.setCurrentDate(Dates.parseMonth("2008/07"));
  }

  protected void tearDown() throws Exception {
    super.tearDown();
    System.setProperty(PicsouApplication.DELETE_LOCAL_PREVAYLER_PROPERTY, "true");
    if (window != null) {
      window.dispose();
    }
    window = null;
    if (picsouApplication != null) {
      picsouApplication.shutdown();
    }
    picsouApplication = null;
  }

  private void startPicsou() {
    window = WindowInterceptor.run(new Trigger() {
      public void run() throws Exception {
        picsouApplication = new PicsouApplication();
        picsouApplication.run();
      }
    });
  }

  public void testJarIsSent() throws Exception {
    SqlConnection connection = getSqlConnection();
    connection.getCreateBuilder(SoftwareInfo.TYPE)
      .set(SoftwareInfo.LATEST_JAR_VERSION, PicsouApplication.JAR_VERSION + 1L)
      .set(SoftwareInfo.LATEST_CONFIG_VERSION, PicsouApplication.BANK_CONFIG_VERSION + 1L)
      .getRequest().run();
    connection.commitAndClose();
    startServers();
    final String jarName = ConfigService.generatePicsouJarName(PicsouApplication.JAR_VERSION + 1L);
    final String configJarName = ConfigService.generateConfigJarName(PicsouApplication.BANK_CONFIG_VERSION + 1L);
    byte[] content = generateConfigContent();
    LicenseTestCase.Retr retr = setFtpReply(jarName, "jar content", configJarName, content);
    startPicsou();
    retr.assertOk();
    LoginChecker loginChecker = new LoginChecker(window);
    loginChecker.logNewUser("user", "passw@rd");
    VersionInfoChecker versionInfo = new VersionInfoChecker(window);
    versionInfo.checkNewVersion();

    OfxBuilder builder = OfxBuilder.init(this);
    builder.addBankAccount(4321, -2, "1111", 123.3, "10/09/2008");
    builder.addTransaction("2008/09/10", -100., "STUPID HEADER blabla");
    String ofxFile = builder.save();
    OperationChecker operation = new OperationChecker(window);
    operation.importOfxFile(ofxFile);
    ViewSelectionChecker views = new ViewSelectionChecker(window);
    views.selectData();
    TransactionChecker transaction = new TransactionChecker(window);
    transaction.initContent()
      .add("10/09/2008", TransactionType.PRELEVEMENT, "GOOD HEADER", "", -234.00)
      .check();
    String path = PicsouApplication.getDataPath();
    File pathToJar = new File(path + "/jars");
    String[] jars = pathToJar.list(new FilenameFilter() {
      public boolean accept(File dir, String name) {
        return name.equals(jarName);
      }
    });
    assertTrue(jars.length == 1);
    File pathToConfig = new File(path + "/configs");
    String[] configs = pathToConfig.list(new FilenameFilter() {
      public boolean accept(File dir, String name) {
        return name.equals(configJarName);
      }
    });
    assertTrue(configs.length == 1);
  }


  private byte[] generateConfigContent() throws Exception {
    File tempFile = File.createTempFile("config", ".jar");
    tempFile.deleteOnExit();
    JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(tempFile));
    jarOutputStream.putNextEntry(new ZipEntry("banks/bankList.txt"));
    jarOutputStream.write("picsouBank.xml\n".getBytes());
    jarOutputStream.putNextEntry(new ZipEntry("banks/picsouBank.xml"));
    jarOutputStream.write(("<globs>\n" +
                           "  <bank name=\"picsouBank\" downloadUrl=\"\" id='-2'>\n" +
                           "    <bankEntity id=\"10807\"/> \n" +
                           "    <bankEntity id=\"4321\"/> \n" +
                           "    <transactionMatcher ofxName=\"STUPID HEADER .*\"\n" +
                           "                        transactionTypeName=\"virement\" " +
                           "                        label=\"GOOD HEADER\"/>" +
                           "  </bank>\n" +
                           "</globs>\n").getBytes());
    jarOutputStream.putNextEntry(new ZipEntry("org/designup/picsou/license/functests/DummyBankPlugin.class"));
    DummyBankPluginDump bankPluginDump = new DummyBankPluginDump();
    jarOutputStream.write(bankPluginDump.dump());
    jarOutputStream.close();
    byte content[] = new byte[(int)tempFile.length()];
    FileInputStream inputStream = new FileInputStream(tempFile);
    inputStream.read(content);
    inputStream.close();
    return content;
  }

  public static void main(String[] args) throws Exception {
    ASMifierClassVisitor.main(new String[]{DummyBankPlugin.class.getName()});
  }

  public static class DummyBankPluginDump implements Opcodes {

    public byte[] dump() throws Exception {

      ClassWriter cw = new ClassWriter(0);
      FieldVisitor fv;
      MethodVisitor mv;
      AnnotationVisitor av0;

      cw.visit(V1_5, ACC_PUBLIC + ACC_SUPER, "org/designup/picsou/license/functests/DummyBankPlugin", null, "java/lang/Object", new String[]{"org/designup/picsou/bank/BankPlugin"});

      {
        mv = cw.visitMethod(ACC_PUBLIC, "<init>", "(Lorg/globsframework/model/GlobRepository;Lorg/globsframework/utils/directory/Directory;)V", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
        mv.visitVarInsn(ALOAD, 2);
        mv.visitLdcInsn(Type.getType("Lorg/designup/picsou/bank/BankPluginService;"));
        mv.visitMethodInsn(INVOKEINTERFACE, "org/globsframework/utils/directory/Directory", "get", "(Ljava/lang/Class;)Ljava/lang/Object;");
        mv.visitTypeInsn(CHECKCAST, "org/designup/picsou/bank/BankPluginService");
        mv.visitVarInsn(ASTORE, 3);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitFieldInsn(GETSTATIC, "org/designup/picsou/model/BankEntity", "TYPE", "Lorg/globsframework/metamodel/GlobType;");
        mv.visitIntInsn(SIPUSH, 4321);
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;");
        mv.visitMethodInsn(INVOKESTATIC, "org/globsframework/model/Key", "create", "(Lorg/globsframework/metamodel/GlobType;Ljava/lang/Object;)Lorg/globsframework/model/Key;");
        mv.visitMethodInsn(INVOKEINTERFACE, "org/globsframework/model/GlobRepository", "get", "(Lorg/globsframework/model/Key;)Lorg/globsframework/model/Glob;");
        mv.visitVarInsn(ASTORE, 4);
        mv.visitVarInsn(ALOAD, 3);
        mv.visitVarInsn(ALOAD, 4);
        mv.visitFieldInsn(GETSTATIC, "org/designup/picsou/model/BankEntity", "BANK", "Lorg/globsframework/metamodel/fields/LinkField;");
        mv.visitMethodInsn(INVOKEINTERFACE, "org/globsframework/model/Glob", "get", "(Lorg/globsframework/metamodel/fields/LinkField;)Ljava/lang/Integer;");
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKEVIRTUAL, "org/designup/picsou/bank/BankPluginService", "add", "(Ljava/lang/Integer;Lorg/designup/picsou/bank/BankPlugin;)V");
        mv.visitInsn(RETURN);
        mv.visitMaxs(3, 5);
        mv.visitEnd();
      }
      {
        mv = cw.visitMethod(ACC_PUBLIC, "apply", "(Lorg/globsframework/model/ReadOnlyGlobRepository;Lorg/globsframework/model/GlobRepository;Lorg/globsframework/model/delta/MutableChangeSet;)V", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 2);
        mv.visitInsn(ICONST_1);
        mv.visitTypeInsn(ANEWARRAY, "org/globsframework/metamodel/GlobType");
        mv.visitInsn(DUP);
        mv.visitInsn(ICONST_0);
        mv.visitFieldInsn(GETSTATIC, "org/designup/picsou/model/ImportedTransaction", "TYPE", "Lorg/globsframework/metamodel/GlobType;");
        mv.visitInsn(AASTORE);
        mv.visitMethodInsn(INVOKEINTERFACE, "org/globsframework/model/GlobRepository", "getAll", "([Lorg/globsframework/metamodel/GlobType;)Lorg/globsframework/model/GlobList;");
        mv.visitVarInsn(ASTORE, 4);
        mv.visitVarInsn(ALOAD, 4);
        mv.visitMethodInsn(INVOKEVIRTUAL, "org/globsframework/model/GlobList", "iterator", "()Ljava/util/Iterator;");
        mv.visitVarInsn(ASTORE, 5);
        Label l0 = new Label();
        mv.visitLabel(l0);
        mv.visitVarInsn(ALOAD, 5);
        mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Iterator", "hasNext", "()Z");
        Label l1 = new Label();
        mv.visitJumpInsn(IFEQ, l1);
        mv.visitVarInsn(ALOAD, 5);
        mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Iterator", "next", "()Ljava/lang/Object;");
        mv.visitTypeInsn(CHECKCAST, "org/globsframework/model/Glob");
        mv.visitVarInsn(ASTORE, 6);
        mv.visitVarInsn(ALOAD, 2);
        mv.visitVarInsn(ALOAD, 6);
        mv.visitMethodInsn(INVOKEINTERFACE, "org/globsframework/model/Glob", "getKey", "()Lorg/globsframework/model/Key;");
        mv.visitFieldInsn(GETSTATIC, "org/designup/picsou/model/ImportedTransaction", "AMOUNT", "Lorg/globsframework/metamodel/fields/DoubleField;");
        mv.visitLdcInsn(new Double("-234.0"));
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;");
        mv.visitMethodInsn(INVOKEINTERFACE, "org/globsframework/model/GlobRepository", "update", "(Lorg/globsframework/model/Key;Lorg/globsframework/metamodel/Field;Ljava/lang/Object;)V");
        mv.visitJumpInsn(GOTO, l0);
        mv.visitLabel(l1);
        mv.visitInsn(RETURN);
        mv.visitMaxs(5, 7);
        mv.visitEnd();
      }
      {
        mv = cw.visitMethod(ACC_PUBLIC, "getVersion", "()I", null, null);
        mv.visitCode();
        mv.visitInsn(ICONST_1);
        mv.visitInsn(IRETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
      }
      cw.visitEnd();

      return cw.toByteArray();
    }
  }
}
