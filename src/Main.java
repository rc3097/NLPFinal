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

import main.TweetVectorizer;
import nlp.langmodel.MaxentTesterModel1;
import nlp.langmodel.MaxentTesterModel2;
import nlp.langmodel.SentimentQuantifier;
import nlp.util.CommandLineUtils;
import nlp.util.Pair;

public class Main {

	HashMap<String, TweetSet> training;
	HashMap<String, TweetSet> test;
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
	

	

	
	
	public HashMap<String, TweetSet> readTweets(String filename) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(filename));
		String line = null;
		String topic = null;
		HashMap<String, TweetSet> tweetSets = new HashMap<String, TweetSet>();
		List<LabeledTweet> tweetSet = new ArrayList<LabeledTweet>();
		line = reader.readLine();
		String[] fields = line.split("\t");
		topic = fields[0];
		while (line != null) {
			if (!topic.equalsIgnoreCase(fields[0])) {
				tweetSets.put(topic,new TweetSet(tweetSet));
				tweetSet = new ArrayList<LabeledTweet>();
				topic = fields[0];
			}
			String tweet = fields[2];
			if (tweet.equalsIgnoreCase("not available")) {
				line = reader.readLine();
				continue;
			}
			boolean positive = fields[1].equalsIgnoreCase("positive");
			LabeledTweet labeledTweet = new LabeledTweet(tweet, positive);  
			tweetSet.add(labeledTweet);
			line = reader.readLine();
		}
		return tweetSets;
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
		SentimentQuantifier LM = null;

		if (argMap.containsKey("-path")) {
			basePath = argMap.get("-path");
			traindata = SC.reader(basePath);
		}

		if (argMap.containsKey("-positive")) {
			basePath = argMap.get("-positive");
			HashMap<Pair<String, String>, List<List<String>>> positivewords = SC
					.reader(basePath);
			traindata.putAll(positivewords);
		}

		if (argMap.containsKey("-negative")) {
			basePath = argMap.get("-negative");
			HashMap<Pair<String, String>, List<List<String>>> negativewords = SC
					.reader(basePath);
			traindata.putAll(negativewords);
		}

		if (argMap.containsKey("-method")) {
			model = argMap.get("-method");
			if (model.equals("ME1")) {
				LM = new MaxentTesterModel1();

			} else if (model.equals("ME2")) {
				LM = new MaxentTesterModel2();
			} else if (model.equalsIgnoreCase("stanford")) {
				TweetVectorizer.initialize();
				
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
