package musescore2html;

import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.TransformerConfigurationException;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;

// for debug
import javax.xml.transform.TransformerException;
import javax.xml.transform.SourceLocator;

import javax.xml.transform.sax.SAXSource;

public class XmlXslTransformer {

	static Transformer transformer = null ;
	static StreamResult output = null;

	/* For commandline usage if omitting outputFileName */
	public static void doTransform(String xmlFilename, String xslFilename)
	throws TransformerConfigurationException, Exception
	{
		try {
			transformer = getTransformer(xslFilename);
			transform(xmlFilename, System.out);
		} catch (Exception exc) {
			getInformationFromException(exc);
			throw new Exception("XML/XSL-error : "+exc.getMessage());
		}
	}

	public static void doTransform(String xmlFilename, String xslFilename, String outFilename)
	throws TransformerConfigurationException, Exception
	{
		try {
			transformer = getTransformer(xslFilename);
			transform(xmlFilename, outFilename);
		} catch (Exception exc) {
			getInformationFromException(exc);
			throw new Exception("XML/XSL-error : "+exc.getMessage());
		}
	}

	/*
	 * Setup an XSL-processor.
	 * XSL : eXtensible Stylesheet Language
	 * @param xslFilename
	 * @param outFilename
	 **/
	public static Transformer getTransformer(String xslFilename)
	throws TransformerConfigurationException, Exception
	{
		// java.lang.Exception
		checkFileIsReadableAndNonEmpty(xslFilename);

		TransformerFactory transformerFactory = TransformerFactory.newInstance();

		// set errorhandler
		XmlXslTransformExceptionListener xmlXslTransformExceptionListener = new XmlXslTransformExceptionListener();
		transformerFactory.setErrorListener(xmlXslTransformExceptionListener);

		StreamSource streamSource = new StreamSource(new File(xslFilename));

		// javax.xml.transform.TransformerConfigurationException
		Transformer transformer = transformerFactory.newTransformer(streamSource);

		return transformer;
	}

	public static void transform(String xmlFilename, OutputStream outputStream)
	throws TransformerException, Exception
	{
		// java.lang.Exception
		checkFileIsReadableAndNonEmpty(xmlFilename);

		// specify input
		StreamSource input = new StreamSource(new FileInputStream(xmlFilename)) ;

		output = new StreamResult(outputStream);

		// javax.xml.transform.TransformerException
		transformer.transform(input,output);
	}

	public static void transform(String xmlFilename, String outFilename)
	throws TransformerException, Exception
	{
		// java.lang.Exception
		checkFileIsReadableAndNonEmpty(xmlFilename);

		// specify input
		StreamSource input = new StreamSource(new FileInputStream(xmlFilename)) ;

		// java.lang.Exception
		checkFileIsWritable(outFilename);

		// java.io.FileNotFoundException
		output = new StreamResult(new FileOutputStream(outFilename));

		// javax.xml.transform.TransformerException
		transformer.transform(input,output);
	}

	public static void checkFileIsReadable(String fileName) throws Exception {
		File file = new File(fileName);
		if (! file.exists()) throw new Exception("File '" + fileName + "' not found.") ;
		if (! file.isFile()) throw new Exception("Path '" + fileName + "' is not a file.") ;
		if (! file.canRead()) throw new Exception("File '" + fileName + "' is not readable.") ;
	}

	public static void checkFileIsReadableAndNonEmpty(String fileName) throws Exception {
		checkFileIsReadable(fileName);
		File file = new File(fileName);
		long size = file.length();
		if (size == 0) throw new Exception("File '" + fileName + "' is empty.") ;
	}

	public static void checkFileIsWritable(String fileName) throws Exception {
		File file = new File(fileName);
		if (file.exists()) {
			if (! file.isFile()) throw new Exception("Path '" + fileName + "' is not a file.") ;
			if (! file.canWrite()) throw new Exception("File '" + fileName + "' is not writable.") ;
		} else {
			String directoryName = new File(file.getAbsolutePath()).getParent();
			File directory = new File(directoryName);
			if (! directory.exists()) throw new Exception("File '" + fileName + "' directory '" + directoryName + "' does not exist.");
			if (! directory.canWrite()) throw new Exception("File '" + fileName + "' directory '" + directoryName + "' is not writable.");
		}
	}

	/*
	 * Determine information from exception.
	 *
	 * @param exc  Exception
	 */
	static boolean getInformationFromException(Exception exc) throws Exception {
		boolean hasShownError = false;
		if (exc instanceof SAXException) {
			Exception pexc = ( (SAXException) exc).getException();
			hasShownError = getInformationFromException(pexc);
		}
		if (exc instanceof SAXParseException) {
			SAXParseException sexc = (SAXParseException) exc ;
			System.err.println("  PublicId    : " + sexc.getPublicId());
			System.err.println("  SystemId    : " + sexc.getSystemId());
			System.err.println("  LineNumber  : " + sexc.getLineNumber());
			System.err.println("  ColumnNumer : " + sexc.getColumnNumber());
			System.err.println("  Message     : " + exc.getMessage());
			return true;
		}
		if (exc instanceof TransformerException) {
			SourceLocator sourceLocator = ( (TransformerException) exc).getLocator();
			if (sourceLocator != null) {
				System.err.println("  PublicId    : " + sourceLocator.getPublicId());
				System.err.println("  SystemId    : " + sourceLocator.getSystemId());
				System.err.println("  LineNumber  : " + sourceLocator.getLineNumber());
				System.err.println("  ColumnNumer : " + sourceLocator.getColumnNumber());
			}
			System.err.println("  Message     : " + exc.getMessage());
			return true;
		}
		if (exc instanceof TransformerConfigurationException) {
			SourceLocator sourceLocator = ( (TransformerException) exc).getLocator();
			if (sourceLocator != null) {
				System.err.println("  PublicId    : " + sourceLocator.getPublicId());
				System.err.println("  SystemId    : " + sourceLocator.getSystemId());
				System.err.println("  LineNumber  : " + sourceLocator.getLineNumber());
				System.err.println("  ColumnNumer : " + sourceLocator.getColumnNumber());
			}
			System.err.println("  Message     : " + exc.getMessage());
			return true;
		}
		if (! hasShownError) {
			System.err.println("Exception");
			System.err.println("  StackTrace :");
			exc.printStackTrace();
			System.err.println("  Class   : '" + exc.getClass().getName() + "'") ;
			System.err.println("  Message : '" + exc.getMessage() + "'");
			return true;
		}
		return hasShownError;
	}

	public static void main(String[] args) throws Exception {
		if (args.length<2) {
			System.out.println("Usage: musescore2html.process.XmlXslTransformer xmlFilename xslFilename (outFilename)");
		}
		System.out.println("XML : '"+args[0]+"'");
		System.out.println("XSL : '"+args[1]+"'");
		if (args.length==2) {
			doTransform(args[0], args[1]);
		} else {
			System.out.println("OUT : '"+args[2]+"'");
			doTransform(args[0], args[1], args[2]);
		}
	}

}
