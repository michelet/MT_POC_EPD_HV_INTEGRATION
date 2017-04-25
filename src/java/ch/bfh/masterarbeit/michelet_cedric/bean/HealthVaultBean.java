package ch.bfh.masterarbeit.michelet_cedric.bean;
import ch.bfh.masterarbeit.michelet_cedric.cda.CDAMeasures;
import ch.bfh.masterarbeit.michelet_cedric.model.CDADocument;
import ch.bfh.masterarbeit.michelet_cedric.model.HealthVaultConstants;
import ch.bfh.masterarbeit.michelet_cedric.model.Thing;
import ch.bfh.masterarbeit.michelet_cedric.model.ThingManager;
import ch.bfh.masterarbeit.michelet_cedric.servlet.Viewer;
import com.microsoft.hsg.ApplicationAuthenticator;
import com.microsoft.hsg.Authenticator;
import com.microsoft.hsg.Connection;
import com.microsoft.hsg.DefaultPrivateKeyStore;
import com.microsoft.hsg.DefaultSharedSecret;
import com.microsoft.hsg.HVAccessor;
import com.microsoft.hsg.Request;
import com.microsoft.hsg.Transport;
import com.microsoft.hsg.URLConnectionTransport;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.StringWriter;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * Handle the connection to HealthVault (authentication + getting data)
 *
 * @author Cédric Michelet
 */
@ManagedBean(name = "healthVaultBean")
@RequestScoped
public class HealthVaultBean implements Serializable {

    //inject reference to documents' bean
    @ManagedProperty(value = "#{documentsBean}")
    private DocumentsBean documentsBean;

    //param for managing session
    private static final String SESSION_AUTH_NAME = "SESSION_AUTH_NAME";
    private static final String SESSION_RECORD_ID = "SESSION_RECORD_ID";

    //param about JKS containing public/private certificate for accessing HealthVault
    private static final String KEYSTORE_PASSWORD = "$hv*2016*keystore!";
    private static final String KEYSTORE_NAME = "/ch/bfh/masterarbeit/michelet_cedric/certificate/keystore.jks";
    private static final String KEYSTORE_ALIAS = "healthvault";

    //HealthVault constants
    private static final String SHELL_URL = "https://account.healthvault-ppe.co.uk"; //use th EU URL (US = "https://account.healthvault-ppe.com")
    private static final String PLATFORM_URL = "https://platform.healthvault-ppe.co.uk/platform/wildcat.ashx";
    private static final String APP_ID = "a0a65579-c1bb-4113-b0b7-5cd0a6804d5b"; //POC application id

    //used to print variables on the page
    private Boolean DEBUG_MODE  = false;
    
    //Filters for user request
    Boolean has_filters = false;
    String filter_from = "";
    String filter_to = "";
    private SimpleDateFormat filter_date_sdf = new SimpleDateFormat("dd/MM/yyyy");
    Boolean filter_weight = true;
    Boolean filter_bloodPressure = true;
    Boolean filter_bloodGlucose = true;
    
    private String loginURL = "";
    private String authURL = "";
    private String authToken = null;
    private String recordId = null;
    private static HashMap<String, ApplicationAuthenticator> authenticatorMap = new HashMap<>();
    private String lastError = null;
    private String lastErrorStackTrace = null;
    private String lastXMLResult;

    private CDADocument tempCDADocument = null;

    /**
     * Constructor
     */
    public HealthVaultBean() {
        HttpServletRequest request = (HttpServletRequest) (FacesContext.getCurrentInstance().getExternalContext().getRequest());
           
        //build HealthVault URLs
        _buildHVURLs();

        //check if we have an authentication token (in request or in session)
        _extractAuthTokenIfExists(request);

        //check if we have user filters
        _extractFiltersIfExist(request);
        
        //get the current record id we are working on
        if(authToken != null) _recoverRecordId(request);

        //build document if needed
        _buildDocumentIfNeeded(request);
    }

