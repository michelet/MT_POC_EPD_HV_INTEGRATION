package ch.bfh.masterarbeit.michelet_cedric.servlet;
import ch.bfh.masterarbeit.michelet_cedric.bean.DocumentsBean;
import ch.bfh.masterarbeit.michelet_cedric.model.CDADocument;
import java.io.IOException;
import java.util.Base64;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Cédric Michelet
 */
@WebServlet(urlPatterns = {"/Viewer"})
public class Viewer extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String docId = request.getParameter("docId");
        if(docId == null || docId.equals("")) return;
        
        Boolean raw = request.getParameter("raw") != null && request.getParameter("raw").equals("true");
        
        DocumentsBean documentsBean = (DocumentsBean) request.getSession().getAttribute("documentsBean");
        if(documentsBean == null) return;
        CDADocument doc = documentsBean.getDocumentById(docId);
        if(doc == null) return;
        
        //String contentType = "text/html";
        
        if(raw) {
            response.setContentType("text/xml");
            response.setCharacterEncoding("UTF-8");
            try (ServletOutputStream out =  response.getOutputStream()) {
                out.print(doc.getXmlContent());
            }
        } else {
            response.setContentType(doc.getContentType());
              
            try (ServletOutputStream out =  response.getOutputStream()) {
                if(doc.getIsBodyEncodedInBase64())
                    out.write(Base64.getDecoder().decode(doc.getBodyContent()));
                else
                    out.print(doc.getBodyContent());
            }
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
