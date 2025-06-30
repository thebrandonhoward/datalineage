package org.example.datalineage.udfoption;

import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructType;

import java.util.List;

public class LineageUtils <T> {

    public static Row coalesceWithLineage(String phone, String altPhone) {
        if (phone != null) return RowFactory.create(phone, "phone");
        if (altPhone != null) return RowFactory.create(altPhone, "alt_phone");
        return RowFactory.create(null, "null");
    }

    public static Row ageCaseWithLineage(Long age) {
        System.out.printf("User Defined Function Processing %s for field: %s\n", "ageCaseWithLineage", age);

        if (age == null) {
            return RowFactory.create(null, "null");
        }
        if (age == 21) {
            return RowFactory.create("exactly 21", "age == 21");
        }
        if (age > 21) {
            return RowFactory.create("above 21", "age > 21");
        }

        return RowFactory.create("below 21", "age < 21");
    }

    public static <T> Row coalesceWithLineage2(List<String> names, List<T> fields) {
        System.out.printf("User Defined Function Processing %s for names: %s and fields: %s -> ", "coalesceWithLineage2", names, fields);

        for (int i = 0; i < fields.size(); i++) {
            T field = fields.get(i);

            if (field != null) {
                System.out.printf("Matched: %s\n", (i < names.size() ? names.get(i) : fields.get(i)));

                return RowFactory.create(field.toString(), "col_" + i);
            }
        }

        System.out.print("Matched: na");

        return RowFactory.create(null, "NA");
    }

    public static StructType getLineageSchema() {
        return new StructType()
                .add("value", DataTypes.StringType)
                .add("lineage", DataTypes.StringType);
    }


    public static String trackCase(String status, String fallback) {
        String result;
        String lineage;

        if ("ACTIVE".equalsIgnoreCase(status)) {
            result = "A";
            lineage = "CASE:ACTIVE";
        } else if ("INACTIVE".equalsIgnoreCase(status)) {
            result = "I";
            lineage = "CASE:INACTIVE";
        } else {
            result = fallback;
            lineage = "CASE:FALLBACK";
        }

        KafkaLineageEmitter.emit("lineage-topic", lineage);
        // Kafka integration

        return result;
    }
}