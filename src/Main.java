import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import nlp.util.CommandLineUtils;
import nlp.util.Pair;

public class Main {

	static class SentenceCollection  {
		static class SentenceIterator implements Iterator<Pair<String,List<String>>> {

			BufferedReader reader;

			public boolean hasNext() {
				try {
					return reader.ready();
				} catch (IOException e) {
					return false;
				}
			}

			public Pair<String,List<String>> next() {
				try {
					String line = reader.readLine();
					String[] parts = line.split("\t");
					String topic = parts[0];
					String label = parts[1];
					line = parts[2];
					String[] words = line.split("\\s+");
					List<String> sentence = new ArrayList<String>();
					for (int i = 0; i < words.length; i++) {
						String word = words[i];
						sentence.add(word.toLowerCase());
					}
					Pair<String,List<String>> resultPair = new Pair<String, List<String>>(label, sentence); 
					return resultPair;
				} catch (IOException e) {
					throw new NoSuchElementException();
				}
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}

			public SentenceIterator(BufferedReader reader) {
				this.reader = reader;
			}
		}

		String fileName;

		public Iterator<Pair<String,List<String>>> iterator() {
			try {
				BufferedReader reader = new BufferedReader(new FileReader(
						fileName));
				return new SentenceIterator(reader);
			} catch (FileNotFoundException e) {
				throw new RuntimeException("Problem with SentenceIterator for "
						+ fileName);
			}
		}

		public int size() {
			int size = 0;
			Iterator<Pair<String,List<String>>> i = iterator();
			while (i.hasNext()) {
				size++;
				i.next();
			}
			return size;
		}

		public HashMap<String, Pair<String, List<String>>> reader(String fileName){
			this.fileName = fileName;
			Iterator<Pair<String,List<String>>> sIterator = iterator();
			
			return null;
		}

	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Map<String, String> argMap = CommandLineUtils
				.simpleCommandLineParser(args);

		String basePath = ".";
		String model = "baseline";
		boolean verbose = false;

		if (argMap.containsKey("--path")) {
			basePath = argMap.get("-path");
		}

		if (argMap.containsKey("-method")) {
			model = argMap.get("-method");
		}
	}

}
