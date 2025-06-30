package org.example.datalineage.udfoption;

import org.apache.spark.sql.*;
import org.apache.spark.sql.api.java.UDF1;
import org.apache.spark.sql.api.java.UDF2;
import org.apache.spark.sql.types.DataTypes;
import scala.collection.JavaConverters;
import scala.collection.Seq;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static org.apache.spark.sql.functions.*;
import static org.example.datalineage.udfoption.LineageUtils.coalesceWithLineage;
import static org.example.datalineage.udfoption.LineageUtils.coalesceWithLineage2;
import static org.example.datalineage.udfoption.LineageUtils.getLineageSchema;

public class UdfOptionLineageMain {
    public static void main(String[] argz) throws URISyntaxException {
        System.setProperty("hadoop.home.dir", "C:\\Users\\thebr\\data\\hadoop\\hadoop-3.3.6");

        SparkSession spark = SparkSession.builder()
                .appName("Lineage Tracker")
                .master("local[*]")
                .config("spark.ui.enabled", "false")
                .getOrCreate();

        Map<String, List<String>> lineageMap = Map.of(
                "phone", List.of("phone", "alt_phone"),
                "email_verified", List.of("email", "backup_email")
        );

        spark.udf().register(
                "trackCase",
                (UDF2<String, String, String>) LineageUtils::trackCase,
                DataTypes.StringType);

        spark.udf().register(
                "coalesceWithLineage",
                (UDF2<String, String, Row>) LineageUtils::coalesceWithLineage,
                LineageUtils.getLineageSchema()
        );

        UDF2<Seq<String>, Seq<Object>, Row> udf = (names, args) ->
                LineageUtils.coalesceWithLineage2(
                        JavaConverters.seqAsJavaList(names),
                        JavaConverters.seqAsJavaList(args)
                );



        spark.udf().register(
                "coalesceWithLineage2",
                udf,
                LineageUtils.getLineageSchema()
        );


        spark.udf().register(
                "ageCaseWithLineage",
                (UDF1<Long, Row>) LineageUtils::ageCaseWithLineage,
                LineageUtils.getLineageSchema()
        );

        /*

         */
        // Optionally run a query to test
        //spark.sql("SELECT trackCase('ACTIVE', 'UNKNOWN') AS result").show();

        URL resource = UdfOptionLineageMain.class.getClassLoader().getResource("datasets/lineage.json");
        String path = Paths.get(resource.toURI()).toString();

        /*

         */
        Dataset<Row> sqlDataset = spark.read()
                .option("multiline", true)
                .json(path)
                .cache(); //Cache pulls to memory immediately
        sqlDataset.show();

        Dataset<Row> enrichedDataset = sqlDataset.select(
                col("id"),
                callUDF("ageCaseWithLineage"
                            , col("age"))
                        .getField("value")
                        .alias("age"),
                callUDF("coalesceWithLineage2"
                            , array(lit("phone"), lit("alt_phone"), lit("na"))
                            , array(col("phone"), col("alt_phone"), lit("na")))
                        .getField("value")
                        .alias("contact_info")
//                callUDF("ageCaseWithLineage", col("age")).getField("value").alias("age_category"),
//                callUDF("ageCaseWithLineage", col("age")).getField("lineage").alias("age_logic")
        );

        enrichedDataset.show(false);

        spark.stop();
    }
}