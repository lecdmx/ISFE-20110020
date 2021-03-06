/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dao;

import Datos.PDF;
import Negocios.Cifrado.Cifrado;
import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import org.apache.fop.apps.FOPException;

/**
 *
 * @author lupe
 */
public class Impresa extends HttpServlet {

    /**
     * Processes requests for both HTTP
     * <code>GET</code> and
     * <code>POST</code> methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        Sql s = new Sql();
        try {
            /*
             * TODO output your page here. You may use following sample code.
             */
            if ("Buscar".equals(request.getParameter("Factura"))) {

                String idUsuario = request.getParameter("idUsuario");
                idUsuario = Cifrado.decodificarBase64(idUsuario);
                String fecha = request.getParameter("Fecha");  /*
                 * aaaa/mm/dd
                 */

                String consulta = "SELECT COUNT(*) as contador FROM factura WHERE idUsuario = " + Integer.parseInt(idUsuario) + " AND fechaElaboracion = " + fecha + "";
                ResultSet rs;
                rs = s.consulta(consulta);


                int Columnas = 0;
                if (rs.next()) {
                    Columnas = rs.getInt("contador");
                }

                if (Columnas > 0) {
                    out.println("<table align= \"center\" id=\"ResultadoBusquedaFactua\">");
                    out.println("<tr>");
                    out.println("<th  align=\"center\">&nbsp; &nbsp; &nbsp; Nombre de la Factura &nbsp; &nbsp;</th>");
                    out.println("<th  align=\"center\">&nbsp; &nbsp; &nbsp; Fecha &nbsp; &nbsp;</th>");
                    out.println("<th  align=\"center\">&nbsp; &nbsp;</th>");
                    out.println("</tr>");

                    consulta = "SELECT f.idFactura,f.fechaElaboracion, f.nombreXML FROM factura f, folios fo WHERE f.idUsuario = " + Integer.parseInt(idUsuario) + " AND f.fechaElaboracion = " + fecha + " AND f.idFolio = fo.idFolio AND fo.usado = 1";
                    rs = s.consulta(consulta);
                    while (rs.next()) {
                        out.println("<tr>");
                        out.println("<td Style=\"font-size: 10px;\" align=\"center\">" + rs.getString("nombreXML") + "</td>");
                        out.println("<td Style=\"font-size: 10px;\" align=\"center\">" + rs.getString("fechaElaboracion") + "</td>");
                        out.println("<td align=\"center\"><span><img src=\"../images/formularios/pdfICON.jpg\" title=\"Generar PDF\" alt=\"Generar PDF\" style=\"cursor:pointer\" onClick=\"GenerarPDF(" + rs.getInt("idFactura") + ")\"/></span></td>");
                        out.println("</tr>");
                    }

                    out.println("</table>");
                    out.println("<br/><br/>");
                } else {
                    out.println("0");
                }
            }else if("generarPDF".equals(request.getParameter("Factura"))){
                Sql sqlPDF = new Sql();
                String query = "select nombreXML,facturaXML from factura where idFactura=" + request.getParameter("idFacturaImpresa") + ";";
                ResultSet rs = sqlPDF.consulta(query);
                String path=this.getServletContext().getRealPath("/");
                File xml = null;
                String nombre=null;
                while (rs.next()) {
                    nombre=rs.getString("nombreXML");
                    xml = new File(path+nombre+".xml");
                    FileOutputStream fos = new FileOutputStream(xml);
                    byte[] buffer = new byte[1];
                    InputStream is = rs.getBinaryStream(2);
                    while (is.read(buffer) > 0) {
                        fos.write(buffer);
                    }
                    fos.close();
                }
                
                File pdf = PDF.generarArchivoPDF(xml, path, nombre+".pdf");
                response.sendRedirect("PDF.jsp?nombrePDF="+nombre);
                //PDF.visualizarPDF(pdf, response, request);
                
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Impresa.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FOPException ex) {
            Logger.getLogger(Impresa.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerConfigurationException ex) {
            Logger.getLogger(Impresa.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerException ex) {
            Logger.getLogger(Impresa.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            Logger.getLogger(Impresa.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(Impresa.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(Impresa.class.getName()).log(Level.SEVERE, null, ex);

        } finally {
            out.close();
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP
     * <code>GET</code> method.
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
     * Handles the HTTP
     * <code>POST</code> method.
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
