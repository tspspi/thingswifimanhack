package at.tspi.android.thingswifiman;

public abstract class NetworkEntry {
    private String name;
    private String capabilities;
    private String description;

    private boolean isConnected;

    public NetworkEntry(String name, String capabilities, String description) {
        this.name = name;
        this.description = description;
        this.capabilities = capabilities;
        this.isConnected = false;
    }

    public String getName() { return this.name; }
    public String getDescription() { return this.description; }
    public String getCapabilities() { return this.capabilities; }
    public boolean isConnected() { return this.isConnected; }

    public void setConnected(boolean isConnected) { this.isConnected = isConnected; }
}
