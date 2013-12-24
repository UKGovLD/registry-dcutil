/******************************************************************
 * File:        ProjectManager.java
 * Created by:  Dave Reynolds
 * Created on:  24 Dec 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.github.ukgovld.dcutil.core;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.dclib.storage.FileStore;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Manages access to a collection of data conversion projects.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class ProjectManager {
    static final Logger log = LoggerFactory.getLogger( ProjectManager.class );
    
    public static final String PROJECT_FILENAME = "project.json";
    public static final String PROJECT_LIST_FILENAME = "projects.json";
    
    protected static ObjectMapper mapper = new ObjectMapper();
    
    // The directory/bucket in which all projects are stored. Configured through app machinery
    protected FileStore store;
    
    transient int count = 0;
    
    // Temp - will be in user database
    protected Map<String, List<String>> userProjects = new HashMap<String, List<String>>();

    /**
     * List all the projects associated with a given user
     */
    public synchronized List<Project> listUserProjects(String user) {
        List<Project> projects = new ArrayList<Project>( userProjects.get(user).size() );
        for (String projectDir : userProjects.get(user)) {
            Project p = getProject(projectDir);
            if (p != null) {
                projects.add( p );
            }
        }
        return projects;
    }
    
    /**
     * Load a project definition from its folder name.
     */
    public Project getProject(String name) {
        // TODO add caching for projects
        try {
            return mapper.readValue(store.read(name + "/" + PROJECT_FILENAME), Project.class);
        } catch (IOException e) {
            log.error("Failed to reload project file: " + name, e);
            return null;
        }
    }

    /**
     * Create a new project and associated folder for a user
     */
    protected Project createProject(String user) throws IOException {
        Project project = new Project();
        String root = allocProjectName();
        project.setRoot(root);
        project.setShortname("project-" + count);
        store.makeFolder(root);
        syncProject(project);
        synchronized (this) {
            List<String> projectNames = userProjects.get(user);
            if (projectNames == null) {
                projectNames = new ArrayList<>();
                userProjects.put(user, projectNames);
            }
            projectNames.add(root);
            OutputStream out = store.write(PROJECT_LIST_FILENAME);
            mapper.writeValue(out, this);
            out.close();
        }
        return project;
    }

    /**
     * Store a project state in its folder
     */
    public void syncProject(Project project) throws IOException {
        OutputStream out = store.write(project.getRoot() + "/" + PROJECT_FILENAME);
        mapper.writeValue(out, project);
        out.close();
    }
    
    private synchronized String allocProjectName() {
        return new SimpleDateFormat("yyyyMMdd-HHmmss-").format( new Date() ) + count++;
    }
    
    public synchronized void setStore(FileStore store) {
        this.store = store;
        // Temp - until we have the user database running
        try {
            ProjectManager state = mapper.readValue( store.read(PROJECT_LIST_FILENAME), ProjectManager.class);
            userProjects = state.userProjects;
        } catch (IOException e) {
            // Assume there is no state to reload
        }
    }
    
    
}
