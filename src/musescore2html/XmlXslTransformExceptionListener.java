package musescore2html;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.SourceLocator;

/**
 * Catches transformer exceptions and saves the first exception
 */
class XmlXslTransformExceptionListener implements ErrorListener {

	/* ------------------------------------------------------------------------------- */
	/* javax.xml.transform.ErrorListener 

	/*
	 * Handles a transformer configuration warning
	 */
	public void warning(TransformerConfigurationException exc) throws TransformerConfigurationException {
		// Throw an exception and stop the processor
		// for a warning, because we don't expect any warnings ;

			System.err.println("TransformConfiguration warning:") ;
			System.err.println("   Message: "+exc.getMessage());
			SourceLocator esl = exc.getLocator();
			if (esl!=null) {
				System.err.println("   Public ID: "+esl.getPublicId());
				System.err.println("   System ID: "+esl.getSystemId());
				System.err.println("   Line number: "+esl.getLineNumber());
				System.err.println("   Column number: "+esl.getColumnNumber());
			}
		throw new TransformerConfigurationException(exc);


	}

	/*
	 * Handles a transformer configuration error
	 */
	public void error(TransformerConfigurationException exc) throws TransformerConfigurationException {
		// XSLT is not as draconian as XML. There are numerous errors
		// which the processor may but does not have to recover from;
		// e.g. multiple templates that match a node with the same
		// priority. I do not want to allow that so I throw this
		// exception here.

			System.err.println("TransformConfiguration error:") ;
			System.err.println("   Message: "+exc.getMessage());
			SourceLocator esl = exc.getLocator();
			if (esl!=null) {
				System.err.println("   Public ID: "+esl.getPublicId());
				System.err.println("   System ID: "+esl.getSystemId());
				System.err.println("   Line number: "+esl.getLineNumber());
				System.err.println("   Column number: "+esl.getColumnNumber());
			}

		throw new TransformerConfigurationException(exc);
	}

	/*
	 * Handles a transformer configuration fatal error
	 */
	public void fatalError(TransformerConfigurationException exc) throws TransformerConfigurationException {
		// This is an error which the processor cannot recover from;
		// e.g. a malformed stylesheet or input document
		// so I must throw this exception here.
		//			throw exception;

			System.err.println("TransformConfiguration fatal error:") ;
			System.err.println("   Message: "+exc.getMessage());
			SourceLocator esl = exc.getLocator();
			if (esl!=null) {
				System.err.println("   Public ID: "+esl.getPublicId());
				System.err.println("   System ID: "+esl.getSystemId());
				System.err.println("   Line number: "+esl.getLineNumber());
				System.err.println("   Column number: "+esl.getColumnNumber());
			}

		throw new TransformerConfigurationException(exc);
	}


	/*
	 * Handles a transformer warning
	 */
	public void warning(TransformerException exc) throws TransformerException {
		// Throw an exception and stop the processor
		// for a warning, because we don't expect any warnings ;

			System.err.println("Transform warning:") ;
			System.err.println("   Message: "+exc.getMessage());
			SourceLocator esl = exc.getLocator();
			if (esl!=null)  {
				System.err.println("   Public ID: "+esl.getPublicId());
				System.err.println("   System ID: "+esl.getSystemId());
				System.err.println("   Line number: "+esl.getLineNumber());
				System.err.println("   Column number: "+esl.getColumnNumber());
			}

		throw new TransformerException(exc);
	}

	/*
	 * Handles a transformer error
	 */
	public void error(TransformerException exc) throws TransformerException {
		// XSLT is not as draconian as XML. There are numerous errors
		// which the processor may but does not have to recover from;
		// e.g. multiple templates that match a node with the same
		// priority. I do not want to allow that so I throw this
		// exception here.

			System.err.println("Transform error:") ;
			System.err.println("   Message: "+exc.getMessage());
			SourceLocator esl = exc.getLocator();
			if (esl!=null) {
				System.err.println("   Public ID: "+esl.getPublicId());
				System.err.println("   System ID: "+esl.getSystemId());
				System.err.println("   Line number: "+esl.getLineNumber());
				System.err.println("   Column number: "+esl.getColumnNumber());
			}

		throw new TransformerException(exc);
	}

	/*
	 * Handles a transformer fatal error
	 */
	public void fatalError(TransformerException exc) throws TransformerException {
		// This is an error which the processor cannot recover from;
		// e.g. a malformed stylesheet or input document
		// so I must throw this exception here.
		//			throw exception;

			System.err.println("Transform fatal error:") ;
			System.err.println("   Message: "+exc.getMessage());
			SourceLocator esl = exc.getLocator();
			if (esl!=null) {
				System.err.println("   Public ID: "+esl.getPublicId());
				System.err.println("   System ID: "+esl.getSystemId());
				System.err.println("   Line number: "+esl.getLineNumber());
				System.err.println("   Column number: "+esl.getColumnNumber());
			}

		throw new TransformerException(exc);
	}

}

