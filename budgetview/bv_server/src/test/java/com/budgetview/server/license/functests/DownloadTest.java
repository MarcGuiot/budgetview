package com.budgetview.server.license.functests;

import com.budgetview.desktop.Application;
import com.budgetview.desktop.startup.AppPaths;
import com.budgetview.desktop.time.TimeService;
import com.budgetview.desktop.userconfig.UserConfigService;
import com.budgetview.functests.checkers.MessageDialogChecker;
import com.budgetview.functests.utils.OfxBuilder;
import com.budgetview.server.license.checkers.FtpServerChecker;
import com.budgetview.functests.checkers.ApplicationChecker;
import com.budgetview.functests.checkers.license.LicenseActivationChecker;
import com.budgetview.server.license.ConnectedTestCase;
import com.budgetview.server.license.model.SoftwareInfo;
import com.budgetview.model.TransactionType;
import org.globsframework.sqlstreams.SqlConnection;
import org.globsframework.utils.Dates;
import org.objectweb.asm.*;
import org.uispec4j.Window;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

/*
 * Note: Il faut mettre les banques specifiques dans la liste de banques de generateConfigContent()
*/
public class DownloadTest extends ConnectedTestCase {
  private ApplicationChecker application;
  long currentVersion;

  protected void setUp() throws Exception {
    System.setProperty("budgetview.log.sout", "true");
    super.setUp();
    System.setProperty(Application.DELETE_LOCAL_PREVAYLER_PROPERTY, "false");
    System.setProperty(Application.IS_DATA_IN_MEMORY, "false");
    TimeService.setCurrentDate(Dates.parseMonth("2008/07"));
    application = new ApplicationChecker();
    currentVersion = Application.JAR_VERSION;
  }

  protected void tearDown() throws Exception {
    super.tearDown();
    System.setProperty(Application.DELETE_LOCAL_PREVAYLER_PROPERTY, "true");
    application.dispose();
    application = null;
    Application.JAR_VERSION = currentVersion;
  }

  public void testDataVersionInTheFuture() throws Exception {
    Application.JAR_VERSION = currentVersion + 1;

    updateDb(Application.JAR_VERSION, Application.BANK_CONFIG_VERSION);
    startServers();
    String ofxFile = OfxBuilder.init(this)
      .addBankAccount(30066, -2, "1111", 123.3, "10/09/2008")
      .addTransaction("2008/09/10", -100., "STUPID HEADER blabla")
      .save();

    application.start();
    application.getOperations().importOfxFile(ofxFile);
    application.getOperations().exit();
    application.dispose();

    Application.JAR_VERSION = currentVersion;

    final String jarName = UserConfigService.generatePicsouJarName(Application.JAR_VERSION + 1);
    FtpServerChecker.Retr retr = ftpServer.setFtpReply(jarName, new String(new byte[12 * 1024 * 1024]), null, null);
    application.startModal();

    Window window1 = application.getWindow();
    MessageDialogChecker messageDialogChecker = new MessageDialogChecker(window1);

    messageDialogChecker.checkInfoMessageContains("The update is completed, you can now restart BudgetView.");
    retr.assertOk();
  }

  public void testJarIsSentToSpecificUser() throws Exception {
    String email = "alfred@free.fr";
    db.registerMail(email, "1234");
    updateDb(Application.JAR_VERSION + 1L, Application.BANK_CONFIG_VERSION + 1L);
    updateDb(email, Application.JAR_VERSION + 2L, Application.BANK_CONFIG_VERSION + 1L);

    startServers();

    final String jarName = UserConfigService.generatePicsouJarName(Application.JAR_VERSION + 1L);
    final String configJarName = UserConfigService.generateConfigJarName(Application.BANK_CONFIG_VERSION + 1L);
    byte[] content = generateConfigContent();
    FtpServerChecker.Retr retr = ftpServer.setFtpReply(jarName, "jar content", configJarName, content);
    application.start();
    retr.assertOk();

    System.setProperty(Application.DELETE_LOCAL_PREVAYLER_PROPERTY, "false");

    Window window = application.getWindow();
    LicenseActivationChecker.enterLicense(window, email, "1234");
    Thread.sleep(1000);
    application.getOperations().exit();
    window.dispose();

    final String jarName2 = UserConfigService.generatePicsouJarName(Application.JAR_VERSION + 2L);

    FtpServerChecker.Retr retr2 = ftpServer.setFtpReply(jarName2, "jar content", null, null);
    application.startWithoutSLA();
    retr2.assertOk();
  }

  public void testJarIsSentAndConfigUpdated() throws Exception {
    updateDb(Application.JAR_VERSION + 1L, Application.BANK_CONFIG_VERSION + 1L);
    startServers();
    final String jarName = UserConfigService.generatePicsouJarName(Application.JAR_VERSION + 1L);
    final String configJarName = UserConfigService.generateConfigJarName(Application.BANK_CONFIG_VERSION + 1L);
    byte[] content = generateConfigContent();
    FtpServerChecker.Retr retr = ftpServer.setFtpReply(jarName, "jar content", configJarName, content);
    application.start();
    retr.assertOk();

    String ofxFile = OfxBuilder.init(this)
      .addBankAccount(4321, -2, "1111", 123.3, "10/09/2008")
      .addTransaction("2008/09/10", -100., "STUPID HEADER blabla")
      .save();

    application.getOperations().importOfxFile(ofxFile);

    application.getViews().selectData();

    application.getTransactions().initContent()
      .add("10/09/2008", TransactionType.VIREMENT, "GOOD HEADER", "", -234.00)
      .check();
    String path = AppPaths.getDefaultDataPath();
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

  private void updateDb(final long jarVersion, final long configVersion) {
    SqlConnection connection = db.getConnection();
    connection.startCreate(SoftwareInfo.TYPE)
      .set(SoftwareInfo.LATEST_JAR_VERSION, jarVersion)
      .set(SoftwareInfo.LATEST_CONFIG_VERSION, configVersion)
      .run();
    connection.commitAndClose();
  }

  private void updateDb(String mail, long version, final long configVersion) {
    SqlConnection connection = db.getConnection();
    connection.startCreate(SoftwareInfo.TYPE)
      .set(SoftwareInfo.MAIL, mail)
      .set(SoftwareInfo.LATEST_JAR_VERSION, version)
      .set(SoftwareInfo.LATEST_CONFIG_VERSION, configVersion)
      .run();
    connection.commitAndClose();
  }

  public void testNewBankInConfig() throws Exception {
    updateDb(Application.JAR_VERSION + 1L, Application.BANK_CONFIG_VERSION + 1L);
    licenseServer.init();
    application.start();

    String fileName = OfxBuilder.init(this)
      .addBankAccount("4321", 111, "1111", 0, "10/09/2008")
      .addTransaction("2008/09/10", -100., "STUPID HEADER blabla")
      .save();

    application.getOperations().importOfxFile(fileName, "Other");

    final String jarName = UserConfigService.generatePicsouJarName(Application.JAR_VERSION + 1L);
    final String configJarName = UserConfigService.generateConfigJarName(Application.BANK_CONFIG_VERSION + 1L);

    FtpServerChecker.Retr retr = ftpServer.setFtpReply(jarName, "jar content", configJarName, generateConfigContent());
    startServersWithoutLicence();
    retr.assertOk();

    application.getTransactions().initContent()
      .add("10/09/2008", TransactionType.VIREMENT, "GOOD HEADER", "", -100.00) // le plugin specific de banque n'est pas appelé
      .check();
  }

  private byte[] generateConfigContent() throws Exception {
    File tempFile = File.createTempFile("config", ".jar");
    tempFile.deleteOnExit();
    JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(tempFile));
    jarOutputStream.putNextEntry(new ZipEntry("banks/bankList.txt"));
    jarOutputStream.write("picsouBank.xml\n".getBytes());
    jarOutputStream.putNextEntry(new ZipEntry("banks/picsouBank.xml"));
    jarOutputStream.write(("<globs>\n" +
                           "  <bankFormat id='-2'>\n" +
                           "    <bank name=\"picsouBank\" downloadUrl=\"\" id='-2'>\n" +
                           "      <bankEntity id=\"10807\"/> \n" +
                           "      <bankEntity id=\"17515\"/> \n" +
                           "      <bankEntity id=\"4321\"/> \n" +
                           "      <bankEntity id=\"20041\"/> \n" +
                           "      <bankEntity id=\"24599\"/> \n" +
                           "    </bank>\n" +
                           "    <transactionMatcher ofxName=\"STUPID HEADER .*\"\n" +
                           "                        transactionTypeName=\"virement\" " +
                           "                        label=\"GOOD HEADER\"/>" +
                           "  </bankFormat>\n" +
                           "</globs>\n").getBytes());
    jarOutputStream.putNextEntry(new ZipEntry("org/designup/picsou/license/functests/DummyBankPlugin.class"));
    DummyBankPluginDump bankPluginDump = new DummyBankPluginDump();
    jarOutputStream.write(bankPluginDump.dump());
    jarOutputStream.close();
    byte content[] = new byte[(int) tempFile.length()];
    FileInputStream inputStream = new FileInputStream(tempFile);
    inputStream.read(content);
    inputStream.close();
    return content;
  }

//  org.objectweb.asm.util.ASMifierClassVisitor
//  /home/guiot/dev/java/lesueur/ref/picsou/picsou_licence_server/target/test-classes/org/designup/picsou/license/functests/DummyBankPlugin.class

