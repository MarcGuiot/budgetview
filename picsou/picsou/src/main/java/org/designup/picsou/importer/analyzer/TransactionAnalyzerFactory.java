package org.designup.picsou.importer.analyzer;

import org.designup.picsou.model.PreTransactionTypeMatcher;
import org.designup.picsou.model.TransactionType;
import org.globsframework.metamodel.GlobModel;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.GlobPrinter;
import org.globsframework.utils.exceptions.InvalidFormat;
import org.globsframework.utils.exceptions.ResourceAccessFailed;
import org.globsframework.xml.XmlGlobParser;

import java.io.*;
import java.util.regex.Pattern;

public class TransactionAnalyzerFactory {

  private GlobModel modelRepository;
  private GlobRepository globRepository;
  private DefaultTransactionAnalyzer analyzer;
  private Long version = 0L;
  public static Pattern BLANK = Pattern.compile("[\\s]+");

  public TransactionAnalyzerFactory(GlobModel modelRepository, GlobRepository globRepository) {
    this.modelRepository = modelRepository;
    this.globRepository = globRepository;
  }

  public static String removeBlankAndToUpercase(final String value) {
    if (value == null) {
      return null;
    }
    return BLANK.matcher(value).replaceAll(" ").trim().toUpperCase();
  }

  public interface Loader {
    InputStream load(String file);
  }

  synchronized public void load(final ClassLoader loader, Long version) {
    load(new Loader() {
      public InputStream load(String file) {
        return loader.getResourceAsStream(file);
      }
    }, version);
  }

  synchronized public void load(Loader loader, Long version) {
    if (this.version < version) {
      this.version = version;
      this.analyzer = new DefaultTransactionAnalyzer();
      loadMatchers(loader);
      analyzer.add(new LabelForCategorizationUpdater());
      analyzer.add(new TransactionDateUpdater());
    }
  }

  synchronized public TransactionAnalyzer getAnalyzer() {
    return analyzer;
  }

  private void loadMatchers(Loader loader) {
    parseDefinitionFile(globRepository, loader);
    registerMatchers(globRepository);
  }

  private void parseDefinitionFile(GlobRepository globRepository, Loader loader) {
    String bankListFileName = "banks/bankList.txt";
    InputStream bankListStream = loader.load(bankListFileName);

    if (bankListStream == null) {
      throw new ResourceAccessFailed("missing bank file list'" + bankListFileName + "'");
    }
    BufferedReader bankListReader;
    try {
      bankListReader = new BufferedReader(new InputStreamReader(bankListStream, "UTF-8"));
    }
    catch (UnsupportedEncodingException e) {
      throw new InvalidFormat(e);
    }
    while (true) {
      String bankFileName;
      try {
        bankFileName = bankListReader.readLine();
      }
      catch (IOException e) {
        throw new ResourceAccessFailed(e);
      }
      if (bankFileName == null) {
        break;
      }
      String path = "banks/" + bankFileName;
      InputStream stream = loader.load(path);
      if (stream != null) {
        InputStreamReader reader;
        try {
          reader = new InputStreamReader(stream, "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
          throw new ResourceAccessFailed(e);
        }
        XmlGlobParser.parse(modelRepository, globRepository, reader, "globs");
      }
      else {
        throw new ResourceAccessFailed("missing bank file '" + path + "'");
      }
    }
    GlobList matchers = globRepository.getAll(PreTransactionTypeMatcher.TYPE);
    for (Glob matcher : matchers) {
      if (matcher.get(PreTransactionTypeMatcher.FOR_OFX) == null) {
        if (OfxTransactionFinalizer.isOfType(matcher)) {
          globRepository.update(matcher.getKey(), PreTransactionTypeMatcher.FOR_OFX, true);
        }
        else if (QifTransactionFinalizer.isOfType(matcher)) {
          globRepository.update(matcher.getKey(), PreTransactionTypeMatcher.FOR_OFX, false);
        }
        else {
          throw new RuntimeException("Unable to know if we should parse ofx tags or qif attributes for " +
                                     GlobPrinter.toString(matcher));
        }
      }
    }
  }

  private void registerMatchers(GlobRepository globRepository) {
    for (Glob matcher : globRepository.getAll(PreTransactionTypeMatcher.TYPE)
      .sort(PreTransactionTypeMatcher.ID)) {
      String label = matcher.get(PreTransactionTypeMatcher.LABEL);
      String originalLabel = matcher.get(PreTransactionTypeMatcher.ORIGINAL_LABEL);
      Glob bank = globRepository.findLinkTarget(matcher, PreTransactionTypeMatcher.BANK);
      TransactionType type = TransactionType.get(matcher);
      String bankType = matcher.get(PreTransactionTypeMatcher.BANK_TYPE);
      String groupForDate = matcher.get(PreTransactionTypeMatcher.GROUP_FOR_DATE);
      String dateFormat = matcher.get(PreTransactionTypeMatcher.DATE_FORMAT);

      if (matcher.get(PreTransactionTypeMatcher.FOR_OFX)) {
        String name = matcher.get(PreTransactionTypeMatcher.OFX_NAME);
        String memo = matcher.get(PreTransactionTypeMatcher.OFX_MEMO);
        String checkNum = matcher.get(PreTransactionTypeMatcher.OFX_CHECK_NUM);
        if (label != null) {
          analyzer.addOfx(name, memo, checkNum, label, bank, bankType, groupForDate, dateFormat, type);
          groupForDate = null; //pour ne pas updater deux fois la date si dans le meme group on a label et originallabel
          dateFormat = null;
        }
        if (originalLabel != null) {
          analyzer.addOriginalOfx(name, memo, checkNum, originalLabel, bank, bankType,
                                  groupForDate, dateFormat, type);
        }
      }
      else {
        String qifM = matcher.get(PreTransactionTypeMatcher.QIF_M);
        String qifP = matcher.get(PreTransactionTypeMatcher.QIF_P);
        if (label != null) {
          analyzer.addQif(qifM, qifP, label, bank, bankType, groupForDate, dateFormat, type);
          groupForDate = null; //pour ne pas updater deux fois la date
          dateFormat = null;
        }
        if (originalLabel != null) {
          analyzer.addOriginalQif(qifM, qifP, originalLabel, bank, bankType,
                                  groupForDate, dateFormat, type);
        }
      }
    }
  }
}
