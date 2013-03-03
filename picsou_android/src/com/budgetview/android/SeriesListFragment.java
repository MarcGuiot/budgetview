package com.budgetview.android;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import com.budgetview.shared.model.SeriesValues;
import com.budgetview.shared.utils.SeriesValuesComparator;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;

import static org.globsframework.model.utils.GlobMatchers.*;

public class SeriesListFragment extends Fragment {
  public static String MONTH_PARAMETER = "com.budgetview.seriesListActivity.parameters.month";
  public static String BUDGET_AREA_PARAMETER = "com.budgetview.seriesListActivity.parameters.series";

  private Integer monthId;
  private Integer budgetAreaId;

  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

    Bundle args = getArguments();
    monthId = args.getInt(MONTH_PARAMETER, -1);
    budgetAreaId = args.getInt(BUDGET_AREA_PARAMETER, -1);

    View view = inflater.inflate(R.layout.series_list, container, false);

    ListView list = (ListView)view.findViewById(R.id.seriesList);
    list.setAdapter(new SeriesListAdapter(inflater));

    return view;
  }

  private class SeriesListAdapter extends BaseAdapter {

    private GlobList seriesValuesList;
    private LayoutInflater inflater;

    private SeriesListAdapter(LayoutInflater inflater) {
      this.inflater = inflater;
      App app = (App)getActivity().getApplication();
      seriesValuesList =
        app.getRepository()
          .getAll(SeriesValues.TYPE,
                  and(
                    fieldEquals(SeriesValues.MONTH, monthId),
                    fieldEquals(SeriesValues.BUDGET_AREA, budgetAreaId)
                  ))
          .sort(new SeriesValuesComparator());
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

      SeriesBlockView view = (SeriesBlockView)previousView;
      if (view == null) {
        view = new SeriesBlockView(getActivity(), null);
      }

      final App app = (App)getActivity().getApplication();
      final Glob seriesValues = seriesValuesList.get(i);
      Glob seriesEntity = app.getRepository().findLinkTarget(seriesValues, SeriesValues.SERIES_ENTITY);

      view.update(monthId, seriesEntity, seriesValues, getActivity());

      return view;
    }
  }
}
