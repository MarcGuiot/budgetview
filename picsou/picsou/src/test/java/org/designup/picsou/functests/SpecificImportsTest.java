package org.designup.picsou.functests;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.crossbowlabs.globs.metamodel.GlobModel;
import org.crossbowlabs.globs.model.*;
import org.crossbowlabs.globs.model.format.GlobPrinter;
import org.crossbowlabs.globs.utils.directory.DefaultDirectory;
import org.crossbowlabs.globs.utils.directory.Directory;
import org.crossbowlabs.globs.xml.XmlGlobParser;
import org.designup.picsou.client.AllocationLearningService;
import org.designup.picsou.functests.utils.DummyServerAccess;
import org.designup.picsou.importer.PicsouImportService;
import org.designup.picsou.importer.TypedInputStream;
import org.designup.picsou.importer.analyzer.TransactionAnalyzerFactory;
import org.designup.picsou.importer.ofx.OfxExporter;
import org.designup.picsou.model.*;

import java.io.*;

public class SpecificImportsTest extends TestCase {
  private static String TEST_FILES_DIR = "testfiles";

  private static String[][] files =
    {{"sg1.qif", "sg1.xml"},
     {"sg2.qif", "sg2.xml"},
     {"cic1.ofx", "cic1.xml"},
     {"ca1.ofx", "ca1.xml"},
     {"laposte1.ofx", "laposte1.xml"},
    };

  public static TestSuite suite() {
    TestSuite suite = new TestSuite();
    for (String[] pair : files) {
      suite.addTest(new ImportTestCase(pair[0], pair[1]));
    }
//    for (String[] pair : files) {
//      suite.addTest(new ExportTestCase(pair[0]));
//    }
    return suite;
  }

  private static class ImportTestCase extends TestCase {
    private String inputFile;
    private String expectedOutputFile;
    private static final String DIRECTORY = File.separator + "testfiles" + File.separator;

    public ImportTestCase(String inputFile, String expectedOutputFile) {
      this.inputFile = DIRECTORY + inputFile;
      this.expectedOutputFile = DIRECTORY + expectedOutputFile;
      setName(inputFile + " ==> " + expectedOutputFile);
    }

    protected void runTest() throws Throwable {
      checkParsing(ImportTestCase.class.getResourceAsStream(inputFile),
                   ImportTestCase.class.getResourceAsStream(expectedOutputFile));
    }
  }

  private static class ExportTestCase extends TestCase {
    private String inputFile;

    public ExportTestCase(String inputFile) {
      this.inputFile = inputFile;
      setName(inputFile + " ==> OFX ==> XML");
    }

    protected void runTest() throws Throwable {
      checkExport(inputFile);
    }

    private void checkExport(String bankInputFileName) throws Exception {
      GlobRepository initialGlobRepository = loadBankFile(ImportTestCase.class.getResourceAsStream(bankInputFileName));

      File ofxOutputFile = writeOfxFile(initialGlobRepository, getTempFilePath(bankInputFileName));

      GlobRepository reloadedGlobRepository = loadBankFile(ofxOutputFile);

      GlobTestUtils.assertEquals(initialGlobRepository, reloadedGlobRepository);
    }

    private File writeOfxFile(GlobRepository globRepository, String filePath) throws IOException {
      File ofxOutputFile = new File(filePath);
      ofxOutputFile.getParentFile().mkdirs();
      FileWriter writer = new FileWriter(ofxOutputFile);
      OfxExporter.write(globRepository, writer);
      writer.close();
      return ofxOutputFile;
    }

    private GlobRepository loadGlobs(File inputFilePath) throws FileNotFoundException {
      Directory directory = new DefaultDirectory();
      directory.add(GlobModel.class, PicsouModel.get());
      GlobRepository globRepository =
        GlobRepositoryBuilder.init().add(directory.get(GlobModel.class).getConstants()).get();
      XmlGlobParser.parse(PicsouModel.get(), globRepository,
                          new FileReader(inputFilePath), "globs");
      return globRepository;
    }
  }

  private static void checkParsing(InputStream ofxInputFilePath, InputStream xmlExpectedOutputFilePath) throws Exception {
    GlobRepository bankDataRepository = loadBankFile(ofxInputFilePath);
    checkOutput(bankDataRepository, xmlExpectedOutputFilePath);
  }

  private static GlobRepository loadBankFile(File ofxInputFilePath) throws IOException {
    return loadBankFile(new FileInputStream(ofxInputFilePath));
  }

  private static GlobRepository loadBankFile(InputStream ofxInputFilePath) throws IOException {
    Directory directory = new DefaultDirectory();
    directory.add(GlobModel.class, PicsouModel.get());

    GlobRepository repository =
      GlobRepositoryBuilder.init().add(directory.get(GlobModel.class).getConstants()).get();

    initRepository(repository);
    TransactionAnalyzerFactory bankFactory = new TransactionAnalyzerFactory(PicsouModel.get());
    PicsouImportService importService = new PicsouImportService(bankFactory,
                                                                new AllocationLearningService(new DummyServerAccess()));
    importService.run(new TypedInputStream(ofxInputFilePath), repository);
    return repository;
  }

  private static void initRepository(GlobRepository repository) {
    createLabelToCategoty(repository, "SARL KALISTEA CARTE PAIEMENT CB PARIS", MasterCategory.FOOD);
    createLabelToCategoty(repository, "BISTROT ANDRE CARTE PAIEMENT CB PARIS", MasterCategory.FOOD);
    createLabelToCategoty(repository, "STATION BP CARTE PAIEMENT CB PARIS", MasterCategory.TRANSPORTS);
    createLabelToCategoty(repository, "STATION BP MAIL CARTE PAIEMENT CB PARIS", MasterCategory.TRANSPORTS);
    createLabelToCategoty(repository, "SARL KALISTEA", MasterCategory.FOOD);
    createLabelToCategoty(repository, "AGF ASSET MANAGEME", MasterCategory.HEALTH);
    createLabelToCategoty(repository, "F COM INTERVENTION MAI", MasterCategory.BANK);
    createLabelToCategoty(repository, "ABON FBQ DONT TVA IDT :", MasterCategory.BANK);
    createLabelToCategoty(repository, "ASS.GENERALES DE FRANCE AGFS", MasterCategory.HEALTH);
    createLabelToCategoty(repository, "TIP FRANCE TELECOM MASSY NOR", MasterCategory.TELECOMS);
    createLabelToCategoty(repository, "EVEIL - JEUX ANTONY", MasterCategory.LEISURES);
    createLabelToCategoty(repository, "MONOPRIX SCEAUX", MasterCategory.FOOD);
    createLabelToCategoty(repository, "INTERETS DEBITEURS", MasterCategory.BANK);
    createLabelToCategoty(repository, "Tip France Telecom", MasterCategory.TELECOMS);
    createLabelToCategoty(repository, "ATAC MAG GAILL", MasterCategory.FOOD);
    createLabelToCategoty(repository, "Websncf", MasterCategory.TRANSPORTS);
    createLabelToCategoty(repository, "Prel Free Telecom", MasterCategory.TELECOMS);
    createLabelToCategoty(repository, "Grosbill", MasterCategory.MULTIMEDIA);
  }

  private static void createLabelToCategoty(GlobRepository repository, String label, MasterCategory category) {
    repository.create(Key.create(LabelToCategory.TYPE,repository.getIdGenerator().getNextId(LabelToCategory.ID, 1)),
                      FieldValue.value(LabelToCategory.LABEL, label),
                        FieldValue.value(LabelToCategory.COUNT, 1),
                        FieldValue.value(LabelToCategory.CATEGORY, category.getId()));
  }

  private static void checkOutput(GlobRepository bankDataRepository,
                                  InputStream expectedOutputFile) throws Exception {

    GlobRepository referenceRepository =
      GlobRepositoryBuilder.init().add(PicsouModel.get().getConstants()).get();
    XmlGlobParser.parse(PicsouModel.get(),
                        referenceRepository,
                        new InputStreamReader(expectedOutputFile),
                        "globs");

    assertEquals(dump(referenceRepository), dump(bankDataRepository));
  }

  private static String dump(GlobRepository bankDataRepository) {
    return GlobPrinter.init(bankDataRepository)
      .showOnly(Bank.TYPE, Account.TYPE, Transaction.TYPE)
      .exclude(Transaction.ID, Account.ID, Transaction.IMPORT)
      .dumpToString();
  }

  private static String getTempFilePath(String inputFileName) {
    return "tmp/ofxExport/" + inputFileName;
  }

  private static String getSampleFilePath(String inputFileName) {
    return TEST_FILES_DIR + "/" + inputFileName;
  }
}
