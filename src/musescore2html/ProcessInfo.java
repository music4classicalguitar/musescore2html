package musescore2html;

import java.util.concurrent.Callable;

class ProcessInfo implements Callable<Integer> {
	private ProcessData processData;
	private Translations translations;
	private int code=0;

	public ProcessInfo(ProcessData processData, Translations translations) {
		this.processData = processData;
		this.translations = translations;
	}

	public Integer call() throws Exception {
		try {
			while (!processData.isFinished()) {
				while (processData.hasData()) {
					ProcessData.Data data[]=processData.getData();
					for (int i=0; i<data.length; i++) {
						code=code|data[i].code;
						if (code>0) System.err.println(data[i].message);
						else System.out.println(data[i].message);
					}
				}
			}
		} catch (InterruptedException iexc) {
			iexc.printStackTrace();
			if (iexc.getMessage()!=null) System.err.println(translations.translate(new String[]{"processinfo.error.message", iexc.getMessage()}));
			else System.err.println(translations.translate("processinfo.error"));
		} catch (Exception exc) {
			exc.printStackTrace();
			if (exc.getMessage()!=null) System.err.println(translations.translate(new String[]{"processinfo.error.message", exc.getMessage()}));
			else System.err.println(translations.translate("processinfo.error"));
		}
		return code;
	}
}