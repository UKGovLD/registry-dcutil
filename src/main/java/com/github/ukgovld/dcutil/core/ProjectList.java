/******************************************************************
 * File:        ProjectList.java
 * Created by:  Dave Reynolds
 * Created on:  31 Dec 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.github.ukgovld.dcutil.core;

import java.util.List;

/**
 * Holds a list of known projects for each user.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public interface ProjectList {
    /**
     * List all project names for the given user.
     */
    List<String> get(String user);

    /**
     * Record a new project name for the given user.
     */
    void add(String user, String project);
    
    /**
     * Remove a project from the list for the given user
     */
    void remove(String user, String project);
}
