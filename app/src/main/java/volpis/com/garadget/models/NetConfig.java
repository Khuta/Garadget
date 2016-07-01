package volpis.com.garadget.models;

import android.os.Parcel;
import android.os.Parcelable;

import volpis.com.garadget.parser.GaradgetField;

public class NetConfig implements Parcelable {
    @GaradgetField("ip")
    String ipAddress;
    @GaradgetField("snet")
    String subnet;
    @GaradgetField("gway")
    String gateway;
    @GaradgetField("mac")
    String macAddress;
    @GaradgetField("ssid")
    String ssid;

    public String getIpAddress() {
        return ipAddress;
    }

    public String getSubnet() {
        return subnet;
    }

    public String getGateway() {
        return gateway;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public String getSsid() {
        return ssid;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.ipAddress);
        dest.writeString(this.subnet);
        dest.writeString(this.gateway);
        dest.writeString(this.macAddress);
        dest.writeString(this.ssid);
    }

    public NetConfig() {
    }

    protected NetConfig(Parcel in) {
        this.ipAddress = in.readString();
        this.subnet = in.readString();
        this.gateway = in.readString();
        this.macAddress = in.readString();
        this.ssid = in.readString();
    }

    public static final Creator<NetConfig> CREATOR = new Creator<NetConfig>() {
        @Override
        public NetConfig createFromParcel(Parcel source) {
            return new NetConfig(source);
        }

        @Override
        public NetConfig[] newArray(int size) {
            return new NetConfig[size];
        }
    };
}
