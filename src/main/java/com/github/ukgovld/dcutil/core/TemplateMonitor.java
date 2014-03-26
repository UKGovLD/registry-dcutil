/******************************************************************
 * File:        TemplateMonitor.java
 * Created by:  Dave Reynolds
 * Created on:  26 Mar 2014
 * 
 * (c) Copyright 2014, Epimorphics Limited
 *
 *****************************************************************/

package com.github.ukgovld.dcutil.core;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.appbase.core.App;
import com.epimorphics.appbase.monitor.ConfigMonitor;
import com.epimorphics.dclib.framework.DataContext;
import com.epimorphics.dclib.framework.Template;
import com.epimorphics.util.FileUtil;

/**
 * Monitors a directory of template files, dynamically loading
 * any changes.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class TemplateMonitor extends ConfigMonitor<MonitoredTemplate> {
    static final Logger log = LoggerFactory.getLogger( TemplateMonitor.class );
    
    protected String defaultTemplatesDir;
    protected String templatesDir;
    protected DataContext dc;
    
    /**
     * Declares a directory of default templates from which the main
     * monitored directory can be initialized.
     */
    public void setDefaultTemplatesDir(String dir) {
        defaultTemplatesDir = dir;
    }
    

    /**
     * Set the directory to be monitored
     */
    public void setDirectory(String dir) {
        templatesDir = dir;
        super.setDirectory(dir);
    }
    
    /**
     * Sets the parent project manager whose data context we will update.
     */
    public void setProjectManager(ProjectManager pm) {
        dc = pm.getDataContext();
    }

    @Override
    protected Collection<MonitoredTemplate> configure(File file) {
        try {
            Template template = dc.registerTemplate( file.getPath() );
            return Collections.singletonList( new MonitoredTemplate(template) );
        } catch (IOException e) {
            log.error("Could not read template file: " + file);
            return Collections.emptyList();
        }
    }
    
    @Override
    public void startup(App app) {
        initializeTemplatesDir();
        super.startup(app);
    }

    private void initializeTemplatesDir() {
        if (defaultTemplatesDir != null && templatesDir != null) {
            File tdir = asFile(templatesDir);
            File tdef = asFile(defaultTemplatesDir);
            if (!tdef.isDirectory() || !tdef.canRead()) {
                log.error( "Can't access default DC template directory: " + tdef );
            } else {
                FileUtil.ensureDir(templatesDir);
                for (String file : tdef.list()) {
                    File src = new File(tdef, file);
                    File dest = new File(tdir, file);
                    if (!dest.exists()) {
                        try {
                            FileUtil.copyResource(src.getPath(), dest.getPath());
                        } catch (IOException e) {
                            log.error("Project initializing template " + file + " into directory " + tdir, e);
                        }
                    }
                }
            }
        }
    }

}
