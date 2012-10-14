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
import com.budgetview.shared.model.BudgetAreaEntity;
import com.budgetview.shared.model.SeriesValues;
import com.budgetview.shared.model.TransactionValues;
import com.budgetview.shared.utils.AmountFormat;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.Key;

import static org.globsframework.model.utils.GlobMatchers.*;

public class TransactionListActivity extends Activity {

  public static String MONTH_PARAMETER = "transactionListActivity.parameters.month";
  public static String SERIES_VALUES_PARAMETER = "transactionListActivity.parameters.series";

  private Integer monthId;
  private Integer seriesValuesId;

  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Intent intent = getIntent();
    monthId = intent.getIntExtra(MONTH_PARAMETER, -1);
    seriesValuesId = intent.getIntExtra(SERIES_VALUES_PARAMETER, -1);

    setContentView(R.layout.transaction_list);

    TextView monthText = (TextView)findViewById(R.id.transactionMonthLabel);
    monthText.setText(Text.monthToString(monthId, getResources()));

    App app = (App)getApplication();

    Glob seriesValues =
      app.getRepository().get(Key.create(SeriesValues.TYPE, seriesValuesId));

    Glob budgetAreaEntity = app.getRepository().findLinkTarget(seriesValues, SeriesValues.BUDGET_AREA);
    String budgetAreaLabel = budgetAreaEntity.get(BudgetAreaEntity.LABEL);
    TextView budgetAreaText = (TextView)findViewById(R.id.transactionSeriesLabel);
    budgetAreaText.setText(budgetAreaLabel + " - " + seriesValues.get(SeriesValues.NAME));

    setTitle("BudgetView - " + budgetAreaLabel);

    ListView list = (ListView)findViewById(R.id.transactionList);
    list.setAdapter(new TransactionListAdapter());
  }

  private class TransactionListAdapter extends BaseAdapter {

    private GlobList transactionValuesList;

    private TransactionListAdapter() {
      App app = (App)getApplication();
      transactionValuesList =
        app.getRepository()
          .getAll(TransactionValues.TYPE, fieldEquals(TransactionValues.SERIES_VALUES, seriesValuesId))
          .sort(TransactionValues.SEQUENCE_NUMBER);
    }

    public int getCount() {
      return transactionValuesList.size();
    }

    public Object getItem(int i) {
      return transactionValuesList.get(i);
    }

    public long getItemId(int i) {
      return getItem(i).hashCode();
    }

    public View getView(int i, View previousView, ViewGroup parent) {

      View view = previousView;
      if (view == null) {
        LayoutInflater inflater = TransactionListActivity.this.getLayoutInflater();
        view = inflater.inflate(R.layout.transaction_block, parent, false);
      }

      final Glob values = transactionValuesList.get(i);
      setText(view, R.id.transactionLabel, values.get(TransactionValues.LABEL));
      setText(view, R.id.transactionAmount, values.get(TransactionValues.AMOUNT));
      setText(view, R.id.transactionDate, "15 oct");

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