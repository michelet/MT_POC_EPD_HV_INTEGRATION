/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.bfh.masterarbeit.michelet_cedric.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 *
 * @author michelet
 */
public class CDADocument {
    private String id;
    private String title;
    private String xmlContent;
    private String contentType="";
    private String bodyContent="";
    private Boolean isBodyEncodedInBase64=false;

    public CDADocument(String id, String title, String xmlContent) {
        this.id = id;
        this.title = title;
        this.xmlContent = xmlContent;
        this._extractContent();
    }

    private void _extractContent() {
        int start = xmlContent.indexOf("representation=\"B64\"");
        if(start >= 0) isBodyEncodedInBase64 = true;
        
        start = xmlContent.indexOf("mediaType=") + "mediaType=".length();
        if(start < 0) return;
        int stop = xmlContent.indexOf("\"",start+2);
        if(stop < 0) return;
        contentType = xmlContent.substring(start+1, stop).trim();
        
        start = xmlContent.indexOf(">",stop);
        if(start < 0) return;
        stop = xmlContent.indexOf("</text>");
        if(stop < 0) return;
        bodyContent = xmlContent.substring(start+1,stop).trim();
    }
    
    /**
     * Read and return the content of a sample CDA file contains in the project
     * @param sampleName Name of the sample file
     * @return XML Content of the sample
     */
    public static String getSampleContent(String sampleName) throws IOException {
        InputStream is = CDADocument.class.getResourceAsStream("/ch/bfh/masterarbeit/michelet_cedric/sample/" + sampleName);

        if (is == null) {
            return null;
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }

        br.close();
        is.close();

        return sb.toString();
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getXmlContent() {
        return xmlContent;
    }

    public String getContentType() {
        return contentType;
    }

    public String getBodyContent() {
        return bodyContent;
    }

    public Boolean getIsBodyEncodedInBase64() {
        return isBodyEncodedInBase64;
    }
}