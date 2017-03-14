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

    public CDADocument(String id, String title, String xmlContent) {
        this.id = id;
        this.title = title;
        this.xmlContent = xmlContent;
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

    public void setTitle(String title) {
        this.title = title;
    }

    public String getXmlContent() {
        return xmlContent;
    }

    public void setXmlContent(String xmlContent) {
        this.xmlContent = xmlContent;
    }
}
