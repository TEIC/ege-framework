package pl.psnc.dl.ege.webapp.servlethelpers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import pl.psnc.dl.ege.EGE;
import pl.psnc.dl.ege.EGEImpl;
import pl.psnc.dl.ege.configuration.EGEConstants;
import pl.psnc.dl.ege.webapp.request.InfoRequestResolver;
import pl.psnc.dl.ege.webapp.request.Method;
import pl.psnc.dl.ege.webapp.request.RequestResolver;
import pl.psnc.dl.ege.webapp.request.RequestResolvingException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;


public class Info extends HttpServlet {
    HttpServlet servlet;
    private static final Logger LOGGER = LogManager
            .getLogger(Info.class);

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
            EGE ege = new EGEImpl();
            //get info from each converter
            //add info which sources are used /usr/share/xml/tei/odd/VERSION
            //TEIROOT default is /usr/share/xml/tei/
            if(new File(EGEConstants.TEIROOT + "odd/VERSION").isFile()){
                String versionodd = Files.readString(Paths.get(EGEConstants.TEIROOT + "odd/VERSION"), StandardCharsets.UTF_8);
                if (!(versionodd == null)) {
                    json_info.put("tei-odd-version", versionodd.replaceAll("\\n", ""));
                }
            }
            if(new File(EGEConstants.TEIROOT + "stylesheet/VERSION").isFile()){
                String versionstylesheets = Files.readString(Paths.get(EGEConstants.TEIROOT + "stylesheet/VERSION"), StandardCharsets.UTF_8);
                if (!(versionstylesheets == null)) {
                    json_info.put("tei-stylesheet-version", versionstylesheets.replaceAll("\\n", ""));
                }
            }
            //add info which MEI de version is used
            //music-encoding/meidev/GITHASH
            if(new File(EGEConstants.MEIROOT + "music-encoding/meidev/GITHASH").isFile()){
                String versionmeidev = Files.readString(Paths.get(EGEConstants.MEIROOT + "music-encoding/meidev/GITHASH"), StandardCharsets.UTF_8);
                if (!(versionmeidev == null)){
                    json_info.put("mei-dev-githash", versionmeidev.replaceAll("\\n", ""));
                }
            }
            if(new File(EGEConstants.MEIROOT + "music-stylesheets/encoding-tools/GITHASH").isFile()){
                String versionmeitools = Files.readString(Paths.get(EGEConstants.MEIROOT + "music-stylesheets/encoding-tools/GITHASH"), StandardCharsets.UTF_8);
                if (!(versionmeitools == null)){
                    json_info.put("mei-encoding-tools-githash", versionmeitools.replaceAll("\\n", ""));
                }
            }
            //resolve request and catch any errors
            RequestResolver rr = new InfoRequestResolver(request,
                    Method.GET);
            //print available info
            //EGE ege = new EGEImpl();
            //List<ConversionsPath> paths = ege.findConversionPaths(idt);
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
        catch (IOException e) {
            e.printStackTrace();
        }
        LOGGER.debug("REQUEST: " + request.getRequestURL() + "; " + request.getMethod() + "; " + response.getStatus());
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