    /**
     * Build the URLs for the authentication and the authorization
     */
    private void _buildHVURLs() {
        //https://msdn.microsoft.com/en-us/healthvault/dn783307
        loginURL = HealthVaultBean.SHELL_URL;
        loginURL += "/redirect.aspx?target=APPAUTH";
        //loginURL += "&appid=" + LoginBean.APP_ID;
        loginURL += "&targetqs=appid%3D" + HealthVaultBean.APP_ID; //%3D --> =
        loginURL += "%26actionqs%3DAppUserId"; //%26 -> &
        loginURL += "%26ismra%3Dtrue"; //Multi-record applications : https://msdn.microsoft.com/en-us/healthvault/dn799044 
        loginURL += "%26redirect=http://localhost:8080/MT_POC_EPD_HV_INTEGRATION/healthVault.xhtml";//for development only @todo remove in production (or use a flag)

        authURL = loginURL.replace("APPAUTH", "AUTH");
    }

    /**
     * Check if the request contains an authentication token. If yes, extract it
     * in a local variable and put it also in session. If no, check if we have a
     * token in session.
     *
     * @param request
     */
    private void _extractAuthTokenIfExists(HttpServletRequest request) {
        //check if we have a new token in the URL
        String target = request.getParameter("target");
        if ("AppAuthSuccess".equalsIgnoreCase(target)) {
            String t = (String) request.getParameter("wctoken");
            if (t != null) {
                authToken = t;
                request.getSession().setAttribute(HealthVaultBean.SESSION_AUTH_NAME, authToken);
                return;
            }
        }
        //check if we have a token in session
        authToken = (String) request.getSession().getAttribute(HealthVaultBean.SESSION_AUTH_NAME);
    }

    /**
     * Get the id of the person's record we are working on (a user can have
     * multiple records) Check if we have the id in session, otherwise call the
     * platform to get the current record id and store it in session.
     *
     * @param request
     */
    private void _recoverRecordId(HttpServletRequest request) {
        //check if we have a recordId in session
        recordId = (String) request.getSession().getAttribute(HealthVaultBean.SESSION_RECORD_ID);
        if (recordId != null && recordId.isEmpty() == false) {
            return;
        }

        //call the platfrom to get the current record id
        try {
            Request req = new Request();
            req.setTtl(3600 * 8 + 300);
            req.setMethodName("GetPersonInfo");
            req.setUserAuthToken(authToken);
            HVAccessor accessor = new HVAccessor();
            accessor.send(req, _getConnection());
            InputStream is = accessor.getResponse().getInputStream();

            XPath xpath = XPathFactory.newInstance().newXPath();
            String exp = "//record/@id";
            recordId = xpath.evaluate(exp, new InputSource(is));
            //put in session
            request.getSession().setAttribute(HealthVaultBean.SESSION_RECORD_ID, recordId);
        } catch (Exception e) {
            //store the error
            lastError = e.toString();
            String s = "";
            for (StackTraceElement ste : e.getStackTrace()) {
                s += ste.toString();
                s += "<br />";
            }
            lastErrorStackTrace = s;
        }
    }
    
    /***
     * Extract the filters from the request if they exists
     * @param request 
     */
    private void _extractFiltersIfExist(HttpServletRequest request) {
        if(request.getParameter("filter") == null) {
            has_filters = false;
            Date date = new Date();
            filter_to = filter_date_sdf.format(date);
            date = new Date(date.getTime() - 31104000000L); //minus 360 days 1000*60*60*24*360
            filter_from = filter_date_sdf.format(date);
        } else {
            //extract filters
            has_filters = true;
            filter_from = request.getParameter("from"); //@todo check if empty and format not correct
            filter_to = request.getParameter("to"); //@todo check if empty and format not correct
            filter_weight = request.getParameter("weight") != null && request.getParameter("weight").equals("on");
            filter_bloodPressure = request.getParameter("blood_pressure") != null && request.getParameter("blood_pressure").equals("on");
            filter_bloodGlucose = request.getParameter("blood_glucose") != null && request.getParameter("blood_glucose").equals("on");
        }
    }

