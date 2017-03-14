/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.bfh.masterarbeit.michelet_cedric.model;

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