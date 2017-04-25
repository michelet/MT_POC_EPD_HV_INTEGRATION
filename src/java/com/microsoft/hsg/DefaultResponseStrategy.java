/*
 * Copyright 2011 Microsoft Corp.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.microsoft.hsg;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.Reader;
import java.util.Base64;


import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.xml.sax.InputSource;
import sun.nio.cs.StandardCharsets;

/**
 * This class is reponsible for checking for errors in the 
 * response from HealthVault before forwarding on along.
 */
public class DefaultResponseStrategy
implements ResponseStrategy
{
	static Logger logger = Logger.getLogger(ResponseStrategy.class);


	/**
	 *  
	 * @see com.microsoft.hsg.ResponseStrategy#doWithResponse(com.microsoft.hsg.Response)
	 */
	public void doWithResponse(Response response)
	{
		try 
		{
                    logger.info("do with response");
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			InputStream istream = response.getInputStream();
			try
			{
				int len;
				byte[] buf = new byte[4096];
				while((len = istream.read(buf)) != -1 )
				{
					baos.write(buf, 0, len);
				}
			} 
			finally
			{
				istream.close();
			}
			
			byte[] message = baos.toByteArray();
			ByteArrayInputStream bais = new ByteArrayInputStream(message);
			
			if (logger.isDebugEnabled()) {
				logger.debug(new String(message));
			}
			
                        //xxx - RESPONSE FROM SERVER
                        //if(true)throw new Exception(new String(message));
                        Logger.getLogger(DefaultResponseStrategy.class).info(new String(message));
                        
			XPath xpath = XPathFactory.newInstance().newXPath();
	        String exp = "/response/status/code";
	        String codeString = xpath.evaluate(exp, new InputSource(bais));
	        
	        int code = 0;
	        if (codeString != null && codeString.length() != 0) {
	        	code = Integer.parseInt(codeString);
	        }
	        
                //xxx - RESPONSE FROM SERVER
                //if(code != 0)
                //    if(true)throw new Exception(new String(message));
                
	        bais.reset();
                                
                //if(true)return;
	        response.setInputStream(bais);
	        response.setResponseCode(code);
		}
		catch(Exception e)
		{
			throw new HVException(e);
		}
        
        checkResponseCode(response);
	}       
	
	private void checkResponseCode(Response response)
	{
		if (response.getResponseCode()==0) return;
		
		DefaultResponseCodeTranslator translator = 
			new DefaultResponseCodeTranslator();
		HVException e = translator.translate(response);
		if (e != null)
		{
			throw e;
		}
	}
}
