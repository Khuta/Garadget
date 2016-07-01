package volpis.com.garadget.models;

import android.os.Parcel;
import android.os.Parcelable;

import volpis.com.garadget.parser.GaradgetField;

public class DoorStatus implements Parcelable {
    @GaradgetField("status")
    String status;              //is current status of the door such as open, closed etc
    @GaradgetField("time")
    String time;                //time in that status 0-119s, 2-119m, 2-47h, 2+d
    @GaradgetField("sensor")
    int reflectionRate;         //is the sensor reflection rate 0-100
    @GaradgetField("signal")
    int signalStrength;        //is WiFi signal strength in dB


    public String getStatus() {
        return status;
    }

    public String getTime() {
        return time;
    }

    public int getReflectionRate() {
        return reflectionRate;
    }

    public int getSignalStrength() {
        return signalStrength;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSignalString() {
        if (signalStrength < -85) {
            return "poor (" + signalStrength + "dB)";
        } else if (signalStrength < -59) {
            return "good (" + signalStrength + "dB)";
        } else {
            return "excellent (" + signalStrength + "dB)";
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.status);
        dest.writeString(this.time);
        dest.writeInt(this.reflectionRate);
        dest.writeInt(this.signalStrength);
    }

    public DoorStatus() {
    }

    protected DoorStatus(Parcel in) {
        this.status = in.readString();
        this.time = in.readString();
        this.reflectionRate = in.readInt();
        this.signalStrength = in.readInt();
    }

    public static final Creator<DoorStatus> CREATOR = new Creator<DoorStatus>() {
        @Override
        public DoorStatus createFromParcel(Parcel source) {
            return new DoorStatus(source);
        }

        @Override
        public DoorStatus[] newArray(int size) {
            return new DoorStatus[size];
        }
    };
}
