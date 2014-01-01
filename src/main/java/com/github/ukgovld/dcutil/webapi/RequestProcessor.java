/******************************************************************
 * File:        RequestProcessor.java
 * Created by:  Dave Reynolds
 * Created on:  24 Dec 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.github.ukgovld.dcutil.webapi;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.appbase.core.AppConfig;
import com.epimorphics.appbase.templates.VelocityRender;
import com.epimorphics.appbase.webapi.WebApiException;
import com.epimorphics.dclib.storage.FileStore;
import com.epimorphics.util.FileUtil;
import com.github.ukgovld.dcutil.core.Project;
import com.github.ukgovld.dcutil.core.ProjectManager;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;

/**
 * Handle REST API requests to implement the data converter utility 
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
@Path("")
public class RequestProcessor {
    static final Logger log = LoggerFactory.getLogger( RequestProcessor.class );
    
    protected @Context ServletContext context;
    protected @Context UriInfo uriInfo;
    protected @Context HttpServletRequest request;
    
    protected ProjectManager projectManager = AppConfig.getApp().getComponentAs("projectManager", ProjectManager.class);
    protected VelocityRender velocity =  AppConfig.getApp().getComponentAs("velocity", VelocityRender.class);
    
    @GET
    @Produces("text/html")
    public StreamingOutput listProjects() {
        return velocity.render("main.vm", uriInfo.getPath(), context, uriInfo.getQueryParameters());
    }
    
    @POST
    @Path("system/new-project")
    @Consumes({MediaType.APPLICATION_FORM_URLENCODED})
    public Response createProject(@Context HttpHeaders hh,
            @FormParam("user") String user,
            @FormParam("shortname") String shortname) {
        // TODO get user from session instead of parameter? 
        try {
            Project project = projectManager.createProject(user);
            if (shortname != null) {
                project.setShortname(shortname);
                project.sync();
            }
            return redirect(project, null);
        } catch (IOException e) {
            log.error("Failed to create project", e);
            throw new WebApiException(500, "I/O error trying to create the project: " + e);
        }
    }
    

    @POST
    @Path("system/upload-data")
    @Consumes({MediaType.MULTIPART_FORM_DATA})
    public Response fileForm(@Context HttpHeaders hh,
            @FormDataParam("project") String projectID,
            @FormDataParam("file") InputStream uploadedInputStream,
            @FormDataParam("file") FormDataContentDisposition fileDetail ) {

        String filename = fileDetail.getFileName();

        Project project = projectManager.getProject(projectID);
        if (project == null) {
            throw new WebApiException(500, "Can't locate the project: " + projectID);
        }

        // Upload file
        try {
            FileStore store = projectManager.getStore();
            OutputStream out = store.write(project.getRoot() + "/" + filename);
            FileUtil.copyResource(uploadedInputStream, out);
            out.close();
        } catch (IOException e) {
            log.error("Failed to upload data file", e);
            throw new WebApiException(Status.BAD_REQUEST, "Failed to upload data file: " + e);
        }
        
        // update project
        project.setSourceFile(filename);
        sync(project);
        return redirect(project, null);
    }
    
    @POST
    @Path("system/select-template")
    @Consumes({MediaType.APPLICATION_FORM_URLENCODED})
    public Response selectTemplate(@Context HttpHeaders hh, 
            @FormParam("project") String projectID,
            @FormParam("template") String templateName,
            @FormParam("tab") String tab) {
        Project project = projectManager.getProject(projectID);
        project.setTemplateName(templateName);
        sync(project);
        return redirect(project, tab);
    }
    
    private void sync(Project project) {
        try {
            project.sync();
        } catch (IOException e) {
            log.error("Failed to update project", e);
            throw new WebApiException(500, "I/O error trying to update the project: " + e);
        }
    }
    
    private Response redirect(Project project, String tab) {
        UriBuilder builder = uriInfo.getBaseUriBuilder().path("show-project").queryParam("project", project.getRoot());
        if (tab != null) {
            builder.queryParam("tab", tab);
        }
        return Response.seeOther(builder.build()).build();
    }
    
}
