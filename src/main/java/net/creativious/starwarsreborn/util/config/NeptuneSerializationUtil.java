package net.creativious.starwarsreborn.util.config;

import java.util.ArrayList;
import java.util.List;

public class NeptuneSerializationUtil {

    private static List<Class<?>> supportedClasses = new ArrayList<>();

    static {
        supportedClasses.add(Boolean.class);
        supportedClasses.add(Integer.class);
        supportedClasses.add(Long.class);
        supportedClasses.add(Double.class);
        supportedClasses.add(String.class);
        supportedClasses.add(int.class);
        supportedClasses.add(long.class);
        supportedClasses.add(double.class);
        supportedClasses.add(boolean.class);
    }

    public static boolean isSupportedClass(Class<?> clazz) {
        return supportedClasses.contains(clazz);
    }

    public static String getSerializedValue(Object value) {
        return (isSupportedClass(value.getClass())) ? value.toString() : "";
    }

    public static Object getDeserializedValue(String value, Class<?> clazz) {
        if (clazz.equals(Boolean.class)) {
            return Boolean.parseBoolean(value);
        } else if (clazz.equals(Integer.class)) {
            return Integer.parseInt(value);
        } else if (clazz.equals(Long.class)) {
            return Long.parseLong(value);
        } else if (clazz.equals(Double.class)) {
            return Double.parseDouble(value);
        } else if (clazz.equals(String.class)) {
            return value;
        } else if (clazz.equals(int.class)) {
            return Integer.parseInt(value);
        } else if (clazz.equals(long.class)) {
            return Long.parseLong(value);
        } else if (clazz.equals(double.class)) {
            return Double.parseDouble(value);
        } else if (clazz.equals(boolean.class)) {
            return Boolean.parseBoolean(value);
        }
        return null;
    }
}
