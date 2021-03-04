package musescore2html;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.PropertyResourceBundle;
import java.util.ArrayList;

import java.text.MessageFormat;

import java.util.Enumeration;
import java.util.Collections;
import java.util.List;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class Translations {

   	private static final String[] languages = new String[] {"en", "nl"};
   	private static final String defaultLanguage = languages[0];
   
	private PropertyResourceBundle resourceBundle;
	private String className = this.getClass().getName();
	private Locale currentLocale = Locale.getDefault(); //, locale = currentLocale;
	private String currentLanguage = currentLocale.getLanguage(), language = currentLanguage;

	public String getKey(String key) {
		return resourceBundle.getString(key);
	}

	public String translate(String key, String[] args) {
		switch (args.length) {
			case 0: return getKey(key);
			default:
				String fmt = getKey(key);
				return (new MessageFormat(fmt)).format(args);
   		}
	}

	public String translate(String arg) {
   		return getKey(arg);
	}

	public String translate(String[] args) {
		String key = args[0];
		ArrayList<String> restArgs = new ArrayList<String>();
		for (int i=1;i<args.length; i++) {
			restArgs.add(args[i]);
   		}
   		return translate(key,restArgs.toArray(new String[] {}));
	}

	public String[] getLanguages() {
		return languages.clone();
	}

	public String getLanguage() {
		return language;
	}

	public PropertyResourceBundle getPropertyResourceBundle(String language) {
		try {
			String resource = "musescore2html/Translations"+(language.equals("")?"":"_"+language)+".properties";
			InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resource);
			InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
			return new PropertyResourceBundle(inputStreamReader);
		} catch (UnsupportedEncodingException exc) {
			exc.printStackTrace();
			if (exc.getMessage()!=null) System.err.println("Exception encoding reading translations:"+exc.getMessage());
			else System.err.println("Exception encoding reading translations");
		} catch (IOException exc) {
			exc.printStackTrace();
			if (exc.getMessage()!=null) System.err.println("Exception reading translations:"+exc.getMessage());
			else System.err.println("Exception reading translations");
		}
		return null;
	}
	
	public void setLanguage(String otherLanguage) {
		otherLanguage = otherLanguage.toLowerCase();
		int defaultIndex = 0;
		int index = -1;
		for (int i=0;i<languages.length; i++) {
			if (otherLanguage.equals(languages[i])) {
				index = i;
				break;
			}
		}
		language = languages[(index==-1)?0:index];
		//locale = new Locale(language);
		//resourceBundle = (PropertyResourceBundle) ResourceBundle.getBundle(className, locale);
		resourceBundle = getPropertyResourceBundle(language);
		if (index==-1) {
			System.err.println(translate(new String[] {"translations.not.supported",otherLanguage,defaultLanguage}));
			System.err.println(translate(new String[] {"translations.using",resourceBundle.getBaseBundleName(),language}));
		}
	}
	
	public Translations(String otherLanguage) {
		setLanguage(otherLanguage);
	}

	public Translations() {
		setLanguage(currentLanguage);
	}

	public void checkLanguage(String otherLanguage) {
		//resourceBundle = (PropertyResourceBundle) ResourceBundle.getBundle(className);
		resourceBundle = getPropertyResourceBundle("");
		//PropertyResourceBundle otherResourceBundle = (PropertyResourceBundle) ResourceBundle.getBundle(className, new Locale(otherLanguage));
		PropertyResourceBundle otherResourceBundle = getPropertyResourceBundle(otherLanguage);
		System.out.println("Comparing resources :");
		System.out.println(resourceBundle.getBaseBundleName());
		System.out.println(otherResourceBundle.getBaseBundleName()+" language '"+otherLanguage+"'");

		System.out.println();
		System.out.println("Missing keys in '"+otherLanguage+"':");
		Enumeration<String> enumeration = resourceBundle.getKeys();
		List<String> list = Collections.list(enumeration);
		Collections.sort(list);
		int missing = 0;
		for (String key : list) {
			String otherValue = (String) otherResourceBundle.handleGetObject(key);
			if (otherValue==null||otherValue.length()==0) {
				missing++;
				System.out.println(key+" = "+resourceBundle.getString(key));
			}
		}
		if (missing==0) System.out.println("No keys missing.");

		System.out.println();
		System.out.println("Unknown keys in '"+otherLanguage+"':");
		enumeration = otherResourceBundle.getKeys();
		list = Collections.list(enumeration);
		Collections.sort(list);
		int unknown = 0;
		for (String key : list) {
			String value = (String) resourceBundle.handleGetObject(key);
			if (value==null||value.length()==0) {
				unknown++;
				System.out.println(key+" = " + otherResourceBundle.getString(key));
			}
		}
		if (unknown==0) System.out.println("No unknown keys.");
	}

	public void listKeys() {
		System.out.println("Listing keys for '"+getLanguage()+"'.");
		Enumeration<String> enumeration = resourceBundle.getKeys();
		List<String> list = Collections.list(enumeration);
		Collections.sort(list);
		for (String key : list) {
			System.out.printf("key %-50s string \"%s\"%n","\"" + key+"\"",resourceBundle.getString(key));
		}
	}

	public static void main(String[] args) {
		String otherlanguage;
		Translations translations = new Translations();
		int n = 0;
		ArrayList<String> restArgs = new ArrayList<String>();
		for (int i=0;i<args.length;i++) {
			switch(args[i]) {
				case "-k":
					n++;
					translations.listKeys();
					break;
				case "-l":
					n++;
					if (i+1<args.length) {
						if (args[i+1].substring(0,1).equals("-")) System.err.println("Option '"+args[+i]+"' : missing argument");
						else {
							otherlanguage = args[++i];
							translations.checkLanguage(otherlanguage);
							System.out.println();
							n++;
						}
					} else System.err.println("Error: Option '"+args[+i]+"' : missing argument");
					break;
				default:
					restArgs.add(args[i]);
					break;
			}
		}
		if (restArgs.size()>0) System.out.println("Translation: >"+translations.translate(restArgs.toArray(new String[] {}))+"<");
	}

}
