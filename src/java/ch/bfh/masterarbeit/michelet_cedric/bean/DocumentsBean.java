/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.bfh.masterarbeit.michelet_cedric.bean;
import ch.bfh.masterarbeit.michelet_cedric.model.CDADocument;
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
    }
    
}