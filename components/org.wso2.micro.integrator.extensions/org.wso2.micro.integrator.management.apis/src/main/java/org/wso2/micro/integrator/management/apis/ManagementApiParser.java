/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied. See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 */
package org.wso2.micro.integrator.management.apis;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.micro.core.util.CarbonException;
import org.wso2.micro.integrator.core.util.MicroIntegratorBaseUtils;
import org.wso2.securevault.SecretResolverFactory;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Objects;

/**
 * This class reads through internal-apis.xml and parses the values.
 */
public class ManagementApiParser {

    private static final Log LOG = LogFactory.getLog(ManagementApiParser.class);
    private static OMElement managementApiElement;
    private HashMap<String, char[]> usersList;

    /**
     * Method to get the File object representation of the internal-apis.xml file.
     *
     * @return File object representation
     */
    public static File getConfigurationFile() {
        return new File(MicroIntegratorBaseUtils.getCarbonConfigDirPath(), "internal-apis.xml");
    }

    /**
     * Method to get absolute path of the internal-apis.xml file.
     *
     * @return The absolute path for the file
     */
    public static String getConfigurationFilePath() {
        return new File(MicroIntegratorBaseUtils.getCarbonConfigDirPath(), "internal-apis.xml").getAbsolutePath();
    }

    public static OMElement getManagementApiElement() throws IOException, CarbonException, XMLStreamException,
            ManagementApiUndefinedException {
        if (Objects.nonNull(managementApiElement)) {
            return managementApiElement;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Parsing the ManagementApi element from " + getConfigurationFilePath());
        }
        Iterator<OMElement> internalApis = getInternalApisElement();
        while (internalApis.hasNext()) {
            OMElement apiOM = internalApis.next();
            String apiName = apiOM.getAttributeValue(new QName("name"));
            if ("ManagementApi".equals(apiName)) {
                managementApiElement = apiOM;
                return apiOM;
            }
        }
        throw new ManagementApiUndefinedException("Management API not defined in " + getConfigurationFilePath());
    }

    public HashMap<String, char[]> getUserList() throws CarbonException, XMLStreamException, IOException,
            UserStoreUndefinedException, ManagementApiUndefinedException {
        if (Objects.nonNull(usersList)) {
            return usersList;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Parsing the UsetStore element from " + getConfigurationFilePath());
        }
        OMElement userStoreOM = getManagementApiElement().getFirstChildWithName(new QName("UserStore"));
        if (Objects.nonNull(userStoreOM)) {
            return populateUserList(userStoreOM.getFirstChildWithName(new QName("users")));
        } else {
            throw new UserStoreUndefinedException("UserStore tag not defined inside the Management API");
        }
    }

    private static Iterator<OMElement> getInternalApisElement() throws IOException, CarbonException, XMLStreamException {
        File mgtApiUserConfig = getConfigurationFile();
        try (InputStream fileInputStream = new FileInputStream(mgtApiUserConfig)) {
            OMElement documentElement = getOMElementFromFile(fileInputStream);
            createSecretResolver(documentElement);

            return documentElement.getFirstChildWithName(new QName("apis")).getChildrenWithName(new QName("api"));
        }
    }

    /**
     * Returns the document OMElement from the internal-apis.xml file.
     *
     * @param fileInputStream input stream of internal-apis.xml
     * @return OMelement of internal-apis.xml
     */
    private static OMElement getOMElementFromFile(InputStream fileInputStream) throws CarbonException, XMLStreamException {
        InputStream inputStream = MicroIntegratorBaseUtils.replaceSystemVariablesInXml(fileInputStream);
        StAXOMBuilder builder = new StAXOMBuilder(inputStream);
        return builder.getDocumentElement();
    }

    /**
     * Populates the userList hashMap by user store OM element
     * @return HashMap<String, char []> Map of credential pairs
     */
    private HashMap<String, char []> populateUserList(OMElement users) {
        HashMap<String, char []> userList = new HashMap<>();
        if (users != null) {
            Iterator usersIterator = users.getChildrenWithName(new QName("user"));
            if (usersIterator != null) {
                while (usersIterator.hasNext()) {
                    OMElement userElement = (OMElement) usersIterator.next();
                    OMElement userNameElement = userElement.getFirstChildWithName(new QName("username"));
                    OMElement passwordElement = userElement.getFirstChildWithName(new QName("password"));
                    if (userNameElement != null && passwordElement != null) {
                        userList.put(userNameElement.getText(), passwordElement.getText().toCharArray());
                    }
                }
            }
        }
        return userList;
    }

    /**
     * Sets the SecretResolver the document OMElement.
     *
     * @param rootElement Document OMElement
     */
    private static void createSecretResolver(OMElement rootElement) {
        SecretResolverFactory.create(rootElement, true);
    }
}

