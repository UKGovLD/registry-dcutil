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
import javax.servlet.http.Cookie;
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

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ByteSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.appbase.core.AppConfig;
import com.epimorphics.appbase.security.AppRealmToken;
import com.epimorphics.appbase.security.Login.OpenidRequest;
import com.epimorphics.appbase.security.UserInfo;
import com.epimorphics.appbase.security.UserStore;
import com.epimorphics.appbase.templates.VelocityRender;
import com.epimorphics.appbase.webapi.WebApiException;
import com.epimorphics.util.EpiException;


@Path("/system/security")
public class LoginCmds {
    static final Logger log = LoggerFactory.getLogger( LoginCmds.class );
    public static final String NOCACHE_COOKIE = "nocache";
    
    protected @Context UriInfo uriInfo;
    protected @Context ServletContext context;
    protected @Context HttpServletResponse response;

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
            return error( e.getMessage() );
        }
    }


    @Path("/pwlogin")
    @POST
    public Response pwlogin(@FormParam("userid") String userid, @FormParam("password") String password, @FormParam("return") String returnURL) {
        try {
            AppRealmToken token = new AppRealmToken(userid, password);
            Subject subject = SecurityUtils.getSubject();
            subject.login(token);
            log.info("Password login for userid " + userid);
            setNocache(response);
            if (returnURL == null || returnURL.isEmpty()) {
                returnURL = "/ui/admin";
            }
            return redirectTo( returnURL );
        } catch (Exception e) {
            log.warn(String.format("Password login failure for userid %s [%s]: %s", userid, e.getClass().toString(), e.getMessage()));
            return error("Login failed");
        }
    }

    @Path("/pwregister")
    @POST
    public Response pwregister(
            @FormParam("userid") String userid, 
            @FormParam("password") String password, 
            @FormParam("name") String name, 
            @FormParam("return") String returnURL) {
        if (userid == null || userid.isEmpty() || password == null || password.isEmpty() || name == null || name.isEmpty()) {
            return error( "You must supply all of a username, display name and password to register" );
        }
        UserStore userstore = AppConfig.getApp().getComponentAs("userstore", UserStore.class);
        UserInfo userinfo = new UserInfo(userid, name);
        if (userstore.register( userinfo )) {
            try {
                AppRealmToken token = new AppRealmToken(userid, true);
                Subject subject = SecurityUtils.getSubject();
                subject.login(token);
                
                userstore.setCredentials(userid, ByteSource.Util.bytes(password), Integer.MAX_VALUE);
                if (returnURL == null || returnURL.isEmpty()) {
                    returnURL = "/ui/admin";
                }
                return redirectTo( returnURL );
            } catch (Exception e) {
                return error("Failed to register the password: " + e);
            }            
        } else {
            return error( "That username is already registered" );
        }
    }

    @Path("/setpassword")
    @POST
    public Response setPassword(@FormParam("currentPassword") String currentPassword, @FormParam("newPassword") String newPassword, @FormParam("return") String returnURL) {
        Subject subject = SecurityUtils.getSubject();
        if (!subject.isAuthenticated()) {
            return error("You must be logged in to reset your password");
        }
        if (newPassword == null || newPassword.isEmpty()) {
            return error("Must give a new password");
        }
        String userid = ((UserInfo)subject.getPrincipal()).getOpenid();
        try {
            // Check current password in case left screen optn
            AppRealmToken token = new AppRealmToken(userid, currentPassword);
            subject.login(token);

            // Now set the password
            UserStore userstore = AppConfig.getApp().getComponentAs("userstore", UserStore.class);
            userstore.setCredentials(userid, ByteSource.Util.bytes(newPassword), Integer.MAX_VALUE);
            log.info("Changed password for user " + userid);
            setNocache(response);
            
            if (returnURL == null || returnURL.isEmpty()) {
                returnURL = "/ui/admin";
            }
            return redirectTo( returnURL );
        } catch (Exception e) {
            log.warn(String.format("Failed to change password for userid %s [%s]: %s", userid, e.getClass().toString(), e.getMessage()));
            return error("Failed to confirm login before changing password");
        }
    }

    @Path("/resetpassword")
    @POST
    public Response resetPassword(@FormParam("userid") String userid, @FormParam("newPassword") String newPassword, @FormParam("return") String returnURL) {
        if (userid == null || userid.isEmpty() || newPassword == null || newPassword.isEmpty()) {
            return error("Must give a user and a new password");
        }
        Subject subject = SecurityUtils.getSubject();
        if (subject.isAuthenticated() && subject.isPermitted("admin:*")) {
            try {
                UserStore userstore = AppConfig.getApp().getComponentAs("userstore", UserStore.class);
                userstore.setCredentials(userid, ByteSource.Util.bytes(newPassword), Integer.MAX_VALUE);
                log.info("Administrator " + subject.getPrincipal() + " changed password for user " + userid);
                
                setNocache(response);
                if (returnURL == null || returnURL.isEmpty()) {
                    returnURL = "/ui/admin";
                }
                return redirectTo( returnURL );
            } catch (Exception e) {
                log.warn(String.format("Administrator failed to change password for userid %s [%s]: %s", userid, e.getClass().toString(), e.getMessage()));
                return error("Failed to change password: " + e);
            }
        } else {
            return error("You must be logged in as an adminstrator to do this");
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

    private Response error(String message) {
        VelocityRender velocity =  AppConfig.getApp().getComponentAs("velocity", VelocityRender.class);
        StreamingOutput out =  velocity.render("error.vm", uriInfo.getPath(), context, uriInfo.getQueryParameters(), "message", message);
        return Response.status(Status.BAD_REQUEST).entity(out).build();
    }
    
    public static void setNocache(HttpServletResponse httpresponse) {
        setNocache(httpresponse, "cache bypass", 60 * 60 *24);
    }
    
//    private void removeNocache(HttpServletResponse httpresponse) {
//        setNocache(httpresponse, null, 0);
//    }
    
    private static void setNocache(HttpServletResponse httpresponse, String value, int age) {
        Cookie cookie = new Cookie(NOCACHE_COOKIE, value);
        cookie.setComment("Bypass proxy cache when logged in");
        cookie.setMaxAge(age);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        httpresponse.addCookie(cookie);
    }
}
