package org.apache.tools.ant;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.FileSystems;
import java.nio.file.NoSuchFileException;

import java.io.IOException;
import java.io.File;

import java.util.ArrayList;

import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.util.FileUtils;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.taskdefs.Copy;

public class MuseScore2HtmlUtils {
	
	public enum TYPE { FILE, DIRECTORY } ;

	public static boolean isCaseSensitiveFileSystem(Path path) throws Exception {
        if (path == null) {
            throw new IllegalArgumentException("Path cannot be null");
        }
        if (!Files.exists(path)) {
            throw new IllegalArgumentException("Path '"+path.toString()+"' does not exist");
        }
        if (!Files.isDirectory(path)) {
            throw new IllegalArgumentException("Path '"+path.toString()+"' is not a directory");
        }
        final String mixedCaseFileNamePrefix = "tSt";
        Path mixedCaseTmpFile = null;
        boolean caseSensitive = true;
        try {
            mixedCaseTmpFile = Files.createTempFile(path, mixedCaseFileNamePrefix, null);
            final Path lowerCasePath = Paths.get(mixedCaseTmpFile.toString().toLowerCase());
            try {
                caseSensitive = !Files.isSameFile(mixedCaseTmpFile, lowerCasePath);
            } catch (NoSuchFileException nsfe) {
                // a NSFE implies that the "lowerCasePath" file wasn't considered to be present
                // even if the different cased file exists. That effectively means this is a
                // case sensitive filesystem
                caseSensitive = true;
            }
        } catch (IOException ioe) {
            throw new Exception("Could not determine the case sensitivity of the " +
                    "filesystem for path " + path + " due to " + ioe);
        } finally {
            // delete the tmp file
            if (mixedCaseTmpFile != null) FileUtils.delete(mixedCaseTmpFile.toFile());
        }
        return caseSensitive;
    }
    
	public static String[] findItems(String pattern, TYPE type, boolean isCaseSensitive) {
		pattern=(new File(pattern)).getAbsolutePath();
		DirectoryScanner ds = new DirectoryScanner();
		ds.setExcludes(DirectoryScanner.getDefaultExcludes());
		ds.setIncludes(new String[] { pattern });
		ds.scan();
		ds.setCaseSensitive(isCaseSensitive);
		ds.scan();
   		return (type==TYPE.FILE)?ds.getIncludedFiles():ds.getIncludedDirectories();
	}  

	public static void main(String[] args) throws Exception {
		boolean isCaseSensitive = isCaseSensitiveFileSystem(FileSystems.getDefault().getPath(System.getProperty("java.io.tmpdir")));
		TYPE type = TYPE.FILE;
		System.out.println("org.apache.tools.ant.MuseScore2HtmlUtils main");
		for (int i=0;i<args.length;i++) {
			if (args[i].equals("-d")) type = TYPE.DIRECTORY;
			else if (args[i].equals("-f")) type = TYPE.FILE;
			else {
				String s=(type==TYPE.FILE)?"files":"directories";
				System.out.println("Search for "+s+" '"+args[i]+"'");
				String[] foundItems = findItems(args[i], type, isCaseSensitive);
				if (foundItems.length==0) System.out.println("No "+s+" found");
				else System.out.println("Found "+foundItems.length+" "+s);
				for (int f = 0; f < foundItems.length; f++) {
					System.out.println("'"+foundItems[f]+"'");
				}
			}
		}
	}

}