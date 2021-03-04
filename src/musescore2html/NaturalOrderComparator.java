package musescore2html;

import java.util.Comparator;
import java.util.*;
import java.text.Normalizer;

import java.io.Serializable;
import java.io.IOException;
import java.lang.ClassNotFoundException;
import java.io.ObjectStreamException;

public class NaturalOrderComparator implements Comparator <Object>, Serializable {

	private boolean ignoreCase = false;
	private int ai = 0, bi = 0;
	private char ca, cb;
	private String a, b;

	public NaturalOrderComparator() {
	}
	
	public NaturalOrderComparator(boolean ignoreCase) {
		this.ignoreCase = ignoreCase;
	}

	private static char charAt(String s, int i) {
		return i >= s.length() ? 0 : s.charAt(i);
	}

	private static boolean isDigit(char c) {
		return Character.isDigit(c) || c == '.' || c == ',';
	}

	/* The longest run of digits wins. That aside, the greatest
	   value wins, but we can't know that it will until we've scanned
	   both numbers to know that they have the same magnitude, so we
	  remember it in bias.
	  */
	private int compareRight() {
		int bias = 0;

		for (;; ai++, bi++) {
			ca = charAt(a, ai);
			cb = charAt(b, bi);

			if (!isDigit(ca) && !isDigit(cb)) return bias;
			if (!isDigit(ca)) return -1;
			if (!isDigit(cb)) return +1;
			if (ca < cb) {
				if (bias == 0) bias = -1;
			} else if (ca > cb) {
				if (bias == 0) bias = +1;
			} else if (ca == 0 && cb == 0) {
				return bias;
			}
		}
	}

	/* Compare two left-aligned numbers: the first to have a different value wins. */
	private int compareLeft() {
		int bias = 0;

		for (;; ai++, bi++) {
			ca = charAt(a, ai);
			cb = charAt(b, bi);

			if (!isDigit(ca) && !isDigit(cb)) return 0;
			if (!isDigit(ca)) return -1;
			if (!isDigit(cb)) return +1;
			if (ca < cb) return -1;
			if (ca > cb) return +1;
		}
	}

	public int compare(Object o1, Object o2) {
		if (o1 == null) {
			System.out.println("o1 is null");
			return 0;
		}
		if (o2 == null) {
			System.out.println("o2 is null");
			return 0;
		}
		a = o1.toString();
		b = o2.toString();
		if (System.getProperty("os.name").toLowerCase().startsWith("mac os x")) {
			Normalizer.normalize(a, Normalizer.Form.NFC);
			Normalizer.normalize(b, Normalizer.Form.NFC);
		}
		ai = 0;
		bi = 0;
		boolean fractional;
		int result;

		while (true) {
			ca = charAt(a, ai);
			cb = charAt(b, bi);
			//System.out.println(ai+" "+ca+" "+bi+" "+cb);
			
			// skip over leading spaces or zeros
			while (Character.isWhitespace(ca)) ca = charAt(a, ++ai);
			while (Character.isWhitespace(cb)) cb = charAt(b, ++bi);

			//System.out.println("After skipping "+ai+" "+ca+" "+bi+" "+cb);
			// Process run of digits
			if (isDigit(ca) && isDigit(cb)) {
				fractional = (ca == '0' | cb == '0');
				
				if (fractional) {
			//System.out.println("Fractional "+ai+" "+ca+" "+bi+" "+cb);
					if ((result = compareLeft()) != 0) return result;
				} else {
					if ((result = compareRight()) != 0) return result;
				}				
			}

			if (ca == 0 && cb == 0) return 0;

			if (ignoreCase) {
				ca = Character.toUpperCase(ca);
				cb = Character.toUpperCase(cb);
			}
			
			if (ca < cb) return -1;
			if (ca > cb) return +1;

			++ai;
			++bi;
		}
	}
	
	private void writeObject(java.io.ObjectOutputStream out) throws IOException {};
	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {};
	private void readObjectNoData() throws ObjectStreamException{};

    public static void main(String[] args) {
    	NaturalOrderComparator naturalOrderComparator = new NaturalOrderComparator();
    	for (int i=0; i<args.length-1; i++) {
    		for (int j=i+1; j<args.length; j++) {
    			System.out.println(">>> '"+args[i]+"' '"+args[j]+"' "+naturalOrderComparator.compare(args[i],args[j]));
    		}
    	}
    	if (args.length>0) {
			List<String> list = Arrays.asList(args);
			System.out.println("Original: ");
			for (int i=0; i<list.size(); i++) System.out.println("'"+list.get(i)+"'");
			Collections.sort(list, naturalOrderComparator);
			System.out.println("Sorted: ");
 			for (int i=0; i<list.size(); i++) System.out.println("'"+list.get(i)+"'");
   		}
    }
}