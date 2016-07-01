package volpis.com.garadget.models;

import com.google.gson.annotations.SerializedName;

public class DataPayload {
    @SerializedName("name")
    private String mName;

    @SerializedName("type")
    private String mType;

    @SerializedName("data")
    private String mData;

    public String getName() {
        return mName;
    }

    public String getType() {
        return mType;
    }

    public String getData() {
        return mData;
    }
}