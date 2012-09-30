package com.budgetview.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.budgetview.android.components.GaugeView;
import com.budgetview.shared.model.BudgetAreaEntity;
import com.budgetview.shared.model.SeriesValues;
import com.budgetview.shared.utils.AmountFormat;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;

import static org.globsframework.model.utils.GlobMatchers.*;

public class SeriesListActivity extends Activity {

  public static String MONTH_PARAMETER = "seriesListActivity.parameters.month";
  public static String BUDGET_AREA_PARAMETER = "seriesListActivity.parameters.series";
  private Integer monthId;
  private Integer seriesId;

  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Intent intent = getIntent();
    monthId = intent.getIntExtra(MONTH_PARAMETER, -1);
    seriesId = intent.getIntExtra(BUDGET_AREA_PARAMETER, -1);

    setContentView(R.layout.series_list);

    ListView list = (ListView)findViewById(R.id.seriesList);
    list.setAdapter(new SeriesListAdapter());
  }

  private class SeriesListAdapter extends BaseAdapter {

    private GlobList seriesValuesList;

    private SeriesListAdapter() {
      seriesValuesList =
        App.getRepository().getAll(SeriesValues.TYPE,
                                   and(
                                     fieldEquals(SeriesValues.MONTH, monthId),
                                     fieldEquals(SeriesValues.BUDGET_AREA, seriesId)
                                   ));
    }

    public int getCount() {
      return seriesValuesList.size();
    }

    public Object getItem(int i) {
      return seriesValuesList.get(i);
    }

    public long getItemId(int i) {
      return getItem(i).hashCode();
    }

    public View getView(int i, View previousView, ViewGroup parent) {

      View view = previousView;
      if (view == null) {
        LayoutInflater inflater = SeriesListActivity.this.getLayoutInflater();
        view = inflater.inflate(R.layout.series, parent, false);
      }

      final Glob values = seriesValuesList.get(i);
      setText(view, R.id.seriesLabel, values.get(SeriesValues.NAME));
      setText(view, R.id.seriesActual, values.get(SeriesValues.AMOUNT));
      setText(view, R.id.seriesPlanned, values.get(SeriesValues.PLANNED_AMOUNT));

      GaugeView gaugeView = (GaugeView)view.findViewById(R.id.seriesGauge);
      gaugeView.getModel()
        .setValues(values.get(SeriesValues.AMOUNT, 0.00),
                   values.get(SeriesValues.PLANNED_AMOUNT, 0.00),
                   values.get(SeriesValues.OVERRUN_AMOUNT, 0.00),
                   values.get(SeriesValues.REMAINING_AMOUNT, 0.00),
                   "", false);
      return view;
    }

    private void setText(View view, int textId, Double value) {
      String text = (value == null) ? "-" : AmountFormat.DECIMAL_FORMAT.format(value);
      setText(view, textId, text);
    }

    private void setText(View view, int textId, String text) {
      TextView textView = (TextView)view.findViewById(textId);
      textView.setText(text);
    }
  }


}