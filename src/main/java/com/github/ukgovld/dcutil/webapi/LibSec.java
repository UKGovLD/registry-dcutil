/******************************************************************
 * File:        LibSec.java
 * Created by:  Dave Reynolds
 * Created on:  4 Jan 2014
 * 
 * (c) Copyright 2014, Epimorphics Limited
 *
 *****************************************************************/

package com.github.ukgovld.dcutil.webapi;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import com.epimorphics.appbase.core.ComponentBase;
import com.epimorphics.appbase.templates.LibPlugin;

/**
 * Velocity library plugin for access the shiro environment.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class LibSec extends ComponentBase implements LibPlugin {

    public Subject getSubject() {
        return SecurityUtils.getSubject();
    }
}
