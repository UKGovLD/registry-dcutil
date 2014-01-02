/******************************************************************
 * File:        MetadataModel.java
 * Created by:  Dave Reynolds
 * Created on:  2 Jan 2014
 * 
 * (c) Copyright 2014, Epimorphics Limited
 *
 *****************************************************************/

package com.github.ukgovld.dcutil.core;

import com.epimorphics.rdfutil.RDFUtil;
import com.epimorphics.registry.vocab.RegistryVocab;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.util.ResourceUtils;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * Provides access to metadata and settings for a conversion project.
 * <p>
 * Allows for arbitrary metadata upload as an RDF file but provides
 * convenience methods for the standard metadata fields.
 * </p>
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class MetadataModel {
    protected Model model;
    protected Resource root;
    
    static final Property entityType = ResourceFactory.createProperty("http://environment.data.gov.uk/registry/structure/ui/entityType");
    static final Property attributionText = ResourceFactory.createProperty("http://schema.theodi.org/odrs#attributionText");

    /**
     * Constructor. Assumes the model contains a single root resource whose (relative) URI is the shortname. 
     */
    public MetadataModel(Model model) {
        this.model = model;
        root = RDFUtil.findRoot(model);
    }
    
    public Model getModel() {
        return model;
    }
    
    public String getShortname() {
        return root.getURI();
    }
    public void setShortname(String shortname) {
        root = ResourceUtils.renameResource(root, shortname);
    }
    
    public String getLabel() { return getText(RDFS.label); }
    public void setLabel(String value) { setText(RDFS.label, value); }
    
    public String getDescription() { return getText(DCTerms.description); }
    public void setDescription(String value) { setText(DCTerms.description, value); }
    
    public String getOwner() { return getResource(RegistryVocab.owner); }
    public void setOwner(String value) { setResource(RegistryVocab.owner, value); setResource(DCTerms.publisher, value); }
    
    public String getCategory() { return getResource(RegistryVocab.category); }
    public void setCategory(String value) { setResource(RegistryVocab.category, value); }
    
    public String getEntityType() { return getResource(entityType); }
    public void setEntityType(String value) { setResource(entityType, value); }
    
    public String getLicense() { return getResource(DCTerms.license); }
    public void setLicense(String value) { setResource(DCTerms.license, value); }

    public String getAttributionText() {
        Resource rights = RDFUtil.getResourceValue(root, DCTerms.rights);
        if (rights != null) {
            return RDFUtil.getStringValue(rights, attributionText);
        } else {
            return null;
        }
    }
    public void setAttributionText(String value) {
        Resource rights = RDFUtil.getResourceValue(root, DCTerms.rights);
        if (rights == null) {
            rights = model.createResource();
            root.addProperty(DCTerms.rights, rights);
        }
        rights.removeAll(attributionText);
        rights.addProperty(attributionText, value, "en");
    }
    
    // Helper methods
    
    protected String getResource(Property prop) {
        Resource r = RDFUtil.getResourceValue(root, prop);
        return r == null ? null : r.getURI();
    }
    
    protected void setResource(Property prop, String value) {
        root.removeAll(prop);
        if (value != null && !value.isEmpty()) {
            root.addProperty(prop, model.createResource(value));
        }
    }

    protected String getText(Property prop) {
        return RDFUtil.getStringValue(root, prop);
    }
    
    protected void setText(Property prop, String value) {
        root.removeAll(prop);
        if (value != null) {
            root.addProperty(prop, value, "en");
        }
    }
}
