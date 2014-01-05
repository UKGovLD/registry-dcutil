/******************************************************************
 * File:        Project.java
 * Created by:  Dave Reynolds
 * Created on:  24 Dec 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.github.ukgovld.dcutil.core;

import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import com.epimorphics.appbase.core.AppConfig;
import com.epimorphics.dclib.framework.ConverterProcess;
import com.epimorphics.dclib.framework.DataContext;
import com.epimorphics.dclib.framework.Template;
import com.epimorphics.dclib.templates.TemplateFactory;
import com.epimorphics.rdfutil.RDFUtil;
import com.epimorphics.tasks.ProgressMonitor;
import com.epimorphics.tasks.SimpleProgressMonitor;
import com.epimorphics.util.NameUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.FileUtils;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * Represents an individual data conversion project including source data,
 * template and metadata. A project's content is stored in a single directory/bucket
 * and all file names are relative to that directory.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class Project {
    public static final String OUTPUT_FILE = "output.ttl";
    public static final String DEFAULT_METADATA_FILE = "metadata.ttl";
    static final String DUMMY = "http://example.com/DONOTUSE/";
    
    protected String root;          // Base folder within the filestore for this project
    protected String templateName;
    protected String sourceFile;
    protected String shortname;     // Notation to use for the generated collection 
    protected String metadataFile;  // May be uploaded and/or edited
    protected String localTemplateFile;
    protected String resultFile;

    @JsonIgnore    protected DataContext dc;
    @JsonIgnore    protected ProjectManager pm;
    @JsonIgnore    protected MetadataModel metadata;
    @JsonIgnore    protected Model result;
    @JsonIgnore    protected CSVPreview preview;
    @JsonIgnore    protected Template template;
    
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
        template = null;
    }
    
    public String getSourceFile() {
        return sourceFile;
    }
    public void setSourceFile(String sourceFile) {
        this.sourceFile = sourceFile;
        preview = null;
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
        metadata = null;
    }

    public String getLocalTemplateFile() {
        return localTemplateFile;
    }

    public void setLocalTemplateFile(String localTemplateFile) {
        this.localTemplateFile = localTemplateFile;
        dc = null;
        template = null;
    }

    public String fullFileName(String file) {
        return root + "/" + file;
    }
    
    
    public String getResultFile() {
        return resultFile;
    }

    public void setResultFile(String resultFile) {
        this.resultFile = resultFile;
    }

    @JsonIgnore
    public DataContext getDataContext() throws IOException {
        if (dc == null) {
            dc = new DataContext( pm.getDataContext() );
            if (localTemplateFile != null) {
                InputStream is = readFile(localTemplateFile);
                Template template = TemplateFactory.templateFrom(is, dc);
                is.close();
                dc.registerTemplate(template);
            }
            dc.setPrefixes( getMetadata().getModel() );
        }
        return dc;
    }
    
    @JsonIgnore
    public MetadataModel getMetadata() throws IOException {
        if (metadata == null) {
            Model model = ModelFactory.createDefaultModel();
            if (metadataFile != null) {
                String lang = FileUtils.guessLang(metadataFile, FileUtils.langTurtle);
                InputStream in = pm.getStore().read( fullFileName(metadataFile) );
                model.read(in, DUMMY, lang);
                model = RDFUtil.mapNamespace(model, DUMMY, "");
            } else {
                model.setNsPrefixes( AppConfig.getApp().getPrefixes() );
                Resource root = model.createResource(shortname)
                        .addProperty(RDFS.label, shortname, "en")
                        .addProperty(DCTerms.description, "Description ...", "en")
                        .addProperty(DCTerms.license, model.createResource("http://www.nationalarchives.gov.uk/doc/open-government-licence/version/2/"));
                Resource rights = model.createResource().addProperty(MetadataModel.attributionText, "Contains public sector information licensed under the Open Government Licence v2.0.");
                root.addProperty(DCTerms.rights, rights);
            }
            metadata = new MetadataModel(model);
        }
        return metadata;
    }
    
    /**
     * Perform the data conversion for a project. Does this synchronously
     */
    public ProgressMonitor convert() throws IOException {
        DataContext dc = getDataContext();
        dc.getGlobalEnv().put("$base", shortname);
        Template template = dc.getTemplate( templateName );
        InputStream is = readFile(sourceFile);
        ConverterProcess process = new ConverterProcess(dc, is);
//        process.setDebug(debug);
        process.setTemplate( template );
        SimpleProgressMonitor reporter = new SimpleProgressMonitor();
        Model outputModel = ModelFactory.createDefaultModel();
        Model metamodel = getMetadata().getModel();
        outputModel.setNsPrefixes( metamodel );
        outputModel.add( metamodel );
        process.setMessageReporter( reporter );
        process.setModel(outputModel);
        boolean ok = process.process();
        if (ok) {
            if (resultFile == null) {
                resultFile = OUTPUT_FILE;
                sync();
            }
            OutputStream out = pm.getStore().write( fullFileName(resultFile) );
            outputModel.write(out, "Turtle");
            out.close();
        }
        return reporter;
    }
    
    @JsonIgnore
    public Model getResult() throws IOException {
        if (result == null && resultFile != null) {
            InputStream in = pm.getStore().read( fullFileName(resultFile) );
            result = ModelFactory.createDefaultModel();
            result.read(in, DUMMY, FileUtils.langTurtle);
            result = RDFUtil.mapNamespace(result, DUMMY, "");
        }
        return result;
    }
    
    @JsonIgnore
    public ResultWrapper getWrappedResult() throws IOException {
        return new ResultWrapper(getResult(), shortname);
    }
    
    /**
     * Save the project state
     */
    public synchronized void sync() throws IOException {
        OutputStream out = pm.getStore().write(getRoot() + "/" + ProjectManager.PROJECT_FILENAME);
        pm.getMapper().writeValue(out, this);
        out.close();
    }
    
    /**
     * Save an updated metadata state, which might involve saving the project as well
     */
    public synchronized void syncMetadata() throws IOException {
        boolean changed = false;
        if ( ! shortname.equals(metadata.getShortname()) ) {
            shortname = metadata.getShortname();
            changed = true;
        }
        if (metadataFile != DEFAULT_METADATA_FILE) {
            metadataFile = DEFAULT_METADATA_FILE;
            changed = true;
        } 
        if (changed) {
            sync();
        }
        OutputStream os = pm.getStore().write(getRoot() + "/" + metadataFile);
        metadata.getModel().write(os, FileUtils.langTurtle);
        os.close();
    }
    
    /**
     * Preview the source data
     * @throws IOException 
     */
    public CSVPreview preview(int maxRows) throws IOException {
        if (preview == null) {
            preview = new CSVPreview(readFile(sourceFile), maxRows);
        }
        return preview;
    }
    
    private InputStream readFile(String file) throws IOException {
        return pm.getStore().read( fullFileName(file) );
    }
    
    /**
     * Access the template
     */
    public Template getTemplate() throws IOException {
        if (template == null) {
            template = getDataContext().getTemplate(templateName);
        }
        return template;
    }
    
    /**
     * Compute list of required columns that are missing
     */
    public List<String> missingColumns() throws IOException {
        List<String> missing = new ArrayList<>();
        Template t = getTemplate();
        if (t != null && sourceFile != null) {
            String[] headers = preview(500).getHeaders();
            for (String col : t.required()) {
                boolean foundIt = false;
                for (String h : headers) {
                    if (h.equals(col)) {
                        foundIt = true;
                        break;
                    }
                }
                if (!foundIt) {
                    missing.add( col );
                }
            }
        }
        return missing;
    }
}
