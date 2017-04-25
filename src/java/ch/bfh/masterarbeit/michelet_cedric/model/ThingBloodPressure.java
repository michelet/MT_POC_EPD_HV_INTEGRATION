package ch.bfh.masterarbeit.michelet_cedric.model;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import org.w3c.dom.Node;

/**
 * Class to store a unique result from HealthVault of type blood pressure.
 * Based on the Microsoft Sample.
 * @author Cédric Michelet
 */
public class ThingBloodPressure extends Thing {
    private String systolic;
    private String diastolic;
    private String pulse;
    
    /**
     * Load the specific attributes related to type blood pressure.
     * @param xpath
     * @param node
     * @throws XPathExpressionException 
     */
    @Override
    protected void loadCustomValuesFromNode(XPath xpath, Node node) throws XPathExpressionException {
        /*<thing>
            <thing-id version-stamp="83b69a4f-50f4-45eb-8d99-82f670a724f3">f5ace0b3-e824-4a60-879c-0590334cf5f1</thing-id>
            <type-id name="Blood pressure">ca3c57f4-f4c1-4e15-be67-0a3caf5414ed</type-id>
            <thing-state>Active</thing-state>
            <flags>0</flags>
            <eff-date>2016-03-01T06:40:00</eff-date>
            <data-xml>
                    <blood-pressure>
                            <when>
                                    <date>
                                            <y>2016</y><m>3</m><d>1</d>
                                    </date>
                                    <time>
                                            <h>6</h><m>40</m>
                                    </time>
                            </when>
                            <systolic>115</systolic>
                            <diastolic>75</diastolic>
                            <pulse>75</pulse>
                            <irregular-heartbeat>false</irregular-heartbeat>
                    </blood-pressure>
                    <common />
            </data-xml>
        </thing>*/

        systolic = xpath.evaluate("data-xml/blood-pressure/systolic", node);
        diastolic = xpath.evaluate("data-xml/blood-pressure/diastolic", node);
        pulse = xpath.evaluate("data-xml/blood-pressure/pulse", node);
    }

    public String getSystolic() {
        return systolic;
    }

    public void setSystolic(String systolic) {
        this.systolic = systolic;
    }

    public String getDiastolic() {
        return diastolic;
    }

    public void setDiastolic(String diastolic) {
        this.diastolic = diastolic;
    }

    public String getPulse() {
        return pulse;
    }

    public void setPulse(String pulse) {
        this.pulse = pulse;
    }

    @Override
    public String getHTMLTableHeader(int countItems) {
        return "<tr><th colspan=\"4\">Blood pressure ("+ countItems +")</th></tr><tr><th>Date</th><th>Systolic</th><th>Diastolic</th><th>Pulse</th></tr>";
    }
    
    @Override
    public String getAsHTMLTableRow() {
        return "<tr><td>" + this.getEffectiveDateFormated() + "</td><td>" + this.systolic + "</td><td>" + this.diastolic + "</td><td>" + this.pulse + "</td></tr>";
    }
}