package pl.psnc.dl.ege.webapp.servlethelpers;

import org.json.JSONException;
import org.json.JSONObject;
import pl.psnc.dl.ege.webapp.request.InfoRequestResolver;
import pl.psnc.dl.ege.webapp.request.Method;
import pl.psnc.dl.ege.webapp.request.RequestResolver;
import pl.psnc.dl.ege.webapp.request.RequestResolvingException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Properties;

public class Info extends HttpServlet {
    HttpServlet servlet;

    public void doGetHelper(HttpServletRequest request, HttpServletResponse response, HttpServlet httpservlet)
            throws IOException, ServletException {
        servlet = httpservlet;
        //String serverInfo = servlet.getServletContext().getServerInfo();
        try {
            //create info json object
            JSONObject json_info = new JSONObject();
            json_info.put("webservice-version", getVersion(request));
            //json_info.put("server-version", serverInfo);
            //json_info.put("os-info", System.getProperty("os.name") + " " + System.getProperty("os.version"));
            json_info.put("java-version", Runtime.version().toString());
            //resolve request and catch any errors
            RequestResolver rr = new InfoRequestResolver(request,
                    Method.GET);
            //print available info
            printInfo(response, rr, json_info);
        }
        catch (RequestResolvingException ex) {
            if (ex.getStatus().equals(
                    RequestResolvingException.Status.WRONG_METHOD)) {
                //TODO : something with "wrong" method message (and others)
                response.sendError(405, Conversion.R_WRONG_METHOD);
            }
            else {
                throw new ServletException(ex);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public synchronized String getVersion(HttpServletRequest request) {
        String version = null;

        // try to load from maven properties first
        try {
            Properties p = new Properties();
            //to do: this needs to be done dynamically so the garage name doesn't matter
            InputStream is = request.getServletContext().getResourceAsStream("/META-INF/maven/pl.psnc.dl.ege.webapp/meigarage/pom.properties");
            if (is == null){
                is = request.getServletContext().getResourceAsStream("/META-INF/maven/pl.psnc.dl.ege.webapp/teigarage/pom.properties");
            }
            if (is != null) {
                p.load(is);
                version = p.getProperty("version", "not found");
            }
            is.close();
        } catch (Exception e) {
            // ignore
        }

        // fallback to using Java API
        if (version == null) {
            Package aPackage = servlet.getClass().getPackage();
            if (aPackage != null) {
                version = aPackage.getImplementationVersion();
                if (version == null) {
                    version = aPackage.getSpecificationVersion();
                }
            }
        }

        if (version == null) {
            // we could not compute the version so use a blank
            version = "";
        }

        return version;
    }

    /*
     * Print info response
     */
    private void printInfo(HttpServletResponse response,
                                           RequestResolver rr, JSONObject json_info)
            throws ServletException
    {
        try {
            response.setContentType("text/xml;charset=utf-8");
            PrintWriter out = response.getWriter();
            if(json_info.length() == 0){
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                return;
            }
            response.setContentType("application/json");
            out.println(json_info);
            out.close();
        }
        catch (IOException ex) {
            throw new ServletException(ex);
        }
    }
}
