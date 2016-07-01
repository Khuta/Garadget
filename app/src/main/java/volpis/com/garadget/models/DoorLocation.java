package volpis.com.garadget.models;

public class DoorLocation {
    String doorId;
    double latitude;
    double longitude;
    int radius;
    boolean isEnabled;

    public DoorLocation(String doorId, double latitude, double longitude, int radius, boolean isEnabled) {
        this.doorId = doorId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.radius = radius;
        this.isEnabled = isEnabled;
    }

    public String getDoorId() {
        return doorId;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public int getRadius() {
        return radius;
    }

    public boolean isEnabled() {
        return isEnabled;
    }
}
