/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.bfh.masterarbeit.michelet_cedric.bean;
import ch.bfh.masterarbeit.michelet_cedric.model.CDADocument;
import java.io.IOException;
import java.util.ArrayList;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

/**
 *
 * @author michelet
 */
@ManagedBean
@SessionScoped
public class DocumentsBean {
    private final ArrayList<CDADocument> documents;
    
    public DocumentsBean() {
        documents = new ArrayList<>();
        this._loadSampleData();
    }
    
    /**
     * Load a list of sample documents
     */
    private void _loadSampleData() {
        try {
            documents.add(new CDADocument("1", "test 1.1", CDADocument.getSampleContent("sample1.xml")));
            documents.add(new CDADocument("2", "test 2", "body"));
            documents.add(new CDADocument("3", "test 3", "body"));
        } catch(IOException e) {
            //@TODO handle error
        }
    }
}