package org.talend.components.simplefileio.runtime.auto;

import com.google.common.net.MediaType;

//copy from daikon-ee
public interface AutoDetectConstants {

    //CSV
    /** The csv format family. */
    MediaType CSV = MediaType.create("text", "csv");
  
    /** Header parameter that might be "present" or "absent". */
    String HEADER_PARAMETER = "header";
  
    /** Record separator parameter: '\n', '\r' and '\r\n' */
    String RECORD_SEPARATOR_PARAMETER = "record_separator";
  
    /** Name of the separator parameter. */
    String DELIMITER_PARAMETER = "delimiter";
  
    /** Name of the text enclosure parameter. */
    String TEXT_ENCLOSURE_CHAR = "text_enclosure_char";
  
    /** Name of the escape character parameter. */
    String ESCAPE_CHAR = "escape_char";
  
    //EXCEL
    String PAGE_NAME_PARAMETER = "page";
  
    String HEADER_SIZE_PARAMETER = "header";
  
    String NUMBER_OF_COLUMNS_PARAMETER = "columns";
  
}
