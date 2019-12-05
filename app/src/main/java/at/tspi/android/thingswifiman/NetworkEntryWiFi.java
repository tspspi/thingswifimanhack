package at.tspi.android.thingswifiman;

public class NetworkEntryWiFi extends NetworkEntry {
    private int rssi;
    private int level;

    public NetworkEntryWiFi(String name, String capabilities, String description, int level, int rssi) {
        super(name, capabilities, description);
        this.level = level;
        this.rssi = rssi;
    }

    public int getRSSI() { return this.rssi; }
    public int getLevel() { return this.level; }
}
