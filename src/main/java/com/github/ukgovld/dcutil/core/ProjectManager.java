/******************************************************************
 * File:        ProjectManager.java
 * Created by:  Dave Reynolds
 * Created on:  24 Dec 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.github.ukgovld.dcutil.core;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.map.LRUMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.appbase.core.ComponentBase;
import com.epimorphics.dclib.framework.ConverterService;
import com.epimorphics.dclib.framework.DataContext;
import com.epimorphics.dclib.storage.FileStore;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Manages access to a collection of data conversion projects.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class ProjectManager extends ComponentBase {
    static final Logger log = LoggerFactory.getLogger( ProjectManager.class );
    
    public static final String PROJECT_FILENAME = "project.json";
    
    protected static ObjectMapper mapper = new ObjectMapper();
    
    // The directory/bucket in which all projects are stored. Configured through app machinery
    protected FileStore store;
    
    transient int count = 0;
    protected ProjectList projectList;
    protected ConverterService converterService = new ConverterService();
    protected String templateDir;
    protected LRUMap projectCache = new LRUMap(50);
    
    /**
     * Set a static template directory loaded in start up.
     * 
     * @deprecated configure a TemplateMonitor instead
     * @param templateDir
     */
    public void setTemplateDir(String  templateDir) {
        try { 
            File dir = asFile(templateDir);
            if (!dir.isDirectory() || !dir.canRead()) {
                log.error( "Can't access DC template directory: " + templateDir );
            } else {
                this.templateDir = dir.getPath();
                DataContext dc = converterService.getDataContext();
                for (String file : dir.list()) {
                    File t = new File(dir, file);
                    try {
                        dc.registerTemplate( t.getPath() );
                        log.info("DC Template: " + file);
                    } catch (IOException e) {
                        log.error( "Can't access DC template: " + t );
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to load templates directory", e);
        }
    }
    
    /**
     * Replace the default converter service with a configured service
     */
    @JsonIgnore 
    public void setConverterService(ConverterService converterService) {
        this.converterService = converterService;
    }
    
    /**
     * List all the projects associated with a given user
     */
    public synchronized List<Project> listUserProjects(String user) {
        List<String> projectNames = projectList.get(user);
        if (projectNames == null) {
            return new ArrayList<>();
        } else {
            List<Project> projects = new ArrayList<Project>( projectNames.size() );
            for (String projectDir : projectNames) {
                Project p = getProject(projectDir);
                if (p != null) {
                    projects.add( p );
                }
            }
            return projects;
        }
    }
    
    /**
     * Load a project definition from its folder name.
     */
    public synchronized Project getProject(String name) {
        Project project = (Project) projectCache.get(name);
        if (project == null) {
            try {
                project = mapper.readValue(store.read(name + "/" + PROJECT_FILENAME), Project.class);
                project.setProjectManager(this);
                projectCache.put(name, project);
            } catch (IOException e) {
                log.error("Failed to reload project file: " + name, e);
                return null;
            }
        } else {
            log.debug("Cache hit for project " + name);
        }
        return project;
    }

    /**
     * Create a new project and associated folder for a user
     */
    public synchronized Project createProject(String user) throws IOException {
        Project project = new Project( );
        project.setProjectManager(this);
        String root = allocProjectName();
        project.setRoot(root);
        project.setShortname("codelist-" + count);
        store.makeFolder(root);
        project.sync();
        projectList.add(user, root);
        return project;
    }
    
    /**
     * Delete a project area and remove it from the user's project list.
     */
    public synchronized void removeProject(String user, String project) throws IOException {
        projectList.remove(user, project);
        store.deleteFolder(project);
    }
    
    private synchronized String allocProjectName() {
        return new SimpleDateFormat("yyyyMMdd-HHmmss-").format( new Date() ) + count++;
    }
    
    public DataContext getDataContext() {
        return converterService.getDataContext();
    }
    
    public void setStore(FileStore store) {
        this.store = store;
    }
    
    public void setProjectList(ProjectList list) {
        projectList = list;
    }
    
    public FileStore getStore() {
        return store;
    }
    
    public ObjectMapper getMapper() {
        return mapper;
    }
}
