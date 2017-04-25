package ch.bfh.masterarbeit.michelet_cedric.model;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import org.w3c.dom.Node;

/**
 * Class to store a unique result from HealthVault of type heart rate.
 * Based on the Microsoft Sample.
 * @author Cédric Michelet
 */
public class ThingBloodGlucose extends Thing {
    private String bloodGlucose;
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
            <thing-id version-stamp="6a2200b6-6e0e-4efd-a0e3-b7cc0430d149">fbbc4199-50b2-485d-8456-4676e3708189</thing-id>
            <type-id name="Blood glucose">879e7c04-4e8a-4707-9ad3-b054df467ce4</type-id>
            <thing-state>Active</thing-state>
            <flags>0</flags>
            <eff-date>2016-05-07T00:00:00</eff-date>
            <data-xml>
                <blood-glucose>
                    <when>
                        <date>
                                <y>2016</y><m>5</m><d>7</d>
                        </date>
                    </when>
                    <value>
                        <mmolPerL>5.2</mmolPerL>
                        <display units="mmol/L" units-code="mmol-per-l">5.2</display>
                    </value>
                    <glucose-measurement-type>
                        <text>Whole blood</text>
                        <code>
                            <value>wb</value>
                            <family>wc</family>
                            <type>glucose-measurement-type</type>
                        </code>
                    </glucose-measurement-type>
                    <measurement-context>
                        <text>Fasting</text>
                        <code>
                            <value>fasting</value>
                            <family>wc</family>
                            <type>glucose-measurement-context</type>
                        </code>
                    </measurement-context>
                </blood-glucose>
                <common />
            </data-xml>
        </thing>*/

        bloodGlucose = xpath.evaluate("data-xml/blood-glucose/value/display", node);
        unitCode = xpath.evaluate("data-xml/blood-glucose/value/display/@units-code", node);
    }

    public String getBloodGlucose() {
        return bloodGlucose;
    }

    public void setBloodGlucose(String bloodGlucose) {
        this.bloodGlucose = bloodGlucose;
    }

    public String getUnitCode() {
        return unitCode;
    }

    public void setUnitCode(String unitCode) {
        this.unitCode = unitCode;
    }

    @Override
    public String getHTMLTableHeader(int countItems) {
        return "<tr><th colspan=\"3\">Blood glucose ("+ countItems + ")</th></tr><tr><th>Date</th><th>Value</th><th>Unit</th></tr>";
    }
    
    @Override
    public String getAsHTMLTableRow() {
        return "<tr><td>" + this.getEffectiveDateFormated() + "</td><td>" + this.bloodGlucose + "</td><td>" + this.unitCode + "</td></tr>";
    }
}