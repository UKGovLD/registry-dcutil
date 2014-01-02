package com.epimorphics.registry.vocab;
/* CVS $Id: $ */
 
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.ontology.*;
 
/**
 * Vocabulary definitions from file:/home/der/projects/java-workspace/ukl/ukl-registry-poc/src/main/vocabs/registry.ttl 
 * @author Auto-generated by schemagen on 29 Apr 2013 21:08 
 */
public class RegistryVocab {
    /** <p>The ontology model that holds the vocabulary terms</p> */
    private static OntModel m_model = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM, null );
    
    /** <p>The namespace of the vocabulary as a string</p> */
    public static final String NS = "http://purl.org/linked-data/registry#";
    
    /** <p>The namespace of the vocabulary as a string</p>
     *  @see #NS */
    public static String getURI() {return NS;}
    
    /** <p>The namespace of the vocabulary as a resource</p> */
    public static final Resource NAMESPACE = m_model.createResource( NS );
    
    /** <p>The ontology's owl:versionInfo as a string</p> */
    public static final String VERSION_INFO = "0.2";
    
    /** <p>An alternative URI for the entity, the alias resource is regarded by this 
     *  register as owl:sameAs the definition entity</p>
     */
    public static final ObjectProperty alias = m_model.createObjectProperty( "http://purl.org/linked-data/registry#alias" );
    
    /** <p>The URI of a graph of annotation statements associate with this item</p> */
    public static final ObjectProperty annotation = m_model.createObjectProperty( "http://purl.org/linked-data/registry#annotation" );
    
    /** <p>Optional classification for a registered item within one or more SKOS classification 
     *  schemes to support navigation and discovery. Orthogonal to the structure provided 
     *  by the register hierarchy which is about governance.</p>
     */
    public static final ObjectProperty category = m_model.createObjectProperty( "http://purl.org/linked-data/registry#category" );
    
    /** <p>A class of entities that can occur in this register</p> */
    public static final ObjectProperty containedItemClass = m_model.createObjectProperty( "http://purl.org/linked-data/registry#containedItemClass" );
    
    /** <p>The entity which has been registered.</p> */
    public static final ObjectProperty definition = m_model.createObjectProperty( "http://purl.org/linked-data/registry#definition" );
    
    /** <p>A resource to which the delegated register delegates, may be a register in 
     *  another registry service, a SPARQL endpoint or some other web resource</p>
     */
    public static final ObjectProperty delegationTarget = m_model.createObjectProperty( "http://purl.org/linked-data/registry#delegationTarget" );
    
    /** <p>The RDF resource identified by an entity reference</p> */
    public static final ObjectProperty entity = m_model.createObjectProperty( "http://purl.org/linked-data/registry#entity" );
    
    /** <p>Indicates the particular version:Version of the entity being referenced.</p> */
    public static final ObjectProperty entityVersion = m_model.createObjectProperty( "http://purl.org/linked-data/registry#entityVersion" );
    
    /** <p>Specifies the object part of a triple pattern used to enumerate the members 
     *  of a delegated register</p>
     */
    public static final ObjectProperty enumerationObject = m_model.createObjectProperty( "http://purl.org/linked-data/registry#enumerationObject" );
    
    /** <p>Specifies the predicate part of a triple pattern used to enumerate the members 
     *  of a delegated register</p>
     */
    public static final ObjectProperty enumerationPredicate = m_model.createObjectProperty( "http://purl.org/linked-data/registry#enumerationPredicate" );
    
    /** <p>Specifies the subject part of a triple pattern used to enumerate the members 
     *  of a delegated register</p>
     */
    public static final ObjectProperty enumerationSubject = m_model.createObjectProperty( "http://purl.org/linked-data/registry#enumerationSubject" );
    
    /** <p>A resource, such as a web accessible-document, which describes the governance 
     *  policy applicable to this register.</p>
     */
    public static final ObjectProperty governancePolicy = m_model.createObjectProperty( "http://purl.org/linked-data/registry#governancePolicy" );
    
    /** <p>Indicates a property which links a member of a collection back to the collection 
     *  itself, this is the reverse direction to the normal ldp:membershipPredicate</p>
     */
    public static final ObjectProperty inverseMembershipPredicate = m_model.createObjectProperty( "http://purl.org/linked-data/registry#inverseMembershipPredicate" );
    
    /** <p>The type of the entity that this record is about. Note that it may be possible 
     *  to register a non-RDF resource in which case this property provides a way 
     *  to state the intended class of the entity even though no direct RDF assertion 
     *  of type is available.</p>
     */
    public static final ObjectProperty itemClass = m_model.createObjectProperty( "http://purl.org/linked-data/registry#itemClass" );
    
    /** <p>The manager of the register, may be a person (foaf:Person) or an organization 
     *  (org:Organization). Operates the register on behalf of the owner, makes day 
     *  to day decisions on acceptance of entries based on agreed principles but it 
     *  may be possible to appeal to the owner to override a decision by the manager.</p>
     */
    public static final ObjectProperty manager = m_model.createObjectProperty( "http://purl.org/linked-data/registry#manager" );
    
    /** <p>Indicates a language supported by the register and the items within it. The 
     *  language should be indicated by a resource within a well-maintained URI set 
     *  such as the Library of Congress language URIs e.g. http://id.loc.gov/vocabulary/iso639-1/en</p>
     */
    public static final ObjectProperty operatingLanguage = m_model.createObjectProperty( "http://purl.org/linked-data/registry#operatingLanguage" );
    
    /** <p>The owner of the register, may be a person (foaf:Person) or an organization 
     *  (org:Organization). The owner has final authority over the contents of the 
     *  regster.</p>
     */
    public static final ObjectProperty owner = m_model.createObjectProperty( "http://purl.org/linked-data/registry#owner" );
    
    /** <p>An item which has been replaced this one within the register. Should be asserted 
     *  between hub resources (VersionedThing).</p>
     */
    public static final ObjectProperty predecessor = m_model.createObjectProperty( "http://purl.org/linked-data/registry#predecessor" );
    
    /** <p>The register in which this item has been registered.</p> */
    public static final ObjectProperty register = m_model.createObjectProperty( "http://purl.org/linked-data/registry#register" );
    
    /** <p>A tagged snapshot of a register</p> */
    public static final ObjectProperty release = m_model.createObjectProperty( "http://purl.org/linked-data/registry#release" );
    
    /** <p>A resource, typically a real-world object, which the registered entity is 
     *  a representation for.</p>
     */
    public static final ObjectProperty representationOf = m_model.createObjectProperty( "http://purl.org/linked-data/registry#representationOf" );
    
    /** <p>A resource representing an RDF graph (within the Registry's SPARQL dataset) 
     *  containing the properties of the reference entity. If not present then assume 
     *  default graph.</p>
     */
    public static final ObjectProperty sourceGraph = m_model.createObjectProperty( "http://purl.org/linked-data/registry#sourceGraph" );
    
    /** <p>The status of this register entry</p> */
    public static final ObjectProperty status = m_model.createObjectProperty( "http://purl.org/linked-data/registry#status" );
    
    /** <p>The person or organization who originally submitted this register entry. Subsequent 
     *  chages to the entry may have been made by other agents.</p>
     */
    public static final ObjectProperty submitter = m_model.createObjectProperty( "http://purl.org/linked-data/registry#submitter" );
    
    /** <p>Indicates a register that is itself an entry in this principle register.</p> */
    public static final ObjectProperty subregister = m_model.createObjectProperty( "http://purl.org/linked-data/registry#subregister" );
    
    /** <p>A SPARQL ASK query which can be used to validate a proposed entry in this 
     *  register. Returns true of an error is found.</p>
     */
    public static final ObjectProperty validationQuery = m_model.createObjectProperty( "http://purl.org/linked-data/registry#validationQuery" );
    
    /** <p>The HTTP status code to return the requester in order to forward the request.</p> */
    public static final DatatypeProperty forwardingCode = m_model.createDatatypeProperty( "http://purl.org/linked-data/registry#forwardingCode" );
    
    /** <p>A short text string which can be used to denote the register item. Must be 
     *  unique within the register. If available it should be used as the path segment, 
     *  relative to the parent register, for the RegisterItem (and for the item itself, 
     *  if managed). Restricted to be a syntactically legal URI segment (i.e. *pchar).</p>
     */
    public static final DatatypeProperty notation = m_model.createDatatypeProperty( "http://purl.org/linked-data/registry#notation" );
    
    /** <p>The source of a SPARQL query</p> */
    public static final DatatypeProperty query = m_model.createDatatypeProperty( "http://purl.org/linked-data/registry#query" );
    
    /** <p>The tag used to label a collection which snapshots the state of a register</p> */
    public static final DatatypeProperty tag = m_model.createDatatypeProperty( "http://purl.org/linked-data/registry#tag" );
    
    /** <p>A register item which represents some form of delegation</p> */
    public static final OntClass Delegated = m_model.createClass( "http://purl.org/linked-data/registry#Delegated" );
    
    /** <p>A register whose member contents are determined through delegation to a SPARQL 
     *  endpoint</p>
     */
    public static final OntClass DelegatedRegister = m_model.createClass( "http://purl.org/linked-data/registry#DelegatedRegister" );
    
    /** <p>A reference to some internal or external Linked Data resource. The reg:reference 
     *  gives the URI of the resource being referenced. If a reg:sourceGraph value 
     *  is present then it is the URI for a named graph within the Registry containing 
     *  the properties of the referenced entity. If reg:entityVersion is present it 
     *  gives URI for the particular version:Version of the entity being referenced. 
     *  Normally only one of reg:sourceGraph and reg:entityVersion is needed since 
     *  versioned entities are normally stored in the default graph.</p>
     */
    public static final OntClass EntityReference = m_model.createClass( "http://purl.org/linked-data/registry#EntityReference" );
    
    /** <p>A register item which forwards all requests to a remote register. Queries 
     *  which traverse the register hierarchy such as entity search will also be forwarded</p>
     */
    public static final OntClass FederatedRegister = m_model.createClass( "http://purl.org/linked-data/registry#FederatedRegister" );
    
    /** <p>A register item which simply forwards all requests to the delegation target.</p> */
    public static final OntClass NamespaceForward = m_model.createClass( "http://purl.org/linked-data/registry#NamespaceForward" );
    
    /** <p>Represents a collection of registered items, together with some associated 
     *  governance regime. If one or more licenses is stated then each license applies 
     *  to all the entries in the register.</p>
     */
    public static final OntClass Register = m_model.createClass( "http://purl.org/linked-data/registry#Register" );
    
    /** <p>A metadata record for an entry in a register. Note that cardinality constraints 
     *  can be met by sub-properties, for example an item with a skos:prefLabel implies 
     *  an rdfs:label and so meets the cardinality constraint on rdfs:label.</p>
     */
    public static final OntClass RegisterItem = m_model.createClass( "http://purl.org/linked-data/registry#RegisterItem" );
    
    /** <p>Represents a SPARQL ASK query as might be used for validation.</p> */
    public static final OntClass SPARQLAskQuery = m_model.createClass( "http://purl.org/linked-data/registry#SPARQLAskQuery" );
    
    /** <p>Represents a SPARQL CONSTRUCT query.</p> */
    public static final OntClass SPARQLConstructQuery = m_model.createClass( "http://purl.org/linked-data/registry#SPARQLConstructQuery" );
    
    /** <p>Represents a SPARQL query as a reusable resource.</p> */
    public static final OntClass SPARQLQuery = m_model.createClass( "http://purl.org/linked-data/registry#SPARQLQuery" );
    
    /** <p>Represents a SPARQL SELECT query.</p> */
    public static final OntClass SPARQLSelectQuery = m_model.createClass( "http://purl.org/linked-data/registry#SPARQLSelectQuery" );
    
    /** <p>Open set of status code for entries in a register</p> */
    public static final OntClass Status = m_model.createClass( "http://purl.org/linked-data/registry#Status" );
    
    /** <p>Concept scheme containing registry status codes</p> */
    public static final Individual StatusScheme = m_model.createIndividual( "http://purl.org/linked-data/registry#StatusScheme", m_model.createClass( "http://www.w3.org/2004/02/skos/core#ConceptScheme" ) );
    
    /** <p>An entry that has been accepted for use and is visible in the default register 
     *  listing. Includes entries that have seen been retired or superseded.</p>
     */
    public static final Individual statusAccepted = m_model.createIndividual( "http://purl.org/linked-data/registry#statusAccepted", Status );
    
    /** <p>An entry that has been retired or replaced and is no longer to be used.</p> */
    public static final Individual statusDeprecated = m_model.createIndividual( "http://purl.org/linked-data/registry#statusDeprecated", Status );
    
    /** <p>An entry that has been accepted into the register temporarily and may be subject 
     *  to change or withdrawal.</p>
     */
    public static final Individual statusExperimental = m_model.createIndividual( "http://purl.org/linked-data/registry#statusExperimental", Status );
    
    /** <p>An entry which has been invalidated due to serious flaws, distinct from retrirement. 
     *  Corresponds to ISO 19135(redraft) 'invalid'</p>
     */
    public static final Individual statusInvalid = m_model.createIndividual( "http://purl.org/linked-data/registry#statusInvalid", Status );
    
    /** <p>An entry that should not be visible in the default register listing. Corresponds 
     *  to ISO 19135:2005 'notValid'</p>
     */
    public static final Individual statusNotAccepted = m_model.createIndividual( "http://purl.org/linked-data/registry#statusNotAccepted", Status );
    
    /** <p>A reserved entry allocated for some as yet undetermined future use.</p> */
    public static final Individual statusReserved = m_model.createIndividual( "http://purl.org/linked-data/registry#statusReserved", Status );
    
    /** <p>An entry that has been withdrawn from use. Corresponds to ISO 19135:2005 'retired'</p> */
    public static final Individual statusRetired = m_model.createIndividual( "http://purl.org/linked-data/registry#statusRetired", Status );
    
    /** <p>An entry that is seen as having a reasonable measure of stability, may be 
     *  used to mark the full adoption of a previously 'experimental' entry.</p>
     */
    public static final Individual statusStable = m_model.createIndividual( "http://purl.org/linked-data/registry#statusStable", Status );
    
    /** <p>A proposed entry which is not yet approved for use for use. Corresponds to 
     *  ISO 19135:(redraft) 'submitted'</p>
     */
    public static final Individual statusSubmitted = m_model.createIndividual( "http://purl.org/linked-data/registry#statusSubmitted", Status );
    
    /** <p>An entry that has been replaced by a new alternative which should be used 
     *  instead. Corresponds to ISO 19135:2005 'superseded'.</p>
     */
    public static final Individual statusSuperseded = m_model.createIndividual( "http://purl.org/linked-data/registry#statusSuperseded", Status );
    
    /** <p>An entry that has been accepted into the register and is deemed fit for use. 
     *  Corresponds to ISO 19135:2005 'valid'.</p>
     */
    public static final Individual statusValid = m_model.createIndividual( "http://purl.org/linked-data/registry#statusValid", Status );
    
}