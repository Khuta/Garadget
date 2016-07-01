package volpis.com.garadget.models;

import android.os.Parcel;
import android.os.Parcelable;

import volpis.com.garadget.parser.GaradgetField;

public class DoorConfig implements Parcelable {


    //TODO http://stackoverflow.com/questions/2273170/how-to-write-javadoc-of-properties
    /**
     *  version, dot separated major and minor (currently 1.7)
     */
    @GaradgetField("ver")
    String version;             //
    @GaradgetField("cnt")
    int cnt;
    @GaradgetField("rdt")
    int sensorScanInterval;     //sensor scan interval in mS (200-60,000, default 1,000)
    @GaradgetField("mtt")
    int doorMovingTime;         //door moving time in mS from completely opened to completely closed (1,000 - 120,000, default 10,000)
    @GaradgetField("mot")
    int mot;
    @GaradgetField("rlt")
    int buttonPressTime;        //button press time mS, time for relay to keep contacts closed (10-2,000, default 300)
    @GaradgetField("rlp")
    int buttonPressesDelay;     //delay between consecutive button presses in mS (10-5,000 default 1,000)
    @GaradgetField("srr")
    int sensorReadsAmount;      //number of sensor reads used in averaging (1-20, default 3)
    @GaradgetField("srt")
    int reflectionThreshold;    //reflection threshold below which the door is considered open (1-80, default 25)
    @GaradgetField("aev")
    int statusAlerts;           //number serving as bitmap for enabling status alerts. See details below.
    @GaradgetField("aot")
    int openTimeout;            //alert for open timeout in seconds (0 disables, default 1,200 - 20min)
    @GaradgetField("ans")
    int nightTimeStart;         //alert for night time start in minutes from midnight (same value as ane disables, default 1320 - 10pm)
    @GaradgetField("ane")
    int nightTimeEnd;           //alert for night time end in minutes from midnight (same value as ans disables, default 360 - 6am)
    @GaradgetField("tzo")
    String tzo;                    //time zone offset or daylight savings time (DST) rule for the region. See details below.
    @GaradgetField("nme")
    String name;                //device name to be used in notifications. At reboot this value will be overwritten with the device name configured in Particle cloud. Update this value to notify the unit about the name change without reboot.

    public String getVersion() {
        return version;
    }

    public int getCnt() {
        return cnt;
    }

    public int getSensorScanInterval() {
        return sensorScanInterval;
    }

    public int getDoorMovingTime() {
        return doorMovingTime;
    }

    public int getMot() {
        return mot;
    }

    public int getButtonPressTime() {
        return buttonPressTime;
    }

    public int getButtonPressesDelay() {
        return buttonPressesDelay;
    }

    public int getSensorReadsAmount() {
        return sensorReadsAmount;
    }

    public int getReflectionThreshold() {
        return reflectionThreshold;
    }

    public int getStatusAlerts() {
        return statusAlerts;
    }

    public int getOpenTimeout() {
        return openTimeout;
    }

    public int getNightTimeStart() {
        return nightTimeStart;
    }

    public int getNightTimeEnd() {
        return nightTimeEnd;
    }

    public String getTzo() {
        return tzo;
    }

    public String getName() {
        return name;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.version);
        dest.writeInt(this.cnt);
        dest.writeInt(this.sensorScanInterval);
        dest.writeInt(this.doorMovingTime);
        dest.writeInt(this.mot);
        dest.writeInt(this.buttonPressTime);
        dest.writeInt(this.buttonPressesDelay);
        dest.writeInt(this.sensorReadsAmount);
        dest.writeInt(this.reflectionThreshold);
        dest.writeInt(this.statusAlerts);
        dest.writeInt(this.openTimeout);
        dest.writeInt(this.nightTimeStart);
        dest.writeInt(this.nightTimeEnd);
        dest.writeString(this.tzo);
        dest.writeString(this.name);
    }

    public DoorConfig() {
    }

    protected DoorConfig(Parcel in) {
        this.version = in.readString();
        this.cnt = in.readInt();
        this.sensorScanInterval = in.readInt();
        this.doorMovingTime = in.readInt();
        this.mot = in.readInt();
        this.buttonPressTime = in.readInt();
        this.buttonPressesDelay = in.readInt();
        this.sensorReadsAmount = in.readInt();
        this.reflectionThreshold = in.readInt();
        this.statusAlerts = in.readInt();
        this.openTimeout = in.readInt();
        this.nightTimeStart = in.readInt();
        this.nightTimeEnd = in.readInt();
        this.tzo = in.readString();
        this.name = in.readString();
    }

    public static final Creator<DoorConfig> CREATOR = new Creator<DoorConfig>() {
        @Override
        public DoorConfig createFromParcel(Parcel source) {
            return new DoorConfig(source);
        }

        @Override
        public DoorConfig[] newArray(int size) {
            return new DoorConfig[size];
        }
    };
}
