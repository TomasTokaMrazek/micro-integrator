/*
 *Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *WSO2 Inc. licenses this file to you under the Apache License,
 *Version 2.0 (the "License"); you may not use this file except
 *in compliance with the License.
 *You may obtain a copy of the License at
 *
 *http://www.apache.org/licenses/LICENSE-2.0
 *
 *Unless required by applicable law or agreed to in writing,
 *software distributed under the License is distributed on an
 *"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *KIND, either express or implied.  See the License for the
 *specific language governing permissions and limitations
 *under the License.
 */
package org.wso2.carbon.esb.handler;

import java.io.IOException;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.esb.integration.common.utils.CarbonLogReader;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

/**
 * Test class to test a synapse handler.
 */
public class HandlerTest extends ESBIntegrationTest {

    private CarbonLogReader carbonLogReader;

    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {
        super.init();
        carbonLogReader = new CarbonLogReader();
    }

    @Test(groups = {"wso2.esb"}, description = "Sending a Message Via proxy to check synapse handler logs")
    public void testSynapseHandlerExecution() throws IOException {
        boolean handlerStatus = false;
        carbonLogReader.start();

        OMElement response = axis2Client
                .sendSimpleStockQuoteRequest(getProxyServiceURLHttp("handlerTestProxy"), null,
                                             "WSO2");
        Assert.assertNotNull(response);

        String message = carbonLogReader.getLogs();
        if (message.contains("handleRequestInFlow") && message.contains("handleRequestOutFlow") && message.contains(
                "handleResponseInFlow") && message.contains("handleResponseOutFlow")) {
            handlerStatus = true;
        }
        carbonLogReader.stop();

        Assert.assertTrue(handlerStatus, "Synapse handler not working properly");
    }

    @Test(groups = {"wso2.esb"}, description = "Sending a message via proxy to check whether Synapse Handlers get "
            + "invoked when a SoapFault come as a response")
    public void testSynapseHandlerExecutionWhenSoapFaultRecieved()
            throws InterruptedException {
        boolean responseInStatus = false;
        boolean errorOnSoapFaultStatus = false;
        carbonLogReader.clearLogs();
        carbonLogReader.start();
        try {
            axis2Client
                    .sendSimpleStockQuoteRequest(getProxyServiceURLHttp("handlerTestProxyWithSoapfault"),
                                                 null, "WSO2");
            fail("This query must throw an exception since SoapFault come as response");
        } catch (AxisFault expected) {
            assertEquals(expected.getReason(), "Custom ERROR Message", "Custom ERROR Message mismatched");
        }

        errorOnSoapFaultStatus = carbonLogReader.checkForLog("Fault Sequence Hit", 10);
        responseInStatus = carbonLogReader.checkForLog("handleResponseInFlow", 10);
        carbonLogReader.stop();

        Assert.assertTrue(errorOnSoapFaultStatus, "When SoapFault come as a response the fault sequence hasn't been "
                + "invoked because of FORCE_ERROR_ON_SOAP_FAULT property is not working properly");
        Assert.assertTrue(responseInStatus, "Synapse handler hasn't been invoked when a Soap Fault received");
    }
}
