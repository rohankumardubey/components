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
    
}
