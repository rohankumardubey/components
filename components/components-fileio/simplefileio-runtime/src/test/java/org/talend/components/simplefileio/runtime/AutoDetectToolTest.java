package org.talend.components.simplefileio.runtime;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.talend.components.simplefileio.runtime.auto.AutoDetectTool;
import org.talend.components.simplefileio.runtime.auto.AutoDetectTool.DetectResult;
import org.talend.components.simplefileio.runtime.auto.FileFormat;

public class AutoDetectToolTest {

     @Test
     public void testCSVDetectBasic() throws IOException {
         try(InputStream is = this.getClass().getResourceAsStream("invalidColumnNumber.txt")) {
             AutoDetectTool adt = new AutoDetectTool();
             DetectResult result = adt.detect(is);
             Assert.assertEquals(FileFormat.CSV,result.getFormatType());
             System.out.println(result.getCSV_delimiter());
             
             //TODO no guess in fact,only return "\n\r", how to use it in csv reader for csv split support and also encoding?
             //so seems need to change the reader
             //In fact, is not difficult to guess the row separator is "\r" or "\n", "\r\n" after get the encoding, that is a improvement,
             //only need to check the "\r" or "\n" or "\r\n" appear sometimes, then it's the row separator, but the risk is no them in the file as other separator, that is a little risk,
             //the risk for performance to fetch to the end can be fixed by only read 10000 bytes for example
             System.out.println(result.getCSV_record_separator().equals("\n\r"));
             
             //only support to check if the first line is header
             System.out.println(result.getCSV_header_present());
             
             //TODO,a fixed value(") in fact now, advice to set it to null as default as it will make csv impossible split
             System.out.println(result.getCSV_text_enclosure_char());
             
             //TODO, a fixed value in fact now, advice to set it to null as default as it will make csv impossible split
             System.out.println(result.getCSV_escape_char());
             
             System.out.println(result.getCSV_charset());
         }
     }
     
     @Test
     public void testExcel97DetectBasic() throws IOException {
         try(InputStream is = this.getClass().getResourceAsStream("basic.xls")) {
             AutoDetectTool adt = new AutoDetectTool();
             DetectResult result = adt.detect(is);
             Assert.assertEquals(FileFormat.EXCEL97,result.getFormatType());
             System.out.println(result.getExcel_sheet());
             System.out.println(result.getExcel_header_size());
             System.out.println(result.getExcel_number_of_columns());
         }
     }
     
     @Test
     public void testExcel2007DetectBasic() throws IOException {
         try(InputStream is = this.getClass().getResourceAsStream("basic.xlsx")) {
             AutoDetectTool adt = new AutoDetectTool();
             DetectResult result = adt.detect(is);
             Assert.assertEquals(FileFormat.EXCEL2007,result.getFormatType());
             System.out.println(result.getExcel_sheet());
             System.out.println(result.getExcel_header_size());
             System.out.println(result.getExcel_number_of_columns());
         }
     }
     
     //TODO there is a bug in daikon-ee for file detect, if pass this file, will throw a exception, will fix it in daikon-ee, not support now
     @Ignore
     @Test
     public void testExcelHtmlDetectBasic() throws IOException {
         try(InputStream is = this.getClass().getResourceAsStream("sales-force.html")) {
             AutoDetectTool adt = new AutoDetectTool();
             DetectResult result = adt.detect(is);
             Assert.assertEquals(FileFormat.EXCELHTML,result.getFormatType());
         }
     }
     
     //TODO the api don't support detect avro, will support it in daikon-ee, not support now
     @Ignore
     @Test
     public void testAvroDetectBasic() throws IOException {
         try(InputStream is = this.getClass().getResourceAsStream("avropath")) {
             AutoDetectTool adt = new AutoDetectTool();
             DetectResult result = adt.detect(is);
             Assert.assertEquals(FileFormat.AVRO,result.getFormatType());
         }
     }
     
     //TODO the api don't support detect parquet, will support it in daikon-ee, not support now
     @Ignore
     @Test
     public void testParquetDetectBasic() throws IOException {
         try(InputStream is = this.getClass().getResourceAsStream("parquetpath")) {
             AutoDetectTool adt = new AutoDetectTool();
             DetectResult result = adt.detect(is);
             Assert.assertEquals(FileFormat.PARQUET,result.getFormatType());
         }
     }
    
}
