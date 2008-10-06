package org.designup.picsou.importer.analyzer;

import org.designup.picsou.model.TransactionType;
import org.designup.picsou.model.TransactionTypeMatcher;
import static org.designup.picsou.model.TransactionTypeMatcher.*;
import org.globsframework.metamodel.GlobModel;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.Strings;
import org.globsframework.utils.exceptions.InvalidFormat;
import org.globsframework.utils.exceptions.ResourceAccessFailed;
import org.globsframework.xml.XmlGlobParser;

import java.io.*;

public class TransactionAnalyzerFactory {

  private GlobModel modelRepository;
  private GlobRepository globRepository;
  private DefaultTransactionAnalyzer analyzer;
  private Long version = 0L;

  public TransactionAnalyzerFactory(GlobModel modelRepository, GlobRepository globRepository) {
    this.modelRepository = modelRepository;
    this.globRepository = globRepository;
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
    BufferedReader bankListReader =
      null;
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
        InputStreamReader reader = null;
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
  }

  private void registerMatchers(GlobRepository globRepository) {
    for (Glob matcher : globRepository.getAll(TransactionTypeMatcher.TYPE).sort(TransactionTypeMatcher.ID)) {
      String regexp = matcher.get(REGEXP);
      String regexpForType = matcher.get(BANK_TYPE);
      TransactionType type = TransactionType.get(matcher);
      String labelRegexp = matcher.get(LABEL);
      Integer groupForDate = matcher.get(GROUP_FOR_DATE);
      String dateFormat = matcher.get(DATE_FORMAT);
      if ((groupForDate != null) && (Strings.isNullOrEmpty(dateFormat))) {
        throw new RuntimeException("You must specify a date format");
      }

      Glob bank = globRepository.findLinkTarget(matcher, BANK);
      if ((labelRegexp != null) && (groupForDate != null)) {
        analyzer.addExclusive(regexp, type, regexpForType, labelRegexp, groupForDate, dateFormat, bank);
      }
      else if (labelRegexp != null) {
        analyzer.addExclusive(regexp, type, regexpForType, labelRegexp, bank);
      }
      else {
        analyzer.addExclusive(regexp, type, regexpForType, bank);
      }
    }
  }
}
