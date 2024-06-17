package pl.psnc.dl.ege.webapp.servlethelpers;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pl.psnc.dl.ege.EGE;
import pl.psnc.dl.ege.EGEImpl;
import pl.psnc.dl.ege.configuration.EGEConstants;
import pl.psnc.dl.ege.exception.CustomizationException;
import pl.psnc.dl.ege.types.CustomizationSetting;
import pl.psnc.dl.ege.types.CustomizationSourceInputType;
import pl.psnc.dl.ege.utils.EGEIOUtils;
import pl.psnc.dl.ege.webapp.request.CustomizationRequestResolver;
import pl.psnc.dl.ege.webapp.request.Method;
import pl.psnc.dl.ege.webapp.request.RequestResolver;
import pl.psnc.dl.ege.webapp.request.RequestResolvingException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class Customization {

    private static final Logger LOGGER = LogManager
            .getLogger(Customization.class);

    private static final long serialVersionUID = 1L;

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    public void doGetHelper(
            HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        LOGGER.debug("REQUEST: " + request.getRequestURL() + " " + request.getContextPath() + " " + request.toString());
        try {
            //resolve request and catch any errors
            RequestResolver rr = new CustomizationRequestResolver(request,
                    Method.GET);
            //print available validation options
            printAvailableCustomizationSettings(response, rr);
        } catch (RequestResolvingException ex) {
            if (ex.getStatus().equals(
                    RequestResolvingException.Status.WRONG_METHOD)) {
                //TODO : something with "wrong" method message (and others)
                response.sendError(405, Conversion.R_WRONG_METHOD);
            } else {
                throw new ServletException(ex);
            }
        }
    }

    /*
     * Print into response available validation options
     */
    private void printAvailableCustomizationSettings(HttpServletResponse response,
                                                     RequestResolver rr)
            throws ServletException {
        EGE ege = new EGEImpl();
        response.setContentType("text/xml;charset=utf-8");
        try {
            PrintWriter out = response.getWriter();
            Set<CustomizationSetting> css = ((EGEImpl) ege).returnSupportedCustomizationSettings();
            if (css.size() == 0) {
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                return;
            }
            String baseprefix = rr.getRequest().getScheme() + "://" +
                    rr.getRequest().getServerName() + ((rr.getRequest().getServerPort() == 80 ||  rr.getRequest().getServerPort() == 443) ? "" : ":" + rr.getRequest().getServerPort())  +
                    rr.getRequest().getContextPath() + (rr.getRequest().getContextPath().endsWith(
                    RequestResolver.SLASH) ? "" : "/");
            out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            out.println("<customizations xmlns:xlink=\"http://www.w3.org/1999/xlink\"  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"" +
                    baseprefix + "schemas/customizations.xsd\">");
            for (CustomizationSetting cs : css) {
                out.println("<customization-setting id=\"" + cs.toString() + "\">");

                out.println("<sources>");
                for (CustomizationSourceInputType s : cs.getSources())
                    out.println("<source id=\"" + s.getId() + "\" name=\"" + s.getName() + "\" type=\"" + s.getType() + "\" path=\"" + s.getPath() + "\"/>");
                out.println("</sources>");

                out.println("<customizations>");
                for (CustomizationSourceInputType s : cs.getCustomizations())
                    out.println("<customization id=\"" + s.getId() + "\" name=\"" + s.getName() + "\" type=\"" + s.getType() + "\" path=\"" + s.getPath() + "\"/>");
                out.println("</customizations>");

                out.println("<outputFormats>");
                for (String s : cs.getOutputFormats())
                    out.println("<outputFormat name=\"" + s + "\"/>");
                out.println("</outputFormats>");


                out.println("</customization-setting>");
            }
            out.println("</customizations>");
            out.close();
        } catch (IOException ex) {
            throw new ServletException(ex);
        }
    }


    public void doPostHelper(
            HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
            LOGGER.debug("REQUEST: " + request.getRequestURL() + " " + request.getContextPath());
        try {
            RequestResolver rr = new CustomizationRequestResolver(request,
                    Method.POST);
            String[] cs = (String[]) rr.getData();
            performCustomization(cs, rr, response);
        } catch (RequestResolvingException ex) {
            if (ex.getStatus().equals(
                    RequestResolvingException.Status.BAD_REQUEST)) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            } else {
                throw new ServletException(ex);
            }
        } catch (CustomizationException ex) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
    }

    /*
     * Performs customization.
     */
    private void performCustomization(String[] csString, RequestResolver rr,
                                      HttpServletResponse response) throws Exception {
        String usedCS = csString[0];
        String usedSource = csString[1];
        String usedCustomization = csString[2];
        String usedOutputFormat = csString[3];

        LOGGER.debug("performCustomization(usedCS: " + usedCS
                + ", usedSource" + usedSource
                + ", usedCustomization" + usedCustomization
                + ", usedOutputFormat" + usedOutputFormat + ")");

        EGE ege = new EGEImpl();
        Set<CustomizationSetting> css = ((EGEImpl) ege).returnSupportedCustomizationSettings();

        CustomizationSetting cs = null;

        for (CustomizationSetting c : css) {
            if (c.getFormat().equals(usedCS)) {
                cs = c;
                break;
            }
        }

        if (cs == null)
            throw new CustomizationException(); //TODO: Better error handling

        CustomizationSourceInputType source = null;
        for (CustomizationSourceInputType i : cs.getSources()) {
            if (i.getId().equals(usedSource)) {
                source = i;
                break;
            }
        }

        if (source == null)
            throw new CustomizationException(); //TODO: Better error handling

        CustomizationSourceInputType customization = null;
        for (CustomizationSourceInputType i : cs.getCustomizations()) {
            if (i.getId().equals(usedCustomization)) {
                customization = i;
                break;
            }
        }

        if (customization == null)
            throw new CustomizationException(); //TODO: Better error handling

        File tmpDir = prepareTempDir();
        HashMap<String, File> fields = parseUploadedFormFields(rr.getRequest(), tmpDir);

        File localSourceFile = null;
        File localCustomizationFile = null;

        if (fields.containsKey("source"))
            localSourceFile = fields.get("source");
        if (fields.containsKey("customization"))
            localCustomizationFile = fields.get("customization");

        String filename = (usedCustomization.startsWith("c-") ? usedCustomization.substring(2) : usedCustomization);
        if (usedCustomization.equals("c-mei-local")) {
            try {
                filename = ((File) fields.get("customization")).getName();
            } catch (Exception e) {
                LOGGER.debug("Cannot find customization file");
            }
        }
        String fileextension;
        if (usedOutputFormat.equals("RelaxNG")) {
            fileextension = ".rng";
        } else if (usedOutputFormat.equals("Compiled ODD")) {
            fileextension = ".odd";
        } else {
            fileextension = ".xml";
        }
        response.setHeader("Content-Disposition", "attachment;filename=\"" + filename + fileextension + "\"");
        LOGGER.debug("Going to perform customization");

        ((EGEImpl) ege).performCustomization(cs, source, customization,
                usedOutputFormat, response.getOutputStream(),
                localSourceFile, localCustomizationFile);

        if (tmpDir != null && tmpDir.exists())
            EGEIOUtils.deleteDirectory(tmpDir);
    }

    private HashMap<String, File> parseUploadedFormFields(HttpServletRequest request, File tmpDir) throws Exception {

        HashMap<String, File> map = new HashMap<String, File>();

        DiskFileItemFactory factory = new DiskFileItemFactory();
        factory.setSizeThreshold(0);
        ServletFileUpload upload = new ServletFileUpload(factory);

        List<?> items = null;
        try {
            items = upload.parseRequest(request);
        } catch (FileUploadException e) {
            e.printStackTrace();
        }

        if (items != null) {
            Iterator<?> iter = items.iterator();
            while (iter.hasNext()) {
                FileItem item = (FileItem) iter.next();

                if (!item.isFormField()) {
                    if (item.getFieldName().equals("source_canonical_file")) {

                        File source = new File(tmpDir + "/" + item.getName());
                        item.write(source);
                        map.put("source", source);

                    } else if (item.getFieldName().equals("local_customization_file")) {

                        File customization = new File(tmpDir + "/" + item.getName());
                        item.write(customization);
                        map.put("customization", customization);
                    }
                }
            }
        }

        return map;
    }

    private File prepareTempDir() {
        File inTempDir = null;
        String uid = UUID.randomUUID().toString();
        inTempDir = new File(EGEConstants.TEMP_PATH + File.separator + uid
                + File.separator);
        inTempDir.mkdir();
        LOGGER.info("Temp dir created: " + inTempDir.getAbsolutePath());
        return inTempDir;
    }
}
