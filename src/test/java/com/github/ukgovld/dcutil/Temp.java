/******************************************************************
 * File:        Temp.java
 * Created by:  Dave Reynolds
 * Created on:  24 Dec 2013
 * 
 * (c) Copyright 2013, Epimorphics Limited
 *
 *****************************************************************/

package com.github.ukgovld.dcutil;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.ukgovld.dcutil.core.Project;

public class Temp {

    public static void main(String[] args) throws IOException {
        Project project = new Project();
        project.setMetadataFile("metadata");
        project.setShortname("foobar");
        project.setSourceFile("source-file.csv");
        
        ObjectMapper mapper = new ObjectMapper();
        String jsonString = mapper.writeValueAsString(project);
        
        System.out.println(jsonString);
        
        Project p2 = mapper.readValue(jsonString, Project.class);
        System.out.println( "P2 = " + mapper.writeValueAsString(p2));
    }
}
