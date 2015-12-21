import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;

import nlp.langmodel.MaxentTesterModel1;
import nlp.langmodel.MaxentTesterModel2;
import nlp.langmodel.SentimentQuantifier;
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
	

	

	
	
	public static HashMap<String, TweetSet> readTweets(String filename) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(filename));
		String line = null;
		String topic = null;
		HashMap<String, TweetSet> tweetSets = new HashMap<String, TweetSet>();
		List<LabeledTweet> tweetSet = new ArrayList<LabeledTweet>();
		line = reader.readLine();
		String[] fields = line.split("\t");
		topic = fields[0];
		while (line != null) {
			fields =line.split("\t");
			if (!topic.equalsIgnoreCase(fields[0])) {
				System.out.println("finished with "+topic+" ("+tweetSet.size()+" examples)");
				System.out.println("starting with "+fields[0]);
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
		reader.close();
		return tweetSets;
	}
	
	public static void serialize(HashMap<String, TweetSet> sets) throws IOException {
		FileOutputStream fos = new FileOutputStream("map_train.ser");
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		oos.writeObject(sets);
		fos.close();
		oos.close();
	}
	
	@SuppressWarnings("unchecked")
	public static HashMap<String, TweetSet> unserialize(String filename) throws IOException, ClassNotFoundException {
		
		FileInputStream fis = new FileInputStream(filename);
		ObjectInputStream ois = new ObjectInputStream(fis);
		HashMap<String,TweetSet> ret = (HashMap<String, TweetSet>) ois.readObject();
		fis.close();
		ois.close();
		return ret;
		
	}

	public static void main(String[] args) throws Exception {
		TweetVectorizer.initialize();
		
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
		
		if (argMap.containsKey("-test")) {
			testPath = argMap.get("-test");
			testdata = SC.reader(testPath);
		}

		if (argMap.containsKey("-path")) {
			basePath = argMap.get("-path");
			traindata = SC.reader(basePath);
		}
		
		if (argMap.containsKey("-serialize")) {
			HashMap<String,TweetSet> testTweets = readTweets(basePath);
			serialize(testTweets);
			System.exit(0);
		}
		
		if (argMap.containsKey("-testSerialize")) {
			HashMap<String,TweetSet> testTweets = unserialize("map.ser");
			for (String topic : testTweets.keySet()) {
				TweetSet ts = testTweets.get(topic);
				for (int i = 0; i < ts.size(); ++i) {
					System.out.println(ts.getTweetSet().get(i));
				}
			}
			System.exit(0);
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
			} else if (model.equals("LSTM")) {
				//get data
				HashMap<String,TweetSet> trainingTweets = unserialize("map_train.ser");
				HashMap<String,TweetSet> validationTweets = new HashMap<String,TweetSet>();
				List<String> topics = new ArrayList<String>();
				for (String topic : trainingTweets.keySet())
					topics.add(topic);
				Random rand = new Random(29182072);//Random.org
				for (int i = 0; i < 20; ++i) {
					int topicIndex = rand.nextInt(topics.size());
					String topic = topics.get(topicIndex);
					validationTweets.put(topic, trainingTweets.get(topic));
					trainingTweets.remove(topic);
					topics.remove(topicIndex);
				}
				HashMap<String,TweetSet> testTweets = unserialize("map_test.ser");
				
				System.out.println("trainingTweets.size() => "+trainingTweets.size());
				System.out.println("validationTweets.size(); => "+validationTweets.size());
				System.out.println("testTweets.size() => "+testTweets.size());
				
				System.out.println("finished reading");
				
				Double[] tempVector = TweetVectorizer.vectorize("hello my name is Bob");
				int inputDimension = tempVector.length;
				
//				for (int numLayers = 1; numLayers <= 5; numLayers++) {
//					for (int lstmLayerSize = 2; lstmLayerSize <= 10; lstmLayerSize += 2) {
//						System.out.println("numLayers = "+numLayers+", lstmLayerSize = "+lstmLayerSize);
//						//create and train
//						LstmRntnQuantifier lstm = new LstmRntnQuantifier(numLayers, lstmLayerSize, inputDimension,false);
//						lstm.train(trainingTweets, 120);
//						
//						System.out.println("Average KLD loss on train: "+lstm.KLD(trainingTweets, false));
//						System.out.println("Average KLD loss on validation: "+lstm.KLD(validationTweets, false));
//					}
//				}
				
				int numLayers = 3, lstmLayerSize = 2;
				System.out.println("numLayers = "+numLayers+", lstmLayerSize = "+lstmLayerSize);
				LstmRntnQuantifier lstm = new LstmRntnQuantifier(numLayers, lstmLayerSize, inputDimension,false);
				lstm.train(trainingTweets, 120);
				
				System.out.println("Average KLD loss on train: "+lstm.KLD(trainingTweets, false));
				System.out.println("Average KLD loss on validation: "+lstm.KLD(validationTweets, false));
				System.out.println("Average KLD loss on test: "+lstm.KLD(testTweets, false));
				
				
				System.exit(0);
			}
			
		}
		
		LM.train(traindata);
		System.out.println(LM.predictProbability(testdata));
	}
}
