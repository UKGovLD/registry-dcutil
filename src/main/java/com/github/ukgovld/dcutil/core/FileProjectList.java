/******************************************************************
 * File:        FileProjectList.java
 * Created by:  Dave Reynolds
 * Created on:  31 Dec 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.github.ukgovld.dcutil.core;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.dclib.storage.FileStore;
import com.epimorphics.util.EpiException;

/**
 * Version of project file list used during debugging, just saves the 
 * list to a file.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class FileProjectList implements ProjectList {
    static final Logger log = LoggerFactory.getLogger( FileProjectList.class );
    public static final String PROJECT_LIST_FILENAME = "projects.json";

    protected Map<String, List<String>> userProjects;
    protected FileStore store;
    
    @SuppressWarnings("unchecked")
    public FileProjectList(FileStore store) {
        this.store = store;
        try {
            ObjectInputStream in = new ObjectInputStream( store.read(PROJECT_LIST_FILENAME) );
            userProjects = (Map<String, List<String>>)in.readObject();
            in.close();
        } catch (Exception e) {
            log.warn("Failed to load project file-based project list");
            userProjects = new HashMap<String, List<String>>();
        }
    }
    
    @Override
    public synchronized List<String> get(String user) {
        return userProjects.get(user);
    }

    @Override
    public synchronized void add(String user, String project) {
        try {
            List<String> projectNames = userProjects.get(user);
            if (projectNames == null) {
                projectNames = new ArrayList<>();
                userProjects.put(user, projectNames);
            }
            projectNames.add(project);
            ObjectOutputStream out = new ObjectOutputStream( store.write(PROJECT_LIST_FILENAME) );
            out.writeObject( userProjects );
            out.close();
        } catch (IOException e) {
            throw new EpiException(e);
        }
    }

    @Override
    public synchronized void remove(String user, String project) {
        try {
            List<String> projectNames = userProjects.get(user);
            if (projectNames == null) {
                throw new EpiException("Can't find project " + project + " for user " + user);
            }
            projectNames.remove(project);
            ObjectOutputStream out = new ObjectOutputStream( store.write(PROJECT_LIST_FILENAME) );
            out.writeObject( userProjects );
            out.close();
        } catch (IOException e) {
            throw new EpiException(e);
        }
    }

}
