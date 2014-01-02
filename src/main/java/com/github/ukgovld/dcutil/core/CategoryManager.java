/******************************************************************
 * File:        CategoryManager.java
 * Created by:  Dave Reynolds
 * Created on:  2 Jan 2014
 * 
 * (c) Copyright 2014, Epimorphics Limited
 *
 *****************************************************************/

package com.github.ukgovld.dcutil.core;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.appbase.core.ComponentBase;
import com.epimorphics.rdfutil.RDFUtil;
import com.epimorphics.registry.vocab.RegistryVocab;
import com.epimorphics.util.EpiException;
import com.epimorphics.vocabs.SKOS;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * Support for managing a set of category labels for use in project metadata.
 *
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
// TODO replace this dummy implementation by one with live access to the registry
public class CategoryManager extends ComponentBase {
    static final Logger log = LoggerFactory.getLogger( CategoryManager.class );
    
    protected Map<String, List<Entry>> tables = new HashMap<String, List<Entry>>();

    public void setDir(String dir) {
        try {
            File dirF = asFile(dir);
            for (String file : dirF.list()) {
                String tableName = file.replaceAll("\\.ttl", "");
                List<Entry> table = listFromModel( FileManager.get().loadModel( new File(dirF, file).getPath() ) );
                tables.put(tableName, table);
                log.info("Loading category table: " + tableName);
            }
        } catch (Exception e) {
            log.error("Failed to initialize category manager", e);
        }
    }
    
    public List<Entry> getTable(String listname) {
        return tables.get(listname);
    }

    protected List<Entry> listFromModel(Model model) {
        ResIterator ri = model.listSubjectsWithProperty(RDF.type, RegistryVocab.Register);
        if (!ri.hasNext()) {
            throw new EpiException("No root register in the category list");
        }
        Resource root = ri.next();
        List<Entry> entries = new ArrayList<>();
        NodeIterator ni = model.listObjectsOfProperty(root, RDFS.member);
        if (! ni.hasNext()) {
            ni = model.listObjectsOfProperty(root, SKOS.member);
        }
        if (ni.hasNext()) {
            while (ni.hasNext()) {
                RDFNode n = ni.next();
                if (n.isResource()) {
                    entries.add(new Entry((Resource)n));
                }
            }
        }
        ResIterator si = model.listSubjectsWithProperty(SKOS.inScheme, root);
        if ( si.hasNext()) {
            while (si.hasNext()) {
                Resource r = si.next();
                entries.add(new Entry(r));
            }
        }
        Collections.sort(entries, new Comparator<Entry>() {
            @Override
            public int compare(Entry o1, Entry o2) {
                return o1.getLabel().compareTo(o2.getLabel());
            }
        });
        entries.add(0, new Entry("Choose ....", ""));
        return entries;
    }
    
    public class Entry {
        protected String label;
        protected String uri;
        
        public Entry(Resource r) {
            label = RDFUtil.getLabel(r);
            uri = r.getURI();
        }
        
        public Entry(String label, String uri) {
            this.label = label;
            this.uri = uri;
        }

        public String getLabel() {
            return label;
        }

        public String getUri() {
            return uri;
        }


    }
    
}
