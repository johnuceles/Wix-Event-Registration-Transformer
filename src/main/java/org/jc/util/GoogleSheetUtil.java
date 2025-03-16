package org.jc.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GoogleSheetUtil {

    public static List<List<Object>> convertToList(Object obj) {
        List<List<Object>> table = new ArrayList<>();
        List<Object> row = new ArrayList<>();

        processObject(obj, row);
        table.add(row); // Store as a row in the table

        return table;
    }

    private static void processObject(Object obj, List<Object> row) {
        if (obj == null) {
            row.add("");  // Empty cell for null values
            return;
        }

        if (obj instanceof String || obj instanceof Number || obj instanceof Boolean) {
            row.add(obj); // Directly add simple types
        } else if (obj instanceof List<?>) {
            // Flatten lists (arrays)
            List<?> list = (List<?>) obj;
            for (Object item : list) {
                processObject(item, row);
            }
        } else if (obj instanceof Map<?, ?>) {
            // Flatten maps (if needed)
            Map<?, ?> map = (Map<?, ?>) obj;
            for (Object value : map.values()) {
                processObject(value, row);
            }
        } else {
            // If it's a custom object, process all its fields recursively
            Field[] fields = obj.getClass().getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true); // Enable access to private fields
                try {
                    processObject(field.get(obj), row);
                } catch (IllegalAccessException e) {
                    row.add(""); // Handle inaccessible fields
                }
            }
        }
    }
}