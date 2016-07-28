package volpis.com.garadget.parser;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import java.lang.reflect.Field;
import volpis.com.garadget.screens.MainActivity;

public class GaradgetParser {

    public static <T> T parse(final Context context, String stringData, Class<T> type) {

        T resultObject = null;
        try {
            resultObject = type.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
            return null;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }

        Field[] fields = type.getDeclaredFields();

        String[] values = stringData.split("\\|");
        for (String value : values) {
            String[] re = value.split("=");
            if (re.length > 1) {
                String fieldName = value.split("=")[0];
                String fieldValue = value.split("=")[1];
                for (Field field : fields) {
                    if (field.isAnnotationPresent(GaradgetField.class)) {
                        GaradgetField serializedName = field.getAnnotation(GaradgetField.class);
                        if (serializedName.value().equals(fieldName)) {
                            field.setAccessible(true);
                            try {
                                if (field.getType().getSimpleName().equals("int")) {
                                    field.set(resultObject, Integer.valueOf(fieldValue));
                                } else if (field.getType().getSimpleName().equals("double")) {
                                    field.set(resultObject, Double.valueOf(fieldValue));
                                } else if (field.getType().getSimpleName().equals("long")) {
                                    field.set(resultObject, Long.valueOf(fieldValue));
                                } else if (field.getType().isAssignableFrom(String.class)) {
                                    field.set(resultObject, fieldValue);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            break;
                        }
                    }
                }
            } else {
                final String finalValue = value;
                if (!((MainActivity) context).isFinishing())
                    ((MainActivity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AlertDialog alertDialog = new AlertDialog.Builder(context).create();
                            alertDialog.setTitle("Parse error");
                            alertDialog.setMessage(finalValue);
                            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    });
                            alertDialog.show();
                        }
                    });
                break;

            }
        }
        return resultObject;
    }

}
