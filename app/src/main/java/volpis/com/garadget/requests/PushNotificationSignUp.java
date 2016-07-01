package volpis.com.garadget.requests;

import android.content.Context;
import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.JsonSyntaxException;
import org.json.JSONArray;
import org.json.JSONException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import io.particle.android.sdk.cloud.ParticleCloudSDK;
import volpis.com.garadget.R;
import volpis.com.garadget.utils.SharedPreferencesUtils;


public class PushNotificationSignUp extends Request<JSONArray> {
    private String mAction;
    private String mDeviceId;
    private final Response.Listener<JSONArray> listener;

    public PushNotificationSignUp(Context context, String deviceid, String action, Response.Listener<JSONArray> listener, Response.ErrorListener errorListener) {
        super(Method.POST, context.getString(R.string.push_url), errorListener);
        mAction = action;
        this.listener = listener;
        this.mDeviceId = deviceid;
    }

    @Override
    protected Response<JSONArray> parseNetworkResponse(NetworkResponse response) {
        try {
            String json = new String(
                    response.data,
                    HttpHeaderParser.parseCharset(response.headers));
            JSONArray jsonObject = new JSONArray(json);
            return Response.success(jsonObject,
                    HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (JsonSyntaxException e) {
            return Response.error(new ParseError(e));
        } catch (JSONException e) {
            return Response.error(new ParseError(e));
        }
    }

    @Override
    protected void deliverResponse(JSONArray jsonObject) {
        listener.onResponse(jsonObject);
    }

    @Override
    public Map<String, String> getParams() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("action", mAction);
        params.put("platform", "gcm");
        params.put("subscriber", SharedPreferencesUtils.getInstance().getRegistrationToken());
        params.put("device", mDeviceId);
        params.put("authtoken", ParticleCloudSDK.getCloud().getAccessToken());
        return params;
    }

    @Override
    protected VolleyError parseNetworkError(VolleyError volleyError) {
        return super.parseNetworkError(volleyError);
    }
}
