package pl.psnc.dl.ege.webapp.request;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class CustomizationRequestResolver extends RequestResolver {
    public static final String CUSTOMIZATION_SLICE_BASE = "Customization";

    public CustomizationRequestResolver(HttpServletRequest request, Method method)
            throws RequestResolvingException
    {
        this.method = method;
        this.request = request;
        init();
    }


    private void init()
            throws RequestResolvingException
    {
        if (method.equals(Method.GET)) {
            resolveGET();
        }
        else if (method.equals(Method.POST)) {
            resolvePOST();
        }
    }


    private void resolvePOST()
            throws RequestResolvingException
    {
        String[] queries = resolveQueries();
        if(queries.length > 2){
            String id = null;
            String source = null;
            String customization = null;
            String outputFormat = null;

            try {
                id = URLDecoder.decode(queries[0], "UTF-8");
                source = URLDecoder.decode(queries[1], "UTF-8");
                customization = URLDecoder.decode(queries[2], "UTF-8");
                outputFormat = URLDecoder.decode(queries[3], "UTF-8");

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                throw new RequestResolvingException(RequestResolvingException.Status.BAD_REQUEST);
            }

            data = new String[]{id, source, customization, outputFormat};
            operation = OperationId.PERFORM_CUSTOMIZATION;
        }
        else{
            throw new RequestResolvingException(RequestResolvingException.Status.BAD_REQUEST);
        }
    }


    private void resolveGET()
            throws RequestResolvingException
    {
        String[] queries = resolveQueries();
        if(queries.length == 1){
            operation = OperationId.PRINT_CUSTOMIZATIONS;
        }
        else{
            throw new RequestResolvingException(RequestResolvingException.Status.WRONG_METHOD);
        }
    }

    private String[] resolveQueries()
    {
        String params = request.getRequestURL().toString();
        params = params.endsWith(SLASH) ? params : params + SLASH;
        params = params.replaceFirst(".*" + CUSTOMIZATION_SLICE_BASE + "\\/", "");

        String[] queries = params.split(SLASH);
        return queries;
    }

    @Override
    public String getLocale()
    {
        return null;
    }
}
