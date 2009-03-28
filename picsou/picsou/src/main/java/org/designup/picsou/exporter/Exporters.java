package org.designup.picsou.exporter;

import org.designup.picsou.exporter.tsv.TsvExporter;
import org.designup.picsou.exporter.ofx.OfxExporter;
import org.globsframework.utils.exceptions.ItemNotFound;
import org.globsframework.utils.directory.Directory;

import java.util.Arrays;
import java.util.List;

public class Exporters {

  private List<Exporter> list;

  public Exporters(Directory directory) {
    this.list = Arrays.asList(new OfxExporter(), new TsvExporter(directory));
  }

  public List<Exporter> getAll() {
    return list;
  }

  public Exporter get(String type) throws ItemNotFound {
    for (Exporter exporter : list) {
      if (exporter.getType().equals(type)) {
        return exporter;
      }
    }
    throw new ItemNotFound("Unknown exporter type: " + type);
  }
}