    /**
     * If we are authenticated and we gto filters, we build the CDA document
     *
     * @param request
     */
    private void _buildDocumentIfNeeded(HttpServletRequest request) {
        //are we connected ?
        if (authToken == null) {
            return;
        }

        //have we filters ?        
        if (has_filters == false) {
            return;
        }

        //get the measures
        List measures = this._getMeasures();
        if (measures == null) {
            return;
        }

        //convert to CDA
        try {
            //build CDA
            CDAMeasures cda = new CDAMeasures(measures, filter_from, filter_to);
            StringWriter sw = new StringWriter();
            CDAUtil.save(cda.getDoc(), sw);
                        
            //encapsulate in local object
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy kk:yy:ss");
            tempCDADocument = new CDADocument(UUID.randomUUID().toString(), "Healthvault measures (" + sdf.format(new Date()) + ")", sw.toString()); //@todo adapt title to filters
            //store into temp session
             request.getSession().setAttribute(Viewer.SESSION_TEMP_NAME, tempCDADocument);
        } catch (Exception e) {
            //store the error
            lastError = e.toString();
            String s = "";
            for (StackTraceElement ste : e.getStackTrace()) {
                s += ste.toString();
                s += "<br />";
            }
            lastErrorStackTrace = s;
        }
    }

    /**
     * Return a list of measures based on filters.
     *
     * @param request
     * @return
     */
    private List<Thing> _getMeasures() {
        try {
            //https://msdn.microsoft.com/en-us/healthvault/dn783318
            //https://developer.healthvault.com/Methods/Overview?Name=GetThings&Version=3
            
            //build request filter
            String filter = "<info><group><filter>";
            if(filter_weight) filter += "<type-id>" + HealthVaultConstants.THING_TYPE_WEIGHT + "</type-id>";
            if(filter_bloodPressure) filter += "<type-id>" + HealthVaultConstants.THING_TYPE_BLOOD_PRESSURE + "</type-id>";
            if(filter_bloodGlucose) filter += "<type-id>" + HealthVaultConstants.THING_TYPE_BLOOD_GLUCOSE + "</type-id>";
            filter += "<eff-date-min>" + this._getFilterFullDateTime(filter_from) + "</eff-date-min>";
            filter += "<eff-date-max>" + this._getFilterFullDateTime(filter_to) + "</eff-date-max>";
            filter += "</filter><format><section>core</section><xml/></format></group></info>";
            
            //*******************builds HealthVault request
            Request request = new Request();
        
            //@todo doesn't work
            //request.setLanguage("FR");
            //request.setCountry("CH");
            request.setMethodName("GetThings");
            request.setInfo(filter);

            request.setTtl(3600 * 8 + 300);
            request.setUserAuthToken(authToken);
            request.setRecordId(recordId);

            HVAccessor accessor = new HVAccessor();
            accessor.send(request, _getConnection());

            //************** convert results to list (to be displayed to the user)
            InputStream istream = accessor.getResponse().getInputStream();
            InputSource isource = new InputSource(istream);
            XPath xpath = XPathFactory.newInstance().newXPath();
            String exp = "//thing";
            NodeList things = (NodeList) xpath.evaluate(exp, isource, XPathConstants.NODESET);
            List<Thing> thingList = new ArrayList<>();
            int count = Math.min(50, things.getLength());
            for (int i = 0; i < count; i++) {
                Node thing = things.item(i);
                //thingList.add(TypeManager.unmarshal(thing));
                thingList.add(ThingManager.unmarshal(thing));
            }

            //************** convert to string (to have results as xml for debugging purpose)
            istream.reset();

            StringBuilder inputStringBuilder = new StringBuilder();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(istream, "UTF-8"));
            String line = bufferedReader.readLine();
            while (line != null) {
                inputStringBuilder.append(line);
                inputStringBuilder.append('\n');
                line = bufferedReader.readLine();
            }
            //lastXMLResult = new String(inputStringBuilder.toString());
            lastXMLResult = inputStringBuilder.toString();
            istream.close();

            return thingList;
        } catch (Exception e) {
            //store the error
            lastError = e.toString();
            String s = "";
            for (StackTraceElement ste : e.getStackTrace()) {
                s += ste.toString();
                s += "<br />";
            }
            lastErrorStackTrace = s;
        }
        return null;
    }
    
    /**
     * Convert display date (dd/MM/yyyy) to full date time.
     * @param date
     * @return 
     */
    private String _getFilterFullDateTime(String date) {
        Date d = null;
        try {
            d = this.filter_date_sdf.parse(date);
        } catch(Exception e) {
            d = new Date(); //@todo handler error better
        }
        
        SimpleDateFormat utc = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");            
        return utc.format(d)+":00";
    }

    /**
     * Return a connection to HealthVault
     *
     * @return
     * @throws Exception
     */
    private Connection _getConnection() throws Exception {
        Connection connection = new Connection();

        connection.setAppId(HealthVaultBean.APP_ID);
        //connection.setSessionToken(authToken);

        connection.setTransport(_getTransport());
        connection.setAuthenticator(_getAuthenticator());

        connection.authenticate();

        return connection;
    }

    /**
     * Return a HealthVault Transport object (with the correct URL)
     *
     * @return
     * @throws Exception
     */
    private static Transport _getTransport() throws Exception {
        int connectTimeout = 0;
        int readTimeout = 0;

        URL url = new URL(HealthVaultBean.PLATFORM_URL);
        URLConnectionTransport transport = new URLConnectionTransport();
        transport.setConnectionTimeout(connectTimeout);
        transport.setReadTimeout(readTimeout);
        transport.setUrl(url);

        return transport;
    }

    /**
     * Return a HealthVault authenticator based on the private/public
     * certificate of the application
     *
     * @return
     */
    private static synchronized Authenticator _getAuthenticator() {
        ApplicationAuthenticator authenticator = authenticatorMap.get(HealthVaultBean.APP_ID);
        if (authenticator == null) {
            String filename = HealthVaultBean.KEYSTORE_NAME;
            String keyName = HealthVaultBean.KEYSTORE_ALIAS;
            String keyStorePassword = HealthVaultBean.KEYSTORE_PASSWORD;

            DefaultPrivateKeyStore keyStore = new DefaultPrivateKeyStore();
            keyStore.setAlias(keyName);
            keyStore.setPassword(keyStorePassword);
            keyStore.setFilename(filename);

            authenticator = new ApplicationAuthenticator();
            authenticator.setSharedSecretGenerator(new DefaultSharedSecret());
            authenticator.setAppId(HealthVaultBean.APP_ID);
            authenticator.setKeyStore(keyStore);

            authenticatorMap.put(HealthVaultBean.APP_ID, authenticator);
        }
        return authenticator;
    }
  
    
    /**
     * ******** GETTER / SETTER ********************
     */
    public DocumentsBean getDocumentsBean() {
        return documentsBean;
    }

    public void setDocumentsBean(DocumentsBean documentsBean) {
        this.documentsBean = documentsBean;
        
        //check if we need to store the current tempDocument into the main documents'bean in session (in temp list of documents)
        //if(tempCDADocument != null)
        //    documentsBean.addTempDocument(tempCDADocument);
    }

    public String getLoginURL() {
        return loginURL;
    }

    public String getAuthURL() {
        return authURL;
    }

    public String getAuthToken() {
        return authToken;
    }

    public String getLastError() {
        return lastError;
    }

    public String getLastErrorStackTrace() {
        return lastErrorStackTrace;
    }

    public String getLastXMLResult() {
        return lastXMLResult;
    }

    public CDADocument getTempCDADocument() {
        return tempCDADocument;
    }

    public String getRecordId() {
        return recordId;
    }

    public String getFilter_from() {
        return filter_from;
    }

    public String getFilter_to() {
        return filter_to;
    }

    public Boolean getFilter_weight() {
        return filter_weight;
    }

    public Boolean getFilter_bloodPressure() {
        return filter_bloodPressure;
    }

    public Boolean getFilter_bloodGlucose() {
        return filter_bloodGlucose;
    }

    public Boolean getDEBUG_MODE() {
        return DEBUG_MODE;
    }
}