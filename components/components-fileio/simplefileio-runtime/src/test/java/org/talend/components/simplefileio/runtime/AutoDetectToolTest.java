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
             System.out.println(result.getCSV_record_separator());
             
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
     
     //TODO there is a bug in daikon-ee for file detect, if pass this file, will throw a exception, will fix it in daikon-ee
     @Ignore
     @Test
     public void testExcelHtmlDetectBasic() throws IOException {
         try(InputStream is = this.getClass().getResourceAsStream("sales-force.html")) {
             AutoDetectTool adt = new AutoDetectTool();
             DetectResult result = adt.detect(is);
             Assert.assertEquals(FileFormat.EXCELHTML,result.getFormatType());
         }
     }
     
     //TODO the api don't support detect avro, will support it in daikon-ee
     @Ignore
     @Test
     public void testAvroDetectBasic() throws IOException {
         try(InputStream is = this.getClass().getResourceAsStream("avropath")) {
             AutoDetectTool adt = new AutoDetectTool();
             DetectResult result = adt.detect(is);
             Assert.assertEquals(FileFormat.AVRO,result.getFormatType());
         }
     }
     
     //TODO the api don't support detect parquet, will support it in daikon-ee
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
