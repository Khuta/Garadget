package volpis.com.garadget.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.AbstractMap;
import java.util.LinkedHashMap;
import java.util.Map;


public class SpinnerMapAdapter extends BaseAdapter {
    private Context mContext;
    private int mResource;
    private int mDropDownResource;
    private LinkedHashMap<String, ?> mObjects;

    public SpinnerMapAdapter(Context context, int resource, LinkedHashMap<String, ?> objects, int selectedPosition) {
        this.mContext = context;
        this.mResource = resource;
        this.mObjects = objects;
    }

    @Override
    public int getCount() {
        return mObjects.size();
    }

    @Override
    public Object getItem(int position) {
        return new AbstractMap.SimpleEntry(mObjects.keySet().toArray()[position], mObjects.values().toArray()[position]);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row = inflater.inflate(mResource, parent, false);
        TextView tvTitle = (TextView) row.findViewById(android.R.id.text1);
        String value = (String) ((Map.Entry) getItem(position)).getKey();
        tvTitle.setText(value);
        return row;
    }

    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row = inflater.inflate(mDropDownResource, parent, false);
        TextView tvTitle = (TextView) row.findViewById(android.R.id.text1);
        String value = (String) ((Map.Entry) getItem(position)).getKey();
        tvTitle.setText(value);
        return row;
    }

    public void setDropDownViewResource(int resource) {
        this.mDropDownResource = resource;
    }
}

