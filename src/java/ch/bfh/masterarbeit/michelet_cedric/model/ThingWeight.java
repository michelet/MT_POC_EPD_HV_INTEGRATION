package ch.bfh.masterarbeit.michelet_cedric.model;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import org.w3c.dom.Node;

/**
 * Class to store a unique result from HealthVault of type weight.
 * Based on the Microsoft Sample.
 * @author Cédric Michelet
 */
public class ThingWeight extends Thing {
    private String weight;
    private String unitCode;

    /**
     * Load the specific attributes related to type Weight.
     * @param xpath
     * @param node
     * @throws XPathExpressionException 
     */
    @Override
    protected void loadCustomValuesFromNode(XPath xpath, Node node) throws XPathExpressionException {
        /*<thing>
            <thing-id version-stamp="bee5a2b1-1489-468b-bac3-328c571b03de">95ae7372-58fa-4d14-80af-8745373a3ab7</thing-id>
            <type-id name="Weight">3d34d87e-7fc1-4153-800f-f56592cb0d17</type-id>
            <thing-state>Active</thing-state>
            <flags>0</flags>
            <eff-date>2016-06-01T00:00:00</eff-date>
            <data-xml>
                    <weight>
                            <when>
                                    <date>
                                            <y>2016</y><m>6</m><d>1</d>
                                    </date>
                            </when>
                            <value>
                                    <kg>77.5</kg>
                                    <display units="kg" units-code="kg">77.5</display>
                            </value>
                    </weight>
                    <common />
            </data-xml>
        </thing>*/

        weight = xpath.evaluate("data-xml/weight/value/display", node);
        unitCode = xpath.evaluate("data-xml/weight/value/display/@units-code", node);
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public String getUnitCode() {
        return unitCode;
    }

    public void setUnitCode(String unitCode) {
        this.unitCode = unitCode;
    }
    
    @Override
    public String getHTMLTableHeader(int countItems) {
        return "<tr><th colspan=\"3\">Weight (" + countItems +")</th></tr><tr><th>Date</th><th>Weight</th><th>Unit</th></tr>";
    }
    
    @Override
    public String getAsHTMLTableRow() {
        return "<tr><td>" + this.getEffectiveDateFormated() + "</td><td>" + this.weight + "</td><td>" + this.unitCode + "</td></tr>";
    }
}