package org.designup.picsou.gui.importer.csv;

import org.designup.picsou.gui.components.CancelAction;
import org.designup.picsou.gui.components.dialogs.PicsouDialog;
import org.designup.picsou.importer.utils.TypedInputStream;
import org.designup.picsou.model.CsvMapping;
import org.designup.picsou.model.ImportedSeries;
import org.designup.picsou.model.ImportedTransaction;
import org.designup.picsou.model.RealAccount;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.repeat.Repeat;
import org.globsframework.gui.splits.repeat.RepeatCellBuilder;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.metamodel.utils.GlobTypeBuilder;
import org.globsframework.model.FieldValue;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.repository.LocalGlobRepository;
import org.globsframework.model.repository.LocalGlobRepositoryBuilder;
import org.globsframework.model.utils.GlobBuilder;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.Strings;
import org.globsframework.utils.collections.MultiMap;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.util.*;
import java.util.List;

public class CsvImporterPanel {
  private Directory localDirectory;
  private LocalGlobRepository localRepository;
  private char separator;
  private TypedInputStream inputStream;
  private GlobType type;
  private GlobList globs = new GlobList();
  private final PicsouDialog dialog;
  private final GlobsPanelBuilder builder;
  private final List<FieldToAssociate> associates = new ArrayList<FieldToAssociate>();
  private Repeat<FieldToAssociate> repeat;
  private GlobRepository initialRepository;

  public CsvImporterPanel(Window parent, TypedInputStream inputStream, GlobRepository initialRepository,
                          GlobRepository globRepository, Directory directory) {
    this.initialRepository = initialRepository;
    this.localDirectory = new DefaultDirectory(directory);
    this.localRepository =
      LocalGlobRepositoryBuilder.init(globRepository)
        .copy(CsvMapping.TYPE, ImportedSeries.TYPE)
        .get();
    builder = new GlobsPanelBuilder(getClass(), "/layout/importexport/importCsvPanel.splits", localRepository, localDirectory);
    initPanel(builder, inputStream);
    this.dialog = PicsouDialog.create(parent, directory);
    dialog.addPanelWithButtons(builder.<JPanel>load(),
                               new AbstractAction(Lang.get("ok")) {
                                 public void actionPerformed(ActionEvent e) {
                                   validate();
                                   dialog.setVisible(false);
                                 }
                               },
                               new CancelAction(dialog));

  }

  public void show() {
    dialog.pack();
    dialog.setVisible(true);
    builder.dispose();
  }

  private void validate() {
    int accountId = localRepository.create(RealAccount.TYPE).get(RealAccount.ID);
    for (Glob glob : globs) {
      String envelope = "";
      String subEnvelope = "";
      Glob targetGlob = localRepository.create(ImportedTransaction.TYPE);
      for (FieldToAssociate associate : associates) {
        if (associate.target == null || associate.target.field == CsvType.NOT_IMPORTED) {
          continue;
        }
        if (associate.target.translate != null) {
          localRepository.update(targetGlob.getKey(), associate.target.importTransactionField,
                                 associate.target.translate.getUpdate(targetGlob.getValue(associate.target.importTransactionField),
                                                                      glob.get(associate.field)));
        }
        else if (associate.target.field == CsvType.ENVELOPE) {
          envelope = glob.get(associate.field);
        }
        else if (associate.target.field == CsvType.SUB_ENVELOPE) {
          subEnvelope = glob.get(associate.field);
        }
      }
      String newSeries = envelope + (Strings.isNotEmpty(subEnvelope) ? ":" + subEnvelope : "");
      if (Strings.isNotEmpty(newSeries)) {
        Glob series = localRepository.getAll(ImportedSeries.TYPE, GlobMatchers.fieldEquals(ImportedSeries.NAME, newSeries)).getFirst();
        if (series == null) {
          series = localRepository.create(ImportedSeries.TYPE, FieldValue.value(ImportedSeries.NAME, newSeries));
        }
        int seriesId = series.get(ImportedSeries.ID);
        localRepository.update(targetGlob.getKey(), ImportedTransaction.SERIES, seriesId);
      }
      localRepository.update(targetGlob.getKey(), ImportedTransaction.ACCOUNT, accountId);
    }
    initialRepository.deleteAll(CsvMapping.TYPE);
    for (FieldToAssociate associate : associates) {
      if (associate.target != null){
        initialRepository.create(CsvMapping.TYPE,
                               FieldValue.value((CsvMapping.FROM), associate.field.getName()),
                               FieldValue.value((CsvMapping.TO), associate.target.field.getName()));
      }
    }
    localRepository.commitChanges(true);
  }

  static class FieldToAssociate {
    public final StringField field;
    public CsvType.Name target;

