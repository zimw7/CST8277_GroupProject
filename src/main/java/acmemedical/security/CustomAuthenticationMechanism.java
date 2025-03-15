/********************************************************************************************************
 * File:  CustomAuthenticationMechanism.java
 * Course Materials CST 8277
 * @author Teddy Yap
 * @author Mike Norman
 * 
 * Note:  Students do NOT need to change anything in this class.
 *
 */
package acmemedical.security;

import static jakarta.security.enterprise.identitystore.CredentialValidationResult.Status.VALID;
import static jakarta.servlet.http.HttpServletRequest.BASIC_AUTH;

import java.util.Base64;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.security.enterprise.AuthenticationException;
import jakarta.security.enterprise.AuthenticationStatus;
import jakarta.security.enterprise.authentication.mechanism.http.HttpAuthenticationMechanism;
import jakarta.security.enterprise.authentication.mechanism.http.HttpMessageContext;
import jakarta.security.enterprise.credential.UsernamePasswordCredential;
import jakarta.security.enterprise.identitystore.CredentialValidationResult;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;

@ApplicationScoped
public class CustomAuthenticationMechanism implements HttpAuthenticationMechanism {

    @Inject
    protected CustomIdentityStore identityStore;

    @Context
    protected ServletContext servletContext;

    @Override
    public AuthenticationStatus validateRequest(HttpServletRequest request, HttpServletResponse response, HttpMessageContext httpMessageContext) throws AuthenticationException {

        AuthenticationStatus result = httpMessageContext.doNothing();
        //Parse BasicAuth header
        String name = null;
        String password = null;
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader != null) {
            boolean startsWithBasic = authHeader.toLowerCase().startsWith(BASIC_AUTH.toLowerCase());
            if (startsWithBasic) {
                String b64Token = authHeader.substring(BASIC_AUTH.length() + 1, authHeader.length());
                //                                              ^^^^^^^^^^^ account for space between BASIC and base64-string
                byte[] token = Base64.getDecoder().decode(b64Token);
                String tmp = new String(token);
                String[] tokenFields = tmp.split(":");
                if (tokenFields.length == 2) {
                    name = tokenFields[0];
                    password = tokenFields[1];
                }
            }
        }
        if (name != null && password != null) {
            CredentialValidationResult validationResult = identityStore.validate(new UsernamePasswordCredential(name, password));
            if (validationResult.getStatus() == VALID) {
                String validationResultStr = String.format("valid result: callerGroups=%s, callerPrincipal=%s",
                    validationResult.getCallerGroups(), validationResult.getCallerPrincipal().getName());
                servletContext.log(validationResultStr);
                result = httpMessageContext.notifyContainerAboutLogin(validationResult);
            }
            else {
                result = httpMessageContext.responseUnauthorized();
            }
        }
        return result;
    }
}