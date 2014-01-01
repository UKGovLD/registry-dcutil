/******************************************************************
 * File:        CSVPreview.java
 * Created by:  Dave Reynolds
 * Created on:  1 Jan 2014
 * 
 * (c) Copyright 2014, Epimorphics Limited
 *
 *****************************************************************/

package com.github.ukgovld.dcutil.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.epimorphics.dclib.framework.BindingEnv;
import com.epimorphics.dclib.sources.CSVInput;

/**
 * Provides an in-memory preview of a CSV data source.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class CSVPreview {

    protected String[] headers;
    protected List<BindingEnv> rows = new ArrayList<>();
    
    public CSVPreview(InputStream source, int maxRows) throws IOException {
        CSVInput in = new CSVInput(source);
        headers = in.getHeaders();
        for (int i = 0; i < maxRows; i++) {
            BindingEnv row = in.nextRow();
            if (row == null) break;
            rows.add(row);
        }
        in.close();
    }

    public String[] getHeaders() {
        return headers;
    }

    public List<BindingEnv> getRows() {
        return rows;
    }
    
    
}
