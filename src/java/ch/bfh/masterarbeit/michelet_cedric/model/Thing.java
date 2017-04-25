package ch.bfh.masterarbeit.michelet_cedric.model;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Node;

/**
 * Abstract class to store a unique result from HealthVault (contains default common attributes to all type of measures).
 * Based on the Microsoft Sample.
 * @author Cédric Michelet
 */
abstract public class Thing {
    String id;
    String type;
    Boolean isActive;
    Date effectiveDate;
    static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Date getEffectiveDate() {
        return effectiveDate;
    }
    
    public String getEffectiveDateFormated() {
        return Thing.simpleDateFormat.format(this.effectiveDate);
    }

    public void setEffectiveDate(Date effectiveDate) {
        this.effectiveDate = effectiveDate;
    }
    
    /**
     * Load the default common attributes to all measures.
     * @param node
     * @throws XPathExpressionException
     * @throws ParseException 
     */
    public void loadValuesFromNode(Node node) throws XPathExpressionException, ParseException {
	XPath xpath = XPathFactory.newInstance().newXPath();
        
        id = xpath.evaluate("thing-id", node);
        type = xpath.evaluate("type-id", node);
        //isActive = xpath.evaluate("thing-state", node).equals("Active") ? true : false; //@TODO ok ?
        isActive = xpath.evaluate("thing-state", node).equals("Active");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        effectiveDate = sdf.parse(xpath.evaluate("eff-date", node));
                
        this.loadCustomValuesFromNode(xpath, node);
    }
    
    /**
     * Abstract. Load the specific value for the data type.
     * @param xpath
     * @param node
     * @throws XPathExpressionException 
     */
    protected abstract void loadCustomValuesFromNode(XPath xpath, Node node) throws XPathExpressionException;
    
    public abstract String getHTMLTableHeader(int countItems);
    public abstract String getAsHTMLTableRow();
}