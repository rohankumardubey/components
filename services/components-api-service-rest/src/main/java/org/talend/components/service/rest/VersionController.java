// ==============================================================================
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
// ==============================================================================

package org.talend.components.service.rest;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.talend.components.service.rest.dto.VersionDto;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

/**
 * Definition controller..
 */
@RestController(value = "VersionController")
@RequestMapping(ServiceConstants.V0 + "/version")
public interface VersionController {

    /**
     * Get the version information for this service.
     *
     * @return a Version object containing the current git information at time of build.
     */
    @RequestMapping(method = GET)
    @ResponseBody
    VersionDto getVersion();
}
