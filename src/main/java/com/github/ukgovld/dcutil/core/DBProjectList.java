/******************************************************************
 * File:        DBProjectList.java
 * Created by:  Dave Reynolds
 * Created on:  4 Jan 2014
 * 
 * (c) Copyright 2014, Epimorphics Limited
 *
 *****************************************************************/

package com.github.ukgovld.dcutil.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.epimorphics.appbase.core.ComponentBase;
import com.epimorphics.appbase.security.AppRealm;
import com.epimorphics.appbase.security.UserStore;

public class DBProjectList extends ComponentBase implements ProjectList {
    public static final String OWNER_ACTION = "owner";
    
    protected UserStore userstore;

    public void setUserStore(UserStore userstore) {
        this.userstore = userstore;
    }

    @Override
    public List<String> get(String user) {
        List<String> projects = new ArrayList<>();
        for (String permission : userstore.getPermissions(user)) {
            if (AppRealm.permissionAction(permission).equals(OWNER_ACTION)) {
                projects.add( AppRealm.permissionPath(permission) );
            }
        }
        Collections.sort(projects);
        return projects;
    }

    @Override
    public void add(String user, String project) {
        userstore.addPermision(user, OWNER_ACTION + ":" + project);
    }

    @Override
    public void remove(String user, String project) {
        userstore.removePermission(user, project);
    }

}
