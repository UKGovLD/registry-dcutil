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
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.appbase.core.AppConfig;
import com.epimorphics.appbase.security.UserInfo;
import com.epimorphics.appbase.templates.VelocityRender;
import com.epimorphics.appbase.webapi.WebApiException;
import com.epimorphics.dclib.storage.FileStore;
import com.epimorphics.util.FileUtil;
import com.github.ukgovld.dcutil.core.DBProjectList;
import com.github.ukgovld.dcutil.core.MetadataModel;
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
    public static final String FULL_MIME_TURTLE = "text/turtle; charset=UTF-8";
    public static final String CONTENT_DISPOSITION_HEADER = "Content-Disposition";
    public static final String CONTENT_DISPOSITION_FMT = "attachment; filename=\"%s.%s\"";
    
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
    
    @Path("project/{project}")
    @GET
    @Produces("text/html")
    public StreamingOutput showProject(@PathParam("project") String project) {
        return velocity.render("show-project.vm", uriInfo.getPath(), context, uriInfo.getQueryParameters(), "project", project);
    }
    
    @Path("project/{project}/data.ttl")
    @GET
    @Produces(FULL_MIME_TURTLE)
    public Response resultDownload(@PathParam("project") String projectID) throws IOException {
        Project project = projectManager.getProject(projectID);
        return downloadresponse(project.getResult(), FULL_MIME_TURTLE, "output", "ttl");
    }

    private Response downloadresponse(Object entity, String mime, String fname, String ext) {
        ResponseBuilder builder = Response.ok().type(mime).entity(entity);
        builder.header(CONTENT_DISPOSITION_HEADER, String.format(CONTENT_DISPOSITION_FMT, fname, ext));
        return builder.build();
    }

    @POST
    @Path("system/new-project")
    @Consumes({MediaType.APPLICATION_FORM_URLENCODED})
    public Response createProject(@Context HttpHeaders hh,
            @FormParam("shortname") String shortname) {
        Subject subject = SecurityUtils.getSubject();
        if (!subject.isAuthenticated()) {
            throw new WebApiException(Status.UNAUTHORIZED, "Please register or login before creating projects");
        }
        try {
            String userid = ((UserInfo)subject.getPrincipal()).getOpenid();
            Project project = projectManager.createProject( userid );
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
    public Response uploadData(@Context HttpHeaders hh,
            @FormDataParam("project") String projectID,
            @FormDataParam("file") InputStream uploadedInputStream,
            @FormDataParam("file") FormDataContentDisposition fileDetail ) {

        Project project = projectManager.getProject(projectID);
        if (project == null) {
            throw new WebApiException(500, "Can't locate the project: " + projectID);
        }

        String filename = fileDetail.getFileName();
        uploadFile(filename, uploadedInputStream, project);

        project.resetSourceFile(filename);
        sync(project);
        return redirect(project, null);
    }

    @POST
    @Path("system/upload-metadata")
    @Consumes({MediaType.MULTIPART_FORM_DATA})
    public Response uploadMetadata(@Context HttpHeaders hh,
            @FormDataParam("project") String projectID,
            @FormDataParam("file") InputStream uploadedInputStream,
            @FormDataParam("file") FormDataContentDisposition fileDetail,
            @FormDataParam("tab") String tab) {

        Project project = projectManager.getProject(projectID);
        if (project == null) {
            throw new WebApiException(500, "Can't locate the project: " + projectID);
        }

        String filename = fileDetail.getFileName();
        uploadFile(filename, uploadedInputStream, project);

        project.setMetadataFile(filename);
        sync(project);
        return redirect(project, tab);
    }
    
    @POST
    @Path("system/select-template")
//    @Consumes({MediaType.APPLICATION_FORM_URLENCODED})
    @Consumes({MediaType.MULTIPART_FORM_DATA})
    public Response selectTemplate(@Context HttpHeaders hh, 
            @FormDataParam("project") String projectID,
            @FormDataParam("template") String templateName,
//            @FormDataParam("file") InputStream uploadedInputStream,
//            @FormDataParam("file") FormDataContentDisposition fileDetail,
            @FormDataParam("tab") String tab) {
        Project project = projectManager.getProject(projectID);
//        String filename = fileDetail.getFileName();
//        if (!filename.isEmpty()) {
//            // Upload a new template
//            uploadFile(filename, uploadedInputStream, project);
//            project.setLocalTemplateFile(filename);
//        } else {
            // Choose a system template
            if (templateName == null || templateName.isEmpty()) {
                project.setTemplateName(null);
            } else {
                project.setTemplateName(templateName);
            }
//        }
        sync(project);
        return redirect(project, tab);
    }
    
    private void uploadFile(String filename, InputStream stream, Project project ) {
        try {
            FileStore store = projectManager.getStore();
            OutputStream out = store.write(project.getRoot() + "/" + filename);
            FileUtil.copyResource(stream, out);
            out.close();
        } catch (IOException e) {
            log.error("Failed to upload file", e);
            throw new WebApiException(Status.BAD_REQUEST, "Failed to upload data file: " + e);
        }
    }
    
    @POST
    @Path("system/update-metadata")
    @Consumes({MediaType.APPLICATION_FORM_URLENCODED})
    public Response updateMetadata(@Context HttpHeaders hh, 
            @FormParam("project")     String projectID,
            @FormParam("shortname")   String shortname,
            @FormParam("label")       String label,
            @FormParam("description") String description,
            @FormParam("owner")       String owner,
            @FormParam("category")    String category,
            @FormParam("entityType")  String entityType,
            @FormParam("license")     String license,
            @FormParam("attributionText") String attributionText,
            @FormParam("tab") String tab) {
        Project project = projectManager.getProject(projectID);
        try {
            MetadataModel mm = project.getMetadata();
            mm.setShortname(shortname);
            mm.setLabel(label);
            mm.setDescription(description);
            mm.setOwner(owner);
            mm.setCategory(category);
            mm.setEntityType(entityType);
            mm.setLicense(license);
            mm.setAttributionText(attributionText);
            project.syncMetadata();
        } catch (IOException e) {
            log.error("Failed to update project metadata", e);
            throw new WebApiException(500, "I/O error trying to update the project metadata: " + e);
        }
        return redirect(project, tab);
    }
    
    @POST
    @Path("system/delete-project")
    @Consumes({MediaType.APPLICATION_FORM_URLENCODED})
    public Response deleteProject(@FormParam("project") String project) {
        Subject subject = SecurityUtils.getSubject();
        if (!subject.isAuthenticated()) {
            throw new WebApiException(Status.UNAUTHORIZED, "Please register or login before deleting projects!");
        }
        try {
            subject.checkPermission(DBProjectList.OWNER_ACTION + ":" + project);
            String userid = ((UserInfo)subject.getPrincipal()).getOpenid();
            projectManager.removeProject(userid, project);
            return Response.seeOther( uriInfo.getBaseUri() ).build();
        } catch (IOException e) {
            log.error("Failed to create project", e);
            throw new WebApiException(500, "I/O error trying to create the project: " + e);
        } catch (AuthorizationException ea) {
            log.error("User " + subject.getPrincipal() + " tried to delete project " + project + " without permission");
            throw new WebApiException(Status.UNAUTHORIZED, "Can only delete projects that you own");
        }
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
        UriBuilder builder = uriInfo.getBaseUriBuilder().path("project/" + project.getRoot());
        if (tab != null) {
            builder.queryParam("tab", tab);
        }
        return Response.seeOther(builder.build()).build();
    }
    
    
}
