// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.marketo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class MarketoUtils {

    private static final List<SimpleDateFormat> allowedDateFormats = Arrays.asList(
            new SimpleDateFormat(MarketoConstants.DATETIME_PATTERN_PARAM),
            new SimpleDateFormat(MarketoConstants.DATETIME_PATTERN_PARAM_ALT),
            new SimpleDateFormat(MarketoConstants.DATETIME_PATTERN_PARAM_UTC),
            new SimpleDateFormat(MarketoConstants.DATETIME_PATTERN_REST),
            new SimpleDateFormat(MarketoConstants.DATETIME_PATTERN_SOAP));

    /**
     * Parse a string amongst date patterns allowed to give back the matching Date object
     * 
     * @param datetime string to parse
     * @return java.util.Date parsed
     * @throws ParseException
     */
    public static Date parseDateString(String datetime) throws ParseException {
        Date result = null;
        for (SimpleDateFormat sdf : allowedDateFormats) {
            try {
                result = sdf.parse(datetime);
                break;
            } catch (ParseException e) {
                // nothing to do
            }
        }
        if (result == null) {
            throw new ParseException(datetime + " don't use a pattern allowed.", 0);
        }

        return result;
    }
}
