/******************************************************************
 * File:        ResultWrapper.java
 * Created by:  Dave Reynolds
 * Created on:  2 Jan 2014
 * 
 * (c) Copyright 2014, Epimorphics Limited
 *
 *****************************************************************/

package com.github.ukgovld.dcutil.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.epimorphics.rdfutil.ModelWrapper;
import com.epimorphics.rdfutil.RDFNodeWrapper;
import com.epimorphics.registry.vocab.Ldbp;
import com.epimorphics.registry.vocab.RegistryVocab;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * Package up the result of a data conversion for scripted viewing.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class ResultWrapper {
    protected ModelWrapper model;
    protected RDFNodeWrapper root;
    protected List<RDFNodeWrapper> entries = new ArrayList<>();
    
    public ResultWrapper(Model result, String rootName) {
        model = new ModelWrapper(result);
        root = model.getNode(rootName);
        
        Resource membershipProp = RDFS.member;
        boolean inverse = false;
        RDFNodeWrapper p = root.getPropertyValue(RegistryVocab.inverseMembershipPredicate);
        if (p != null) {
            membershipProp = p.asResource();
            inverse = true;
        } else {
            p = root.getPropertyValue(Ldbp.membershipPredicate);
            if (p != null) {
                membershipProp = p.asResource();
            }
        }
        entries = inverse ? root.listInLinks(membershipProp) : root.listPropertyValues(membershipProp);
        Collections.sort(entries, new Comparator<RDFNodeWrapper>() {
            @Override
            public int compare(RDFNodeWrapper o1, RDFNodeWrapper o2) {
                return o1.getURI().compareTo( o2.getURI() );
            }
        });
    }

    public ModelWrapper getModel() {
        return model;
    }

    public RDFNodeWrapper getRoot() {
        return root;
    }

    public List<RDFNodeWrapper> getEntries() {
        return entries;
    }
    
}
