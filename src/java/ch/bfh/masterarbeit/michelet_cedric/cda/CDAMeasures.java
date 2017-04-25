package ch.bfh.masterarbeit.michelet_cedric.cda;
import ch.bfh.masterarbeit.michelet_cedric.model.HealthVaultConstants;
import ch.bfh.masterarbeit.michelet_cedric.model.Thing;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.XMLWorkerHelper;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Base64;
import org.ehealth_connector.cda.ch.AbstractCdaCh;
import org.ehealth_connector.common.Author;
import org.ehealth_connector.common.Name;
import org.ehealth_connector.common.Patient;
import org.ehealth_connector.common.enums.AdministrativeGender;
import org.ehealth_connector.common.enums.LanguageCode;
import org.openhealthtools.mdht.uml.cda.ch.CHFactory;
import java.util.Date;
import java.util.List;
import org.openhealthtools.mdht.uml.cda.CDAFactory;
import org.openhealthtools.mdht.uml.cda.Component2;
import org.openhealthtools.mdht.uml.cda.NonXMLBody;
import org.openhealthtools.mdht.uml.hl7.datatypes.BinaryDataEncoding;
import org.openhealthtools.mdht.uml.hl7.datatypes.DatatypesFactory;
import org.openhealthtools.mdht.uml.hl7.datatypes.ED;

/**
 * Handle a default CDA-CH document body level 1 (body as PDF).
 * The PDF body is generated from a list of measures.
 * @author Cédric Michelet
 */
public class CDAMeasures extends AbstractCdaCh {

    public CDAMeasures(List<Thing> measures, String from, String to) {
        super(CHFactory.eINSTANCE.createCDACH().init());
        super.initCda();
        try {
            _createCDAFromMeasures(measures, from, to);
        } catch(Exception e) {
            e.printStackTrace();
            //@todo handle exception
        }
    }    
       
    /**
     * Build the content of the document (metadata + body) based on the measures.
     * @param measures
     * @param from
     * @param to
     * @throws DocumentException
     * @throws IOException 
     */
    private void _createCDAFromMeasures(List<Thing> measures, String from, String to) throws DocumentException, IOException {
        //doc: https://www.medshare.net/fileadmin/eHealthConnectorAPI/doc/index.html?org/ehealth_connector/communication/xd/xdm/class-use/XdmContents.html
        
        this.setLanguageCode(LanguageCode.ENGLISH);
        
        /*
        	org.ehealth_connector.communication.ch.DocumentMetadataCh metaData) {
		metaData.addAuthor(new Author(new Name("Gerald", "Smitty"), "1234"));
		metaData.setDestinationPatientId(new Identificator("2.16.756.5.37", "123.73.423.124"));
		metaData.setTypeCode(TypeCode.ANASTHESIE_BERICHT);
		metaData.setFormatCode(FormatCode.EIMPFDOSSIER);
		metaData.setClassCode(ClassCode.ALERTS);
		metaData.setHealthcareFacilityTypeCode(
				HealthcareFacilityTypeCode.AMBULANTE_EINRICHTUNG_INKL_AMBULATORIUM);
		metaData.setPracticeSettingCode(PracticeSettingCode.ALLERGOLOGIE);
		metaData.addConfidentialityCode(ConfidentialityCode.ADMINISTRATIVE_DATEN);
        */
        
        //**************** author
        //@todo use DemoUtil
        
        Patient patient = new Patient(new Name("Cédric","Michelet"),AdministrativeGender.MALE, new Date(1976,04,16));   
        Author author = new Author(patient);
        
        this.addAuthor(author);
        this.setPatient(patient);
        
        //verabreichteImpfung.setConfidentialityCode(Confidentiality.RESTRICED);
        
        //**********************  create document as html
        String htmlDocument = _buildDocumentAsHTML(measures, from, to);
        
        //**********************  convert html to pdf
        //http://hmkcode.com/itext-html-to-pdf-using-java/ @todo convert to pdf
        Document document = new Document();
        //PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream("pdf.pdf"));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = PdfWriter.getInstance(document, baos);
        document.open();
        //XMLWorkerHelper.getInstance().parseXHtml(writer, document, new FileInputStream("index.html"));
        InputStream stream = new ByteArrayInputStream(htmlDocument.getBytes(StandardCharsets.UTF_8));
        XMLWorkerHelper.getInstance().parseXHtml(writer, document, stream);
        document.close();
        //convert pdf to base64 string
        String pdfBase64 = Base64.getEncoder().encodeToString(baos.toByteArray());
        
        
        //**********************  add pdf to body
        NonXMLBody body = CDAFactory.eINSTANCE.createNonXMLBody();
        ED text = DatatypesFactory.eINSTANCE.createED();
        
        text.addText(pdfBase64);
        text.setMediaType("application/pdf");
        text.setRepresentation(BinaryDataEncoding.B64);
        body.setText(text);
        Component2 c2 = CDAFactory.eINSTANCE.createComponent2();
        c2.setNonXMLBody(body);
        this.getDocRoot().getClinicalDocument().setComponent(c2);
                
        //save to file
        //this.saveToFile("c:\\DOCUMENTS\\demo-cda.xml");  
    }
       
    /**
     * Create a html representation of the document
     * @param measures
     * @param from
     * @param to
     * @return 
     */
    private String _buildDocumentAsHTML(List<Thing> measures, String from, String to) {
        StringBuilder sb = new StringBuilder(1024);
        
        sb.append("<!DOCTYPE html><html><head>");
        sb.append("<style>table {border-collapse: collapse; margin-bottom: 25px;} td {border: 1px solid;padding:10px 5px;} th {border: 1px solid;padding: 10px 5px; font-weight:bold;background-color:#F1F1F1;} h1 {text-decoration: underline;}</style>");
        sb.append("</head><body><h1>Export from HealthVault</h1>");
        
        //add current timestamp
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy kk:mm:ss");
        sb.append("Generation date: ");
        sb.append(sdf.format(date));
        
        sb.append("<br /><br />Values from: " + from + " to: " + to + "<br />");
        
        sb.append(_getMeasuresTypeAsHTMLArray(measures, HealthVaultConstants.THING_TYPE_BLOOD_PRESSURE));
        sb.append(_getMeasuresTypeAsHTMLArray(measures, HealthVaultConstants.THING_TYPE_BLOOD_GLUCOSE));
        sb.append(_getMeasuresTypeAsHTMLArray(measures, HealthVaultConstants.THING_TYPE_WEIGHT));
        sb.append("</body></html>");
        
        return sb.toString();
    }
    
    private String _getMeasuresTypeAsHTMLArray(List<Thing> measures, String measuresType) {
        StringBuilder sb = new StringBuilder(1024);
        int count = 0;
        Thing lastElement = null;
        
        for(Thing t : measures) {
            if(t.getType().equals(measuresType)) {
                lastElement = t;
                sb.append(t.getAsHTMLTableRow());
                count = count + 1;
            }
        }
        
        if(count == 0) return ""; //if no measure, return empty string
        
        sb.insert(0, lastElement.getHTMLTableHeader(count));
        sb.insert(0, "<table>");
        sb.append("</table>");
        return sb.toString();
    }
}