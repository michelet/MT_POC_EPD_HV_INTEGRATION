package ch.bfh.masterarbeit.michelet_cedric.bean;
import ch.bfh.masterarbeit.michelet_cedric.model.CDADocument;
import java.io.IOException;
import java.util.ArrayList;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

/**
 * Bean that store all the documents.
 * The documents are stored in memory, and when the server is restarted all the new added documents are lost.
 * @author michelet
 */
@ManagedBean(name="documentsBean")
@SessionScoped
public class DocumentsBean {
    private ArrayList<CDADocument> documents;
        
    /**
     * Constructor
     */
    public DocumentsBean() {
        documents = new ArrayList<>();
        this._loadSampleData();
    }
    
    /**
     * Load a list of sample documents
     */
    private void _loadSampleData() {
        try {
            documents.add(new CDADocument("1", "Discharge sumary", CDADocument.getSampleContent("sample1.xml")));
            documents.add(new CDADocument("2", "Radiology report", CDADocument.getSampleContent("sample2.xml")));
        } catch(IOException e) {
            //@TODO handle error
        }
    }
    
    /**
     * Get all the documents
     * @return List of documents (CDADocument)
     */
    public ArrayList<CDADocument> getDocuments() {
        return documents;
    }
   
    /**
     * Get a specific document by its ID
     * @param id ID of the document to retrieve
     * @return CDADocument (or null if not found)
     */
    public CDADocument getDocumentById(String id) {
        for(CDADocument doc : documents) {
            if(doc.getId().equals(id)) return doc;
        }
        return null;
    }
    
    /**
     * Add a document to the list.
     * @param document 
     */
    public void addDocument(CDADocument document) {
        documents.add(document);
    }
}