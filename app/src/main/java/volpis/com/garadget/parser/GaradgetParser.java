package volpis.com.garadget.parser;

import java.lang.reflect.Field;

public class GaradgetParser {

    public static <T> T parse(String stringData, Class<T> type) {

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
        }
        return resultObject;
    }

}
