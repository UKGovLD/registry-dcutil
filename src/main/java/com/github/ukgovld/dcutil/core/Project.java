/******************************************************************
 * File:        Project.java
 * Created by:  Dave Reynolds
 * Created on:  24 Dec 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.github.ukgovld.dcutil.core;

/**
 * Represents an individual data conversion project including source data,
 * template and metadata. A project content is stored in a single directory/bucket
 * and all file names are relative to that directory.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class Project {
    protected String root;          // Base folder within the filestore for this project
    protected String templateFile;
    protected String sourceFile;
    protected String shortname;     // Notation to use for the generated collection 
    protected String metadataFile;  // May be uploaded and/or edited

    
    public String getRoot() {
        return root;
    }
    public void setRoot(String root) {
        this.root = root;
    }
    public String getTemplateFile() {
        return templateFile;
    }
    public void setTemplateFile(String templateFile) {
        this.templateFile = templateFile;
    }
    public String getSourceFile() {
        return sourceFile;
    }
    public void setSourceFile(String sourceFile) {
        this.sourceFile = sourceFile;
    }
    public String getShortname() {
        return shortname;
    }
    public void setShortname(String shortname) {
        this.shortname = shortname;
    }
    public String getMetadataFile() {
        return metadataFile;
    }
    public void setMetadataFile(String metadataFile) {
        this.metadataFile = metadataFile;
    }
    
    
}
