package org.talend.components.simplefileio.runtime.auto;

import java.io.InputStream;
import java.nio.charset.Charset;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.net.MediaType;

/**
 * adapter the auto detect tool in daikon-ee
 *
 */
public class AutoDetectTool {
  
    private static final Log LOG = LogFactory.getLog(AutoDetectTool.class);
  
    public static class DetectResult {
        private FileFormat format = FileFormat.UNKOWN;
        
        private MediaType mt;
        
        public DetectResult(MediaType mt) {
            this.mt = mt;
        }
        
        public FileFormat getFormatType() {
            return format;
        }
        
        public void setFormatType(FileFormat format) {
            this.format = format;
        }
      
        //CSV
        public boolean getCSV_header_present() {
            return "present".equals(mt.parameters().get(AutoDetectConstants.HEADER_PARAMETER));
        }
        
        public String getCSV_record_separator() {
            String rs = mt.parameters().get(AutoDetectConstants.RECORD_SEPARATOR_PARAMETER).get(0);
            if(rs == null || rs.isEmpty()) {
                return "\n";
            }
            return rs.substring(1, rs.length()-1);
        }
        
        public char getCSV_delimiter() {
            String delimiter = mt.parameters().get(AutoDetectConstants.DELIMITER_PARAMETER).get(0);
            if(delimiter == null || delimiter.isEmpty()) {
                return ';';
            }
            return delimiter.charAt(1);
        }
        
        public char getCSV_text_enclosure_char() {
            String tec = mt.parameters().get(AutoDetectConstants.TEXT_ENCLOSURE_CHAR).get(0);
            if(tec == null || tec.isEmpty()) {
                return '\"';
            }
            return tec.charAt(1);
        }
        
        public char getCSV_escape_char() {
            String ec = mt.parameters().get(AutoDetectConstants.ESCAPE_CHAR).get(0);
            if(ec == null || ec.isEmpty()) {
                return '\\';
            }
            return ec.charAt(1);
        }
        
        public Charset getCSV_charset() {
            return mt.charset().get();
        }
        
        //Excel 2007 and Excel 97
        public String getExcel_sheet() {
            return mt.parameters().get(AutoDetectConstants.PAGE_NAME_PARAMETER).get(0);
        }
        
        public int getExcel_header_size() {
            return Integer.valueOf(mt.parameters().get(AutoDetectConstants.HEADER_SIZE_PARAMETER).get(0));
        }
        
        public int getExcel_number_of_columns() {
            return Integer.valueOf(mt.parameters().get(AutoDetectConstants.NUMBER_OF_COLUMNS_PARAMETER).get(0));
        }
        
        //Excel Html, parameters are self define in file, only encoding is TODO
        
        //AVRO self define in file
        //PARQUET self define in file
    }
    
    private final SchemaDetection sd;
    
    public AutoDetectTool() {
        sd = new SchemaDetection();
        sd.registerDetectors();
        sd.registerParsers();
    }
    
    public DetectResult detect(InputStream in) {
        try {
            MediaType mt = sd.detect(in);
            DetectResult result = new DetectResult(mt);
            
            //CSV
            if(mt.is(AutoDetectConstants.CSV)) {
                result.setFormatType(FileFormat.CSV);
            }
            
            //Excel 2007
            if(mt.is(AutoDetectConstants.EXCEL2007)) {
                result.setFormatType(FileFormat.EXCEL2007);
            }
            
            //Excel 97
            if(mt.is(MediaType.MICROSOFT_EXCEL)) {
                result.setFormatType(FileFormat.EXCEL97);
            }
            
            //TODO Excel HTML
            //result.setFormatType(FileFormat.EXCELHTML);
            
            //TODO AVRO
            //result.setFormatType(FileFormat.AVRO);
            
            //TODO PARQUET
            //result.setFormatType(FileFormat.PARQUET);
            
            return result;
        } catch (Exception e) {
             throw new RuntimeException("can't guess the file format, now support csv, excel 97 and 2007 format : " + e.getClass() + " : " + e.getMessage() + " : " + e.getCause());
        }
    }
    
}
