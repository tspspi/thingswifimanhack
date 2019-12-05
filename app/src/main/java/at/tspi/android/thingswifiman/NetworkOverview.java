/*
    Android Things Network Manager

    This network manager is licensed under BSD license.
    Details see ...
 */

package at.tspi.android.thingswifiman;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import at.tspi.android.autschank2.R;

public class NetworkOverview extends AppCompatActivity {
    private static final String TAG = NetworkOverview.class.getCanonicalName();
    private WifiManager wifiManager = null;
    private BroadcastReceiver wifiScanResultReceiver = null;

    private List<WifiConfiguration> configuredNetworks = null;

    private NetworkEntryListAdapter listAdapter = null;

    private Handler handler = new Handler();
    private Runnable updateJob = new Runnable() {
        @Override
        public void run() {
            if(wifiManager != null) {
                if(!wifiManager.startScan()) {
                    Log.e(TAG, "Failed to start WiFi scan");
                }
            }

            handler.postDelayed(this, 5000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network_overview);
    }

    private void updateListConfiguredNetworks() {
        if(wifiManager != null) {
            configuredNetworks = wifiManager.getConfiguredNetworks();
        } else {
            configuredNetworks = new ArrayList<WifiConfiguration>();
        }
    }

    private void updateList(List<ScanResult> results) {
        // Add scanned networks to list
        ArrayList<NetworkEntry> newList = new ArrayList<NetworkEntry>();

        WifiInfo winfo = null;
        if(wifiManager != null) {
            winfo = wifiManager.getConnectionInfo();
        }

        for(ScanResult res : results) {
            if(res.SSID.trim().equals("")) {
                Log.d(TAG, "Discovered empty SSID");
                continue;
            }
            Log.d(TAG, "Discovered "+res.SSID);
            String description = "";
            String currentSsid = null;
            if(winfo != null) {
                currentSsid = winfo.getSSID();
                if (currentSsid.startsWith("\"") && currentSsid.endsWith("\"") && (currentSsid.length() >= 2)) {
                    currentSsid = currentSsid.substring(1, currentSsid.length() - 1);
                }
            }

            if(winfo != null) {
                if(res.SSID.equals(currentSsid)) {
                    description = Formatter.formatIpAddress(winfo.getIpAddress());
                }
            }
            NetworkEntryWiFi wifiEnt = new NetworkEntryWiFi(res.SSID, res.capabilities, description, WifiManager.calculateSignalLevel(res.level, 5), res.level);
            if(winfo != null) {
                if(res.SSID.equals(currentSsid)) {
                    wifiEnt.setConnected(true);
                }
            }
            newList.add(wifiEnt);
        }

        if(listAdapter != null) {
            listAdapter.clear();
            listAdapter.addAll(newList);
            listAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        final Activity ctx = this;

        wifiManager = (WifiManager)this.getSystemService(Context.WIFI_SERVICE);
        if(wifiManager == null) {
            Log.d(TAG, "Failed to get access to WiFi manager");
            return;
        }

        // Query list of configured networks


        ListView lvNetworks = findViewById(R.id.lstNetworks);
        listAdapter = new NetworkEntryListAdapter(this);
        lvNetworks.setAdapter(listAdapter);

        lvNetworks.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parentView, View view, int position, long id) {
                final NetworkEntry ne = (NetworkEntry)parentView.getAdapter().getItem(position);

                final Dialog diagConfig = new Dialog(ctx);
                diagConfig.requestWindowFeature(Window.FEATURE_NO_TITLE);
                diagConfig.setCancelable(true);
                diagConfig.setContentView(R.layout.dialog_wifi_password);

                TextView tvSSID = diagConfig.findViewById(R.id.tvSSID);
                final EditText txtPasswordPSK = diagConfig.findViewById(R.id.txtPSK);
                Button btnBack = diagConfig.findViewById(R.id.btnBack);
                Button btnSave = diagConfig.findViewById(R.id.btnOk);

                tvSSID.setText(ne.getName());
                btnBack.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        diagConfig.dismiss();
                    }
                });

                btnSave.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.d(TAG, "Building configuration for "+ne.getName()+" with password "+txtPasswordPSK.getText());
                        WifiConfiguration wifiConfiguration = new WifiConfiguration();
                        wifiConfiguration.SSID = "\"" + ne.getName() + "\"";
                        wifiConfiguration.preSharedKey = "\"" + txtPasswordPSK.getText() + "\"";

                        WifiManager manager = (WifiManager)getSystemService(WIFI_SERVICE);
                        int newNetworkId = manager.addNetwork(wifiConfiguration);
                        Log.d(TAG, "Added configuration for "+ne.getName());
                        Log.d(TAG, "New network ID "+newNetworkId);
                        if(newNetworkId != -1) {
                            wifiManager.disconnect();
                            wifiManager.enableNetwork(newNetworkId, true);
                            wifiManager.reconnect();
                        }

                        diagConfig.dismiss();
                    }
                });

                diagConfig.show();
            }
        });

        Button btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ctx.finish();
            }
        });

        wifiScanResultReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "Received broadcast (WiFi scanning)");
                boolean scanSuccessful = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false);

                List<ScanResult> results;
                if (scanSuccessful) {
                    Log.d(TAG, "Got scan results");
                    results = wifiManager.getScanResults(); // Current results
                    // Update list
                } else {
                    Log.e(TAG, "WiFi scanning failed");
                    results = wifiManager.getScanResults(); // Old results
                    // Failed to scan ...
                }
                updateList(results);
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Register our receiver
        // IntentFilter intentFilter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        IntentFilter intentFilter = new IntentFilter("android.net.wifi.SCAN_RESULTS");
        this.registerReceiver(wifiScanResultReceiver, intentFilter);
        Log.d(TAG, "Registered result receiver");

        if(wifiManager.isWifiEnabled()) {
            // Only start scanning if WiFi is enabled, else we only display the wired connection
            if (!wifiManager.startScan()) {
                Log.e(TAG, "Failed to start WiFi scan");
            }
            handler.postDelayed(this.updateJob, 5000);
        } else {
            Log.w(TAG, "WiFi seems to be disabled ...");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        this.unregisterReceiver(wifiScanResultReceiver);
        Log.d(TAG, "Unregistered result receiver");
    }

}
