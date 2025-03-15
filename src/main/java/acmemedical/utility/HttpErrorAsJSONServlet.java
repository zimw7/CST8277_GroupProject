/********************************************************************************************************
 * File:  HttpErrorAsJSONServlet.java
 * Course Materials CST 8277
 * @author Teddy Yap
 * @author Mike Norman
 * 
 * Note:  Students do NOT need to change anything in this class.
 *
 */
package acmemedical.utility;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.Status.MOVED_PERMANENTLY;
import static jakarta.ws.rs.core.Response.Status.OK;
import static jakarta.ws.rs.core.Response.Status.fromStatusCode;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.core.Response;

import com.fasterxml.jackson.databind.ObjectMapper;

import acmemedical.rest.resource.HttpErrorResponse;

@WebServlet({"/http-error-as-json-handler"})
public class HttpErrorAsJSONServlet extends HttpServlet implements Serializable {
    private static final long serialVersionUID = 1L;
    //Write an object to a JSON file or read a JSON to object
    static ObjectMapper objectMapper;
    public static ObjectMapper getObjectMapper() {
        return objectMapper;
    }
    public static void setObjectMapper(ObjectMapper objectMapper) {
        HttpErrorAsJSONServlet.objectMapper = objectMapper;
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        int statusCode = response.getStatus();
        if (statusCode >= OK.getStatusCode() && statusCode < (MOVED_PERMANENTLY.getStatusCode() - 1)) {
            super.service(request, response);
        }
        else {
            response.setContentType(APPLICATION_JSON);
            Response.Status status = fromStatusCode(statusCode);
            HttpErrorResponse httpErrorResponse = new HttpErrorResponse(statusCode, status.getReasonPhrase());
            String httpErrorResponseStr = getObjectMapper().writeValueAsString(httpErrorResponse);
            try (PrintWriter writer = response.getWriter()) {
                writer.write(httpErrorResponseStr);
                writer.flush();
            }
        }
    }
}