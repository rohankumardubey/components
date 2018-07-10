package org.talend.components.simplefileio.runtime;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;
import org.talend.components.simplefileio.runtime.auto.AutoDetectTool;
import org.talend.components.simplefileio.runtime.auto.AutoDetectTool.DetectResult;

public class AutoDetectToolTest {

     @Test
     public void testCSVDetectBasic() throws IOException {
         try(InputStream is = this.getClass().getResourceAsStream("invalidColumnNumber.txt")) {
             AutoDetectTool adt = new AutoDetectTool();
             DetectResult result = adt.detect(is);
             System.out.println(result.getFormatType());
         }
     }
     
     @Test
     public void testExcel97DetectBasic() throws IOException {
         try(InputStream is = this.getClass().getResourceAsStream("basic.xls")) {
             AutoDetectTool adt = new AutoDetectTool();
             DetectResult result = adt.detect(is);
             System.out.println(result.getFormatType());
         }
     }
     
     @Test
     public void testExcel2007DetectBasic() throws IOException {
         try(InputStream is = this.getClass().getResourceAsStream("basic.xlsx")) {
             AutoDetectTool adt = new AutoDetectTool();
             DetectResult result = adt.detect(is);
             System.out.println(result.getFormatType());
         }
     }
     
     @Test
     public void testExcelHtmlDetectBasic() throws IOException {
         try(InputStream is = this.getClass().getResourceAsStream("sales-force.html")) {
             AutoDetectTool adt = new AutoDetectTool();
             DetectResult result = adt.detect(is);
             System.out.println(result.getFormatType());
         }
     }
    
}
