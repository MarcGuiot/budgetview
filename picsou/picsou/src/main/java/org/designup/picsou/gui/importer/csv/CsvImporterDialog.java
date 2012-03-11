package org.designup.picsou.gui.importer.csv;

import org.designup.picsou.gui.components.CancelAction;
import org.designup.picsou.gui.components.dialogs.PicsouDialog;
import org.designup.picsou.importer.csv.CsvReader;
import org.designup.picsou.importer.csv.CsvType;
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
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.metamodel.utils.GlobTypeBuilder;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.repository.LocalGlobRepository;
import org.globsframework.model.repository.LocalGlobRepositoryBuilder;
import org.globsframework.model.utils.GlobBuilder;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.OperationCancelled;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;
import java.util.List;

import static org.globsframework.model.FieldValue.value;

public class CsvImporterDialog {

  private LocalGlobRepository localRepository;
  private GlobRepository initialRepository;

  private GlobList globs = new GlobList();

  private char currentSeparator;
  private TypedInputStream inputStream;

  private final PicsouDialog dialog;
  private final GlobsPanelBuilder builder;
  private final List<FieldAssociation> associations = new ArrayList<FieldAssociation>();
  private Repeat<FieldAssociation> repeat;
  private boolean cancelled;

  public CsvImporterDialog(Window parent, TypedInputStream inputStream, GlobRepository initialRepository,
                           GlobRepository repository, Directory directory) {
    this.initialRepository = initialRepository;

    this.localRepository =
      LocalGlobRepositoryBuilder.init(repository)
        .copy(CsvMapping.TYPE, ImportedSeries.TYPE)
        .get();

    builder = new GlobsPanelBuilder(getClass(), "/layout/importexport/csvImporterDialog.splits",
                                    localRepository, directory);
    initPanel(builder, inputStream);
    this.dialog = PicsouDialog.create(parent, directory);
    dialog.addPanelWithButtons(builder.<JPanel>load(), new OkAction(), new CancelAction(dialog) {
      public void actionPerformed(ActionEvent e) {
        cancelled = true;
        super.actionPerformed(e);
      }
    });
  }

  public GlobList show() throws OperationCancelled {
    if (globs.isEmpty()) {
      return globs;
    }
    dialog.pack();
    GuiUtils.showCentered(dialog);
    builder.dispose();
    if (cancelled) {
      throw new OperationCancelled("CSV import cancelled");
    }
    return globs;
  }

  private static class FieldAssociation {
    public final StringField fileField;
    public CsvType.Mapper mapper;

    public FieldAssociation(StringField fileField) {
      this.fileField = fileField;
    }
  }

  private void initPanel(GlobsPanelBuilder builder, TypedInputStream inputStream) {
    this.inputStream = inputStream;

    this.currentSeparator = CsvReader.findSeparator(getReader());

    repeat = builder.addRepeat("csvAssociationsRepeat", associations, new CsvImporterFieldRepeatComponentFactory());

    ButtonGroup radioGroup = new ButtonGroup();
    addSeparatorRadio(builder, radioGroup, "separatorSep1", "tab", '\t');
    addSeparatorRadio(builder, radioGroup, "separatorSep2", " ; ", ';');
    addSeparatorRadio(builder, radioGroup, "separatorSep3", " : ", ':');
    addSeparatorRadio(builder, radioGroup, "separatorSep4", " , ", ',');

    preloadFile();
  }

