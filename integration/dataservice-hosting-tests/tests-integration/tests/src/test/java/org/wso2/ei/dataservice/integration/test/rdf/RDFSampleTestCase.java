/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.ei.dataservice.integration.test.rdf;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.dataservices.samples.rdf_dataservice.DataServiceFaultException;
import org.wso2.carbon.dataservices.samples.rdf_dataservice.SamplesRDFSampleServiceStub;
import org.wso2.ei.dataservice.integration.test.DSSIntegrationTest;

import java.rmi.RemoteException;

import static org.testng.Assert.assertTrue;

public class RDFSampleTestCase extends DSSIntegrationTest {

    private static final Log log = LogFactory.getLog(RDFSampleTestCase.class);

    private SamplesRDFSampleServiceStub stub;

    @BeforeClass(alwaysRun = true)
    public void serviceDeployment() throws Exception {
        super.init();
        String serviceName = "RDFSampleService";
        String serviceEndPoint = getServiceUrlHttp(serviceName);
        stub = new SamplesRDFSampleServiceStub(serviceEndPoint);
        log.info(serviceName + " uploaded");
    }

    @Test(groups = { "wso2.dss" })
    public void getAllMovieData() throws RemoteException, DataServiceFaultException {

        log.info("Running RDFSampleServiceTestCase#getAllMovieData");
        assertTrue(stub.getAllMovieData().length > 0, "No of movies should be greater than zero");
        log.info("RDF select Operation Success");
    }

    @Test(groups = { "wso2.dss" })
    public void getMovieDataByGenre() throws RemoteException, DataServiceFaultException {

        log.info("Running RDFSampleServiceTestCase#getMoviesByGenre");
        assertTrue(stub.getMoviesByGenre("Comedy").length > 0, "No of movies should be greater than zero");
        log.info("RDF select Operation with param Success");
    }

}