  public static class DummyBankPluginDump implements Opcodes {

    public static byte[] dump() throws Exception {

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
        mv = cw.visitMethod(ACC_PUBLIC, "apply", "(Lorg/globsframework/model/Glob;Lorg/globsframework/model/Glob;Lorg/globsframework/model/GlobList;Lorg/globsframework/model/ReadOnlyGlobRepository;Lorg/globsframework/model/GlobRepository;Lorg/globsframework/model/delta/MutableChangeSet;)Z", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 5);
        mv.visitInsn(ICONST_1);
        mv.visitTypeInsn(ANEWARRAY, "org/globsframework/metamodel/GlobType");
        mv.visitInsn(DUP);
        mv.visitInsn(ICONST_0);
        mv.visitFieldInsn(GETSTATIC, "org/designup/picsou/model/ImportedTransaction", "TYPE", "Lorg/globsframework/metamodel/GlobType;");
        mv.visitInsn(AASTORE);
        mv.visitMethodInsn(INVOKEINTERFACE, "org/globsframework/model/GlobRepository", "getAll", "([Lorg/globsframework/metamodel/GlobType;)Lorg/globsframework/model/GlobList;");
        mv.visitVarInsn(ASTORE, 7);
        mv.visitVarInsn(ALOAD, 7);
        mv.visitMethodInsn(INVOKEVIRTUAL, "org/globsframework/model/GlobList", "iterator", "()Ljava/util/Iterator;");
        mv.visitVarInsn(ASTORE, 8);
        Label l0 = new Label();
        mv.visitLabel(l0);
        mv.visitVarInsn(ALOAD, 8);
        mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Iterator", "hasNext", "()Z");
        Label l1 = new Label();
        mv.visitJumpInsn(IFEQ, l1);
        mv.visitVarInsn(ALOAD, 8);
        mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Iterator", "next", "()Ljava/lang/Object;");
        mv.visitTypeInsn(CHECKCAST, "org/globsframework/model/Glob");
        mv.visitVarInsn(ASTORE, 9);
        mv.visitVarInsn(ALOAD, 5);
        mv.visitVarInsn(ALOAD, 9);
        mv.visitMethodInsn(INVOKEINTERFACE, "org/globsframework/model/Glob", "getKey", "()Lorg/globsframework/model/Key;");
        mv.visitFieldInsn(GETSTATIC, "org/designup/picsou/model/ImportedTransaction", "AMOUNT", "Lorg/globsframework/metamodel/fields/DoubleField;");
        mv.visitLdcInsn(new Double("-234.0"));
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;");
        mv.visitMethodInsn(INVOKEINTERFACE, "org/globsframework/model/GlobRepository", "update", "(Lorg/globsframework/model/Key;Lorg/globsframework/metamodel/Field;Ljava/lang/Object;)V");
        mv.visitJumpInsn(GOTO, l0);
        mv.visitLabel(l1);
        mv.visitInsn(ICONST_1);
        mv.visitInsn(IRETURN);
        mv.visitMaxs(5, 10);
        mv.visitEnd();
      }
      {
        mv = cw.visitMethod(ACC_PUBLIC, "postApply", "(Lorg/globsframework/model/GlobList;Lorg/globsframework/model/Glob;Lorg/globsframework/model/GlobRepository;Lorg/globsframework/model/GlobRepository;Lorg/globsframework/model/ChangeSet;)V", null, null);
        mv.visitCode();
        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 6);
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