  private void addSeparatorRadio(GlobsPanelBuilder builder, ButtonGroup radioGroup, String componentName, String text, final char separatorChar) {
    JRadioButton radio = new JRadioButton(text);
    radioGroup.add(radio);
    builder.add(componentName, radio);
    radio.setSelected(currentSeparator == separatorChar);
    radio.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        currentSeparator = separatorChar;
        preloadFile();
      }
    });
  }

  private void preloadFile() {
    try {
      globs.clear();
      associations.clear();

      BufferedReader reader = getReader();
      String firstLine = reader.readLine();
      if (Strings.isNotEmpty(firstLine)) {
        GlobType type = createGlobType(firstLine);

        parseLineGlobs(reader, type);

        List<Field> fields = Arrays.asList(type.getFields()).subList(1, type.getFieldCount());
        for (Field field : fields) {
          FieldAssociation association = new FieldAssociation((StringField)field);
          associations.add(association);
        }
        presetAssociations();
      }

      repeat.set(associations);
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private void parseLineGlobs(BufferedReader reader, GlobType type) throws IOException {
    List<String> elements = CsvReader.readLine(reader.readLine(), currentSeparator);
    while (elements != null) {
      GlobBuilder globBuilder = GlobBuilder.init(type);
      int i = 1;
      for (String element : elements) {
        StringField field = (StringField)type.getField(i);
        globBuilder.set(field, element);
        i++;
      }
      globs.add(globBuilder.get());
      elements = CsvReader.readLine(reader.readLine(), currentSeparator);
    }
  }

  private GlobType createGlobType(String firstLine) {
    GlobTypeBuilder typeBuilder = GlobTypeBuilder.init("CSV");
    typeBuilder.addIntegerKey("ID");
    int columnCount = 0;
    List<String> elements = CsvReader.readLine(firstLine, currentSeparator);
    for (String element : elements) {
      if (Strings.isNullOrEmpty(element)) {
        typeBuilder.addStringField(Lang.get("import.csv.first.column", columnCount));
      }
      else {
        typeBuilder.addStringField(element);
      }
    }
    return typeBuilder.get();
  }

  private void presetAssociations() {
    Map<String, CsvType.Mapper> csvTypesByName = new HashMap<String, CsvType.Mapper>();
    for (CsvType.Mapper mapper : CsvType.getMappers()) {
      for (String fieldName : mapper.defaultFieldNames) {
        csvTypesByName.put(normalize(fieldName), mapper);
      }
    }
    for (Glob csvMapping : initialRepository.getAll(CsvMapping.TYPE)) {
      csvTypesByName.put(normalize(csvMapping.get(CsvMapping.FIELD_NAME)),
                         CsvType.get(csvMapping.get(CsvMapping.CSV_TYPE_NAME)));
    }
    for (FieldAssociation association : associations) {
      CsvType.Mapper mapper = csvTypesByName.get(normalize(association.fileField.getName()));
      if (mapper != null) {
        association.mapper = mapper;
      }
    }
  }

  private String normalize(String fieldName) {
    return Strings.unaccent(fieldName.toLowerCase())
      .replace("-", " ")
      .replace("\"", "")
      .trim();
  }

  private BufferedReader getReader() {
    return new BufferedReader(inputStream.getBestProbableReader());
  }

  private class CsvImporterFieldRepeatComponentFactory implements RepeatComponentFactory<FieldAssociation> {

    private static final int MAX_LABEL_LENGTH = 40;

    public void registerComponents(RepeatCellBuilder cellBuilder, final FieldAssociation fieldAssociation) {
      final StringField field = fieldAssociation.fileField;
      String firstLineContent = Strings.cut(field.getName(), MAX_LABEL_LENGTH);
      cellBuilder.add("first", new JLabel(firstLineContent));
      String secondLineContent = globs.size() > 0 ? Strings.cut(globs.get(0).get(field), MAX_LABEL_LENGTH) : "";
      cellBuilder.add("second", new JLabel(secondLineContent));
      final JComboBox component = new JComboBox(CsvType.getMappers());
      if (fieldAssociation.mapper != null) {
        component.setSelectedItem(fieldAssociation.mapper);
      }
      component.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          fieldAssociation.mapper = (CsvType.Mapper)component.getSelectedItem();
        }
      });
      cellBuilder.add("dataName", component);
      final JLabel label = new JLabel();
      component.setRenderer(new ListCellRenderer() {
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
          label.setText(((CsvType.Mapper)value).name);
          return label;
        }
      });
    }
  }

  private class OkAction extends AbstractAction {
    public OkAction() {
      super(Lang.get("ok"));
    }

    public void actionPerformed(ActionEvent e) {
      validate();
      dialog.setVisible(false);
    }
  }

  private void validate() {
    int accountId = localRepository.create(RealAccount.TYPE).get(RealAccount.ID);
    for (Glob glob : globs) {
      String envelope = "";
      String subEnvelope = "";
      Glob targetGlob = localRepository.create(ImportedTransaction.TYPE);
      for (FieldAssociation association : associations) {
        if (association.mapper == null || association.mapper.field == CsvType.NOT_IMPORTED) {
          continue;
        }
        if (association.mapper.converter != null) {
          localRepository.update(targetGlob.getKey(), association.mapper.importedTransactionField,
                                 association.mapper.converter.convert(targetGlob.getValue(association.mapper.importedTransactionField),
                                                                      glob.get(association.fileField)));
        }
        else if (association.mapper.field == CsvType.ENVELOPE) {
          envelope = glob.get(association.fileField);
        }
        else if (association.mapper.field == CsvType.SUB_ENVELOPE) {
          subEnvelope = glob.get(association.fileField);
        }
      }
      String newSeries = envelope + (Strings.isNotEmpty(subEnvelope) ? ":" + subEnvelope : "");
      if (Strings.isNotEmpty(newSeries)) {
        Glob series = localRepository.getAll(ImportedSeries.TYPE, GlobMatchers.fieldEquals(ImportedSeries.NAME, newSeries)).getFirst();
        if (series == null) {
          series = localRepository.create(ImportedSeries.TYPE, value(ImportedSeries.NAME, newSeries));
        }
        int seriesId = series.get(ImportedSeries.ID);
        localRepository.update(targetGlob.getKey(), ImportedTransaction.SERIES, seriesId);
      }
      localRepository.update(targetGlob.getKey(), ImportedTransaction.ACCOUNT, accountId);
    }
    initialRepository.deleteAll(CsvMapping.TYPE);
    for (FieldAssociation associate : associations) {
      if (associate.mapper != null) {
        initialRepository.create(CsvMapping.TYPE,
                                 value(CsvMapping.FIELD_NAME, associate.fileField.getName()),
                                 value(CsvMapping.CSV_TYPE_NAME, associate.mapper.field.getName()));
      }
    }
    localRepository.commitChanges(true);
  }
}
