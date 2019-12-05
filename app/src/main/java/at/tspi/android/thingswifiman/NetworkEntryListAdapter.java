package at.tspi.android.thingswifiman;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import at.tspi.android.autschank2.R;

public class NetworkEntryListAdapter extends ArrayAdapter<NetworkEntry> {
    public NetworkEntryListAdapter(Context context) {
        super(context, R.layout.networkoverviewitem);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        NetworkEntry ent = getItem(position);
        if(convertView == null) {
            LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.networkoverviewitem, parent, false);
        }

        // Fill entry
        if(ent instanceof NetworkEntryWiFi) {
            NetworkEntryWiFi wifi = (NetworkEntryWiFi)ent;

            // Fill for WiFi network
            TextView tvSSID = convertView.findViewById(R.id.tvSSID);
            TextView tvDetails = convertView.findViewById(R.id.tvDetails);

            if(tvSSID != null) {
                tvSSID.setText(wifi.getName());
            }
            if(tvDetails != null) {
                if (wifi.getDescription() != null) {
                    tvDetails.setText(wifi.getDescription() + "\n" + wifi.getCapabilities());
                } else {
                    tvDetails.setText(wifi.getCapabilities());
                }
            }

            // Select image for level
            ImageView ivLevel = convertView.findViewById(R.id.ivLevel);

            if(ivLevel != null) {
                switch (wifi.getLevel()) { // We expect levels 0-4 (i.e. 5 levels)
                    case 0:
                        ivLevel.setImageDrawable(getContext().getDrawable(R.drawable.ic_signal_wifi_0_bar_black_48dp));
                        break;
                    case 1:
                        ivLevel.setImageDrawable(getContext().getDrawable(R.drawable.ic_signal_wifi_1_bar_black_48dp));
                        break;
                    case 2:
                        ivLevel.setImageDrawable(getContext().getDrawable(R.drawable.ic_signal_wifi_2_bar_black_48dp));
                        break;
                    case 3:
                        ivLevel.setImageDrawable(getContext().getDrawable(R.drawable.ic_signal_wifi_3_bar_black_48dp));
                        break;
                    case 4:
                        ivLevel.setImageDrawable(getContext().getDrawable(R.drawable.ic_signal_wifi_4_bar_black_48dp));
                        break;
                    default:
                        if (wifi.getLevel() > 4) {
                            ivLevel.setImageDrawable(getContext().getDrawable(R.drawable.ic_signal_wifi_4_bar_black_48dp));
                            break;
                        } else {
                            ivLevel.setImageDrawable(getContext().getDrawable(R.drawable.ic_signal_wifi_off_black_48dp));
                            break;
                        }
                }
            }
        }

        return convertView;
    }
}
