/******************************************************************
 * File:        Login.java
 * Created by:  Dave Reynolds
 * Created on:  4 Jan 2014
 * 
 * (c) Copyright 2014, Epimorphics Limited
 *
 *****************************************************************/

package com.github.ukgovld.dcutil.webapi;

import static com.epimorphics.appbase.security.Login.logout;
import static com.epimorphics.appbase.security.Login.processOpenID;
import static com.epimorphics.appbase.security.Login.verifyResponse;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;

import com.epimorphics.appbase.core.AppConfig;
import com.epimorphics.appbase.security.Login.OpenidRequest;
import com.epimorphics.appbase.security.UserStore;
import com.epimorphics.appbase.templates.VelocityRender;
import com.epimorphics.appbase.webapi.WebApiException;
import com.epimorphics.util.EpiException;


@Path("/system/security")
public class LoginCmds {
    protected @Context UriInfo uriInfo;
    protected @Context ServletContext context;

    @Path("/login")
    @POST
    public Response login(
            @FormParam("provider") String provider, 
            @FormParam("return") String returnURL,
            @Context HttpServletRequest request, 
            @Context HttpServletResponse response) {
        OpenidRequest oid = new OpenidRequest(uriInfo.getBaseUri().toString() + "system/security/response");
        oid.setProvider(provider);
        oid.setReturnURL(returnURL);
        try {
            processOpenID(request, response, oid);
        }  catch (Exception e) {
            throw new WebApiException(Status.BAD_REQUEST, "Login/registration action failed: " + e);
        }
        return Response.ok().build();
    }

    @Path("/register")
    @POST
    public Response register(
            @FormParam("provider") String provider, 
            @FormParam("return") String returnURL, 
            @Context HttpServletRequest request, 
            @Context HttpServletResponse response) {
        OpenidRequest oid = new OpenidRequest(uriInfo.getBaseUri().toString() + "system/security/response");
        oid.setProvider(provider);
        oid.setReturnURL(returnURL);
        oid.setRegister(true);
        try {
            processOpenID(request, response, oid);
        }  catch (Exception e) {
            throw new WebApiException(Status.BAD_REQUEST, "Login/registration action failed: " + e);
        }
        return Response.ok().build();
    }

    @Path("/logout")
    @POST
    public void doLogout(@Context HttpServletRequest request, @Context HttpServletResponse response) throws IOException {
        logout(request);
        response.sendRedirect(request.getServletContext().getContextPath());
    }

    @Path("/response")
    @GET
    public Response openIDResponse(@Context HttpServletRequest request, @Context HttpServletResponse response) {
        try {
            UserStore userstore = AppConfig.getApp().getComponentAs("userstore", UserStore.class);
            return redirectTo( verifyResponse(request, response, userstore) );
        } catch (Exception e) {
            return renderError( e.getMessage() );
        }
    }

    private Response redirectTo(String path) {
        URI uri;
        try {
            uri = new URI(path);
            return Response.seeOther(uri).build();
        } catch (URISyntaxException e) {
            throw new EpiException(e);
        }
    }

    private Response renderError(String message) {
        VelocityRender velocity =  AppConfig.getApp().getComponentAs("velocity", VelocityRender.class);
        StreamingOutput out =  velocity.render("error.vm", uriInfo.getPath(), context, uriInfo.getQueryParameters(), "message", message);
        return Response.status(Status.BAD_REQUEST).entity(out).build();
    }
}
