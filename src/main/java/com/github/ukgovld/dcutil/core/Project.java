/******************************************************************
 * File:        Project.java
 * Created by:  Dave Reynolds
 * Created on:  24 Dec 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.github.ukgovld.dcutil.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.epimorphics.dclib.framework.ConverterProcess;
import com.epimorphics.dclib.framework.DataContext;
import com.epimorphics.dclib.framework.Template;
import com.epimorphics.dclib.templates.TemplateFactory;
import com.epimorphics.tasks.ProgressMonitor;
import com.epimorphics.tasks.SimpleProgressMonitor;
import com.epimorphics.util.NameUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Represents an individual data conversion project including source data,
 * template and metadata. A project's content is stored in a single directory/bucket
 * and all file names are relative to that directory.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class Project {
    public static final String OUTPUT_FILE = "output.ttl";
    
    protected String root;          // Base folder within the filestore for this project
    protected String templateName;
    protected String sourceFile;
    protected String shortname;     // Notation to use for the generated collection 
    protected String metadataFile;  // May be uploaded and/or edited
    protected String localTemplateFile;
    
    @JsonIgnore
    protected ProjectManager pm;
    
    protected void setProjectManager(ProjectManager pm) {
        this.pm = pm;
    }
    
    public String getRoot() {
        return root;
    }
    public void setRoot(String root) {
        this.root = root;
    }
    
    public String getTemplateName() {
        return templateName;
    }
    public void setTemplateName(String templateFile) {
        this.templateName = templateFile;
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
        this.shortname = NameUtils.safeName(shortname);
    }
    
    public String getMetadataFile() {
        return metadataFile;
    }
    
    public void setMetadataFile(String metadataFile) {
        this.metadataFile = metadataFile;
    }

    public String getLocalTemplateFile() {
        return localTemplateFile;
    }

    public void setLocalTemplateFile(String localTemplateFile) {
        this.localTemplateFile = localTemplateFile;
    }

    public String fullFileName(String file) {
        return root + "/" + file;
    }
    
    @JsonIgnore
    public DataContext getDataContext() throws IOException {
        DataContext dc = pm.getDataContext();
        if (localTemplateFile != null) {
            InputStream is = readFile(localTemplateFile);
            Template template = TemplateFactory.templateFrom(is, dc);
            is.close();
            
            dc = new DataContext(dc);
            dc.registerTemplate(template);
        }
        return dc;
    }
    
    
    /**
     * Perform the data conversion for a project. Does this synchronously
     */
    public ProgressMonitor convert() throws IOException {
        DataContext dc = getDataContext();
        Template template = dc.getTemplate( templateName );
        InputStream is = readFile(sourceFile);
        ConverterProcess process = new ConverterProcess(dc, is);
//        process.setDebug(debug);
        process.setTemplate( template );
        SimpleProgressMonitor reporter = new SimpleProgressMonitor();
        process.setMessageReporter( reporter );
        boolean ok = process.process();
        if (ok) {
            OutputStream out = pm.getStore().write( fullFileName(OUTPUT_FILE) );
            process.getModel().write(out, "Turtle");
            out.close();
        }
        return reporter;
    }
    
    /**
     * Save the project state
     */
    public void sync() throws IOException {
        OutputStream out = pm.getStore().write(getRoot() + "/" + ProjectManager.PROJECT_FILENAME);
        pm.getMapper().writeValue(out, this);
        out.close();
    }
    
    /**
     * Preview the source data
     * @throws IOException 
     */
    public CSVPreview preview(int maxRows) throws IOException {
        return new CSVPreview(readFile(sourceFile), maxRows);
    }
    
    private InputStream readFile(String file) throws IOException {
        return pm.getStore().read( fullFileName(file) );
    }
}