    public FieldToAssociate(StringField field) {
      this.field = field;
    }
  }

  private void initPanel(GlobsPanelBuilder builder, TypedInputStream inputStream) {
    this.inputStream = inputStream;
    BufferedReader reader = getReader();
    try {
      String line = reader.readLine();
      int tabCount = 0;
      int dot = 0;
      int virgule = 0;
      int towDot = 0;
      for (int i = 0; i < line.length(); i++) {
        char c = line.charAt(i);
        if (c == '\t') {
          tabCount++;
        }
        if (c == ';') {
          dot++;
        }
        if (c == ',') {
          virgule++;
        }
        if (c == ':') {
          towDot++;
        }
      }
      if (tabCount > 1) {
        separator = '\t';
      }
      else if (dot > 1) {
        separator = ';';
      }
      else if (virgule > 1) {
        separator = ',';
      }
      else if (towDot > 1) {
        separator = ':';
      }
    }
    catch (Exception e) {
    }


    repeat = builder.addRepeat("CSV_LINE", associates, new CsvImporterFieldRepeatComponentFactory());
    ButtonGroup radioGroup = new ButtonGroup();
    add(builder, radioGroup, "separatorSep1", "tab", '\t');
    add(builder, radioGroup, "separatorSep2", " ; ", ';');
    add(builder, radioGroup, "separatorSep3", " : ", ':');
    add(builder, radioGroup, "separatorSep4", " , ", ',');

    initFirstCsvLine();
  }

  private void add(GlobsPanelBuilder builder, ButtonGroup radioGroup, String sep1, String text, final char c) {
    JRadioButton radioSep1 = new JRadioButton(text);
    radioGroup.add(radioSep1);
    builder.add(sep1, radioSep1);
    radioSep1.setSelected(separator == c);
    radioSep1.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        separator = c;
        initFirstCsvLine();
      }
    });
  }

  private void initFirstCsvLine() {
    try {
      globs.clear();
      associates.clear();
      BufferedReader reader = getReader();
      String firstLine = reader.readLine();
      GlobTypeBuilder typeBuilder = GlobTypeBuilder.init("CSV");
      typeBuilder.addIntegerKey("ID");
      int columnCount = 0;
      List<String> elements = CsvReader.readLine(firstLine, separator);
      for (String element : elements) {
        if (Strings.isNullOrEmpty(element)) {
          typeBuilder.addStringField(Lang.get("import.csv.first.column", columnCount));
        }
        else {
          typeBuilder.addStringField(element);
        }
      }
      type = typeBuilder.get();
      elements = CsvReader.readLine(reader.readLine(), separator);
      while (elements != null) {
        GlobBuilder globBuilder = GlobBuilder.init(type);
        int i = 1;
        for (String element : elements) {
          StringField field = (StringField)type.getField(i);
          globBuilder.set(field, element);
          i++;
        }
        globs.add(globBuilder.get());
        elements = CsvReader.readLine(reader.readLine(), separator);
      }
      List<Field> items = Arrays.asList(type.getFields()).subList(1, type.getFieldCount());
      for (Field item : items) {
        FieldToAssociate associate = new FieldToAssociate((StringField)item);
        associates.add(associate);
      }
      GlobList csvMappings = initialRepository.getAll(CsvMapping.TYPE);
      Map<String, Glob> csvMapping = new HashMap<String, Glob>();
      for (Glob mapping : csvMappings) {
        csvMapping.put(mapping.get(CsvMapping.FROM), mapping);
      }
      for (FieldToAssociate associate : associates) {
        Glob glob = csvMapping.get(associate.field.getName());
        if (glob != null){
          associate.target = CsvType.get(glob.get(CsvMapping.TO)); 
        }
      }
      repeat.set(associates);
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private BufferedReader getReader() {
    return new BufferedReader(inputStream.getBestProbableReader());
  }

  private class CsvImporterFieldRepeatComponentFactory implements RepeatComponentFactory<FieldToAssociate> {

    public void registerComponents(RepeatCellBuilder cellBuilder, final FieldToAssociate fieldToAssociate) {
      final StringField field = fieldToAssociate.field;
      cellBuilder.add("first", new JLabel(field.getName()));
      cellBuilder.add("second", new JLabel(globs.get(0).get(field)));
      final JComboBox component = new JComboBox(CsvType.getValues());
      if (fieldToAssociate.target != null) {
        component.setSelectedItem(fieldToAssociate.target);
      }
      component.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          fieldToAssociate.target = (CsvType.Name)component.getSelectedItem();
        }
      });
      cellBuilder.add("dataName", component);
      final JLabel label = new JLabel();
      component.setRenderer(new ListCellRenderer() {
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
          label.setText(((CsvType.Name)value).name);
          return label;
        }
      });
    }
  }
}
