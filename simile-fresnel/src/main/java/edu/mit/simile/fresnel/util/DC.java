package edu.mit.simile.fresnel.util;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;
  
/**
 * Vocabulary definitions from src/rdf/ontologies/external/dublin-core-1.1.rdf 
 * @author Auto-generated by schemagen on 28 Mar 2006 15:51 
 */
public class DC {
    /** <p>The RDF model that holds the vocabulary terms</p> */
    private static Repository m_model;
    static {
    		try {
    			m_model = new SailRepository(new MemoryStore());
    			m_model.initialize();
    		} catch (RepositoryException e) {
    			System.err.println("Failed to initialize");
    		}
    }
    
    /** <p>The namespace of the vocabulary as a string</p> */
    public static final String NS = "http://purl.org/dc/elements/1.1/";
    
    /** <p>The namespace of the vocabulary as a string</p>
     *  @see #NS */
    public static String getURI() {return NS;}
    
    /** <p>The namespace of the vocabulary as a resource</p> */
    public static final Resource NAMESPACE = m_model.getValueFactory().createURI( NS );
    
    /** <p>A name given to the resource.</p> */
    public static final URI title = m_model.getValueFactory().createURI("http://purl.org/dc/elements/1.1/title" );
    
    /** <p>An entity primarily responsible for making the content of the resource.</p> */
    public static final URI creator = m_model.getValueFactory().createURI( "http://purl.org/dc/elements/1.1/creator" );
    
    /** <p>The topic of the content of the resource.</p> */
    public static final URI subject = m_model.getValueFactory().createURI( "http://purl.org/dc/elements/1.1/subject" );
    
    /** <p>An account of the content of the resource.</p> */
    public static final URI description = m_model.getValueFactory().createURI( "http://purl.org/dc/elements/1.1/description" );
    
    /** <p>An entity responsible for making the resource available</p> */
    public static final URI publisher = m_model.getValueFactory().createURI( "http://purl.org/dc/elements/1.1/publisher" );
    
    /** <p>An entity responsible for making contributions to the content of the resource.</p> */
    public static final URI contributor = m_model.getValueFactory().createURI( "http://purl.org/dc/elements/1.1/contributor" );
    
    /** <p>A date associated with an event in the life cycle of the resource.</p> */
    public static final URI date = m_model.getValueFactory().createURI( "http://purl.org/dc/elements/1.1/date" );
    
    /** <p>The nature or genre of the content of the resource.</p> */
    public static final URI type = m_model.getValueFactory().createURI( "http://purl.org/dc/elements/1.1/type" );
    
    /** <p>The physical or digital manifestation of the resource.</p> */
    public static final URI format = m_model.getValueFactory().createURI( "http://purl.org/dc/elements/1.1/format" );
    
    /** <p>An unambiguous reference to the resource within a given context.</p> */
    public static final URI identifier = m_model.getValueFactory().createURI( "http://purl.org/dc/elements/1.1/identifier" );
    
    /** <p>A reference to a resource from which the present resource is derived.</p> */
    public static final URI source = m_model.getValueFactory().createURI( "http://purl.org/dc/elements/1.1/source" );
    
    /** <p>A language of the intellectual content of the resource.</p> */
    public static final URI language = m_model.getValueFactory().createURI( "http://purl.org/dc/elements/1.1/language" );
    
    /** <p>A reference to a related resource.</p> */
    public static final URI relation = m_model.getValueFactory().createURI( "http://purl.org/dc/elements/1.1/relation" );
    
    /** <p>The extent or scope of the content of the resource.</p> */
    public static final URI coverage = m_model.getValueFactory().createURI( "http://purl.org/dc/elements/1.1/coverage" );
    
    /** <p>Information about rights held in and over the resource.</p> */
    public static final URI rights = m_model.getValueFactory().createURI( "http://purl.org/dc/elements/1.1/rights" );
    
}
