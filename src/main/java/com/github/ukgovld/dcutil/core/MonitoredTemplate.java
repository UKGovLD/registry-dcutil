/******************************************************************
 * File:        MonitoredTemplate.java
 * Created by:  Dave Reynolds
 * Created on:  26 Mar 2014
 * 
 * (c) Copyright 2014, Epimorphics Limited
 *
 *****************************************************************/

package com.github.ukgovld.dcutil.core;

import com.epimorphics.appbase.monitor.ConfigInstance;
import com.epimorphics.dclib.framework.Template;

/**
 * Wraps up a template so it can act as a monitored configuration instance.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class MonitoredTemplate implements ConfigInstance {
    protected Template template;
    
    public MonitoredTemplate(Template template) {
        this.template = template;
    }
    
    public Template getTemplate() {
        return template;
    }

    @Override
    public String getName() {
        return template.getName();
    }
    
}
