import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import nlp.langmodel.LanguageModel;
import nlp.langmodel.MaxentTesterModel1;
import nlp.langmodel.MaxentTesterModel2;
import nlp.util.CommandLineUtils;
import nlp.util.Pair;

public class Main {

	static class SentenceCollection {
		static class SentenceIterator implements
				Iterator<Pair<Pair<String, String>, List<String>>> {

			BufferedReader reader;

			public boolean hasNext() {
				try {
					return reader.ready();
				} catch (IOException e) {
					return false;
				}
			}

			public Pair<Pair<String, String>, List<String>> next() {
				try {
					String line = reader.readLine();
					String[] parts = line.split("\t");
					String topic = parts[0];
					String label = parts[1];
					line = parts[2];
					String[] words = line.split("\\s+");
					List<String> sentence = new ArrayList<String>();
					if (!line.equals("Not Available")) {
						for (int i = 0; i < words.length; i++) {
							String word = words[i];
							sentence.add(word.toLowerCase());
						}
					}
					Pair<String, String> headPair = new Pair<String, String>(
							topic, label);
					return new Pair<Pair<String, String>, List<String>>(
							headPair, sentence);
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

		public Iterator<Pair<Pair<String, String>, List<String>>> iterator() {
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
			Iterator<Pair<Pair<String, String>, List<String>>> i = iterator();
			while (i.hasNext()) {
				size++;
				i.next();
			}
			return size;
		}

		public HashMap<Pair<String, String>, List<List<String>>> reader(
				String fileName) {
			this.fileName = fileName;
			Iterator<Pair<Pair<String, String>, List<String>>> sIterator = iterator();
			HashMap<Pair<String, String>, List<List<String>>> resultHashMap = new HashMap<Pair<String, String>, List<List<String>>>();
			while (sIterator.hasNext()) {
				Pair<Pair<String, String>, List<String>> sPair = sIterator
						.next();
				Pair<String, String> topic_label = sPair.getFirst();
				List<String> sentencelist = sPair.getSecond();
				if (sentencelist.size() == 0)
					continue;
				if (resultHashMap.containsKey(topic_label)) {
					List<List<String>> tempLists = resultHashMap
							.get(topic_label);
					tempLists.add(sentencelist);
				} else {
					List<List<String>> tempLists = new ArrayList<List<String>>();
					tempLists.add(sentencelist);
					resultHashMap.put(topic_label, tempLists);
				}
			}
			return resultHashMap;
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Map<String, String> argMap = CommandLineUtils
				.simpleCommandLineParser(args);

		String basePath = "";
		String model = "baseline";
		String testPath = "";
		boolean verbose = false;
		SentenceCollection SC = new SentenceCollection();
		HashMap<Pair<String, String>, List<List<String>>> testdata = null;
		HashMap<Pair<String, String>, List<List<String>>> traindata = null;
		LanguageModel LM = null;

		if (argMap.containsKey("-path")) {
			basePath = argMap.get("-path");
			traindata = SC.reader(basePath);
		}

		if (argMap.containsKey("-method")) {
			model = argMap.get("-method");
			if (model.equals("ME1")) {
				LM = new MaxentTesterModel1();

			} else if (model.equals("ME2")) {
				LM = new MaxentTesterModel2();
			}
		}
		LM.train(traindata);
		if (argMap.containsKey("-test")) {
			testPath = argMap.get("-test");
			testdata = SC.reader(testPath);
		}
		System.out.println(LM.predictProbability(testdata));
	}
}
