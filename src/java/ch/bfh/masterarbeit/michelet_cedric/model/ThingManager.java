package ch.bfh.masterarbeit.michelet_cedric.model;
import java.text.ParseException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Node;

/**
 * Handle a result from HealthVault and convert it to a local object (class).
 * Based on the Microsoft Sample.
 * @author Cédric Michelet
 */
public class ThingManager {
    
    /**
     * Take a result as XML node and convert it to a local object.
     * @param node XML node containing the value
     * @return
     * @throws XPathExpressionException
     * @throws ParseException 
     */
    public static Thing unmarshal(Node node) throws XPathExpressionException, ParseException {
        //search class related to thing type
        XPath xpath = XPathFactory.newInstance().newXPath();
        String typeId = xpath.evaluate("type-id", node);
        
        Thing t = null;
        
        switch(typeId) {
            case HealthVaultConstants.THING_TYPE_WEIGHT:
                t = new ThingWeight();
                t.loadValuesFromNode(node);
                break;
            case HealthVaultConstants.THING_TYPE_BLOOD_PRESSURE:
                t = new ThingBloodPressure();
                t.loadValuesFromNode(node);
                break;
            case HealthVaultConstants.THING_TYPE_BLOOD_GLUCOSE:
                t = new ThingBloodGlucose();
                t.loadValuesFromNode(node);
        }
        
    	return t;
    }
}