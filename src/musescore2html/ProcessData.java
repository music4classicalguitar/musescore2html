package musescore2html;

import java.util.LinkedList;

public class ProcessData {

	public static class Data {
		public String message;
		public int code;

		public Data(String dataMessage, int dataCode) {
			message = dataMessage;
			code = dataCode;
		}
	}

	LinkedList <Data> list = new LinkedList<Data>();
	private int capacity = 2;
	private boolean finished = false;

	public synchronized void addData(String message, int code) throws InterruptedException {
		synchronized(this) {
			while (list.size() == capacity) wait();
			list.add(new Data(message, code));
			notify();
		}
	}

	public synchronized void setFinished() throws InterruptedException {
		finished = true;
		notify();
	}

	public synchronized boolean isFinished() throws InterruptedException {
		return finished&&list.size()==0;
	}

	public synchronized boolean hasData() throws InterruptedException {
		return (list.size()>0);
	}

	public synchronized Data[] getData() throws InterruptedException {
		synchronized(this) {
			while (!finished&&list.size() == 0) wait();
			Data data[] = list.toArray(new Data[0]);
			list.clear();
			notify();
			return data;
		}
	}
}