package volpis.com.garadget.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import volpis.com.garadget.R;

public class DrawerAdapter extends ArrayAdapter<String> {

    private Context mContext;
    private LayoutInflater mInflater;
    private ArrayList<String> drawerItems;

    public DrawerAdapter(Context context, int resource, ArrayList<String> drawerItems) {
        super(context, resource, drawerItems);
        this.drawerItems = drawerItems;
        mContext = context;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
                    convertView = mInflater.inflate(R.layout.item_drawer, null);
                    holder.textView = (TextView) convertView.findViewById(R.id.tv_title);
                    holder.llItem = (LinearLayout) convertView.findViewById(R.id.ll_item);
                    convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.textView.setText(drawerItems.get(position));
        return convertView;
    }

    public static class ViewHolder {
        public TextView textView;
        public LinearLayout llItem;
    }

}
