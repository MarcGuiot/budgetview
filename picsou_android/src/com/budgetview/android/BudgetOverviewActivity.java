package com.budgetview.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import com.budgetview.android.components.GaugeView;
import com.budgetview.shared.model.BudgetAreaEntity;
import com.budgetview.shared.model.BudgetAreaValues;
import com.budgetview.shared.model.MobileModel;
import com.budgetview.shared.utils.AmountFormat;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.repository.DefaultGlobRepository;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.xml.XmlGlobParser;

import java.io.InputStream;
import java.io.InputStreamReader;

public class BudgetOverviewActivity extends Activity {

  public static final String EXTRA_MESSAGE = "com.budgetview.android.MESSAGE";

  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    GlobRepository repository = loadRepository();

    setContentView(R.layout.budget_overview);

    ListView list = (ListView)findViewById(R.id.budgetAreaList);
    list.setAdapter(new BudgetAreaListAdapter(repository));
  }

  private GlobRepository loadRepository() {
    GlobRepository repository = App.getRepository();
    InputStream inputStream = getResources().openRawResource(R.raw.globsdata);
    XmlGlobParser.parse(MobileModel.get(), repository, new InputStreamReader(inputStream), "globs");
    return repository;
  }

  private class BudgetAreaListAdapter extends BaseAdapter {

    private GlobList budgetAreaValues;
    private GlobRepository repository;

    private BudgetAreaListAdapter(GlobRepository repository) {
      this.repository = repository;
      budgetAreaValues = repository.getAll(BudgetAreaValues.TYPE, GlobMatchers.fieldEquals(BudgetAreaValues.MONTH, 201207));
    }

    public int getCount() {
      return budgetAreaValues.size();
    }

    public Object getItem(int i) {
      return budgetAreaValues.get(i);
    }

    public long getItemId(int i) {
      return getItem(i).hashCode();
    }

    public View getView(int i, View previousView, ViewGroup parent) {

      View view = previousView;
      if (view == null) {
        LayoutInflater inflater = BudgetOverviewActivity.this.getLayoutInflater();
        view = inflater.inflate(R.layout.budget_area, parent, false);
      }

      final Glob values = budgetAreaValues.get(i);
      Glob entity = repository.findLinkTarget(values, BudgetAreaValues.BUDGET_AREA);

      view.setOnClickListener(new View.OnClickListener() {
        public void onClick(View view) {
          Intent intent = new Intent(BudgetOverviewActivity.this, SeriesListActivity.class);
          intent.putExtra(SeriesListActivity.MONTH_PARAMETER, values.get(BudgetAreaValues.MONTH));
          intent.putExtra(SeriesListActivity.BUDGET_AREA_PARAMETER, values.get(BudgetAreaValues.BUDGET_AREA));
          startActivity(intent);
        }
      });


      setText(view, R.id.budgetAreaLabel, entity.get(BudgetAreaEntity.LABEL));
      setText(view, R.id.budgetAreaActual, values.get(BudgetAreaValues.ACTUAL));
      setText(view, R.id.budgetAreaPlanned, values.get(BudgetAreaValues.INITIALLY_PLANNED));

      GaugeView gaugeView = (GaugeView)view.findViewById(R.id.budgetAreaGauge);
      gaugeView.getModel()
        .setValues(values.get(BudgetAreaValues.ACTUAL),
                   values.get(BudgetAreaValues.INITIALLY_PLANNED),
                   values.get(BudgetAreaValues.OVERRUN),
                   values.get(BudgetAreaValues.REMAINDER),
                   "", false);
      Log.d("BudgetOverviewActivity", "Updated " + entity.get(BudgetAreaEntity.LABEL)+ " with " + values.get(BudgetAreaValues.INITIALLY_PLANNED));
      return view;
    }

    private void setText(View view, int textId, Double value) {
      setText(view, textId, AmountFormat.DECIMAL_FORMAT.format(value));
    }

    private void setText(View view, int textId, String text) {
      TextView textView = (TextView)view.findViewById(textId);
      textView.setText(text);
    }
  }
}
