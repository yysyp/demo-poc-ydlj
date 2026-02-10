package ps.demo.jpademo.common;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.hadoop.conf.Configuration;
import org.apache.parquet.avro.AvroParquetReader;
import org.apache.parquet.avro.AvroParquetWriter;
import org.apache.parquet.hadoop.ParquetReader;
import org.apache.parquet.hadoop.ParquetWriter;
import org.apache.parquet.hadoop.util.HadoopInputFile;
import org.apache.parquet.hadoop.util.HadoopOutputFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParquetTool {



    public static void main(String[] args) throws IOException {
        //https://github.com/steveloughran/winutils/tree/master/hadoop-3.0.0/bin  
        //System.setProperty("hadoop.home.dir", System.getProperty("user.dir") + "/src/test/resource/hadoop-2.6.0");
        String path = "target/parquet/test-output.parquet";
        Files.deleteIfExists(
                Paths.get(path)
        );

        Schema schema = new Schema.Parser().parse(
                """
                   {"type": "record", "name": "TestRd", "fields": [{"name": "field1", "type": "string"}]}     
                   """
        );

        List<Map<String, Object>> records = List.of(Map.of("field1", "value1"));
        writeParquet(path, schema, records, new Configuration());

        List<Map<String, Object>> readRecords = readParquet(path, new Configuration());
        System.out.println(readRecords);


    }

    public static List<Map<String, Object>> readParquet(String path, Configuration configuration) {
        List<Map<String, Object>> records = new ArrayList<>();
        try (ParquetReader<GenericRecord> reader = AvroParquetReader.<GenericRecord>builder(
                HadoopInputFile.fromPath(new org.apache.hadoop.fs.Path(path), configuration))
                .withConf(configuration).build()) {
            GenericRecord record;
            while ((record = reader.read()) != null) {
                Map<String, Object> recordMap = new HashMap<>();
                GenericRecord finalRecord = record;
                record.getSchema().getFields().forEach(field ->
                    recordMap.put(field.name(), finalRecord.get(field.name())));

                records.add(recordMap);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return records;
    }

    public static void writeParquet(String path, Schema schema, List<Map<String, Object>> records, Configuration configuration) {
        org.apache.hadoop.fs.Path fsPath = new org.apache.hadoop.fs.Path(path);
        System.out.println("fsPath " + fsPath.toUri());
        try (ParquetWriter<GenericRecord> writer = AvroParquetWriter
                .<GenericRecord>builder(HadoopOutputFile.fromPath(fsPath, configuration))
                .withSchema(schema).withConf(configuration).build()) {

            for(Map<String, Object> record : records) {
                GenericRecord genericRecord = new GenericData.Record(schema);
                record.forEach(genericRecord::put);
                writer.write(genericRecord);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
