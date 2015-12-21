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
import java.util.Map.Entry;

import main.TweetVectorizer;
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

	public static HashMap<String, TweetSet> readTweets(String filename)
			throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(filename));
		String line = null;
		String topic = null;
		HashMap<String, TweetSet> tweetSets = new HashMap<String, TweetSet>();
		List<LabeledTweet> tweetSet = new ArrayList<LabeledTweet>();
		line = reader.readLine();
		String[] fields = line.split("\t");
		topic = fields[0];
		while (line != null) {
			fields = line.split("\t");
			if (!topic.equalsIgnoreCase(fields[0])) {
				System.out.println("finished with " + topic + " ("
						+ tweetSet.size() + " examples)");
				System.out.println("starting with " + fields[0]);
				tweetSets.put(topic, new TweetSet(tweetSet));
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

	public static void serialize(HashMap<String, TweetSet> sets)
			throws IOException {
		FileOutputStream fos = new FileOutputStream("map.ser");
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		oos.writeObject(sets);
		fos.close();
		oos.close();
	}

	@SuppressWarnings("unchecked")
	public static HashMap<String, TweetSet> unserialize() throws IOException,
			ClassNotFoundException {

		FileInputStream fis = new FileInputStream("map.ser");
		ObjectInputStream ois = new ObjectInputStream(fis);
		HashMap<String, TweetSet> ret = (HashMap<String, TweetSet>) ois
				.readObject();
		fis.close();
		ois.close();
		return ret;

	}

	public static HashMap<String, Pair<List<List<String>>, List<List<String>>>> reformatdata(
			HashMap<Pair<String, String>, List<List<String>>> testdata) {
		HashMap<String, Pair<List<List<String>>, List<List<String>>>> formatteddata = new HashMap<String, Pair<List<List<String>>, List<List<String>>>>();
		for (Entry<Pair<String, String>, List<List<String>>> entry : testdata
				.entrySet()) {
			Pair<String, String> keyPair = entry.getKey();
			String label = keyPair.getSecond();
			String topic = keyPair.getFirst();
			List<List<String>> sentenceList = entry.getValue();
			if (formatteddata.containsKey(topic)) {
				Pair<List<List<String>>, List<List<String>>> valueList = formatteddata
						.get(topic);
				if (label.equals("positive")) {
					valueList.setFirst(sentenceList);
				} else {
					valueList.setSecond(sentenceList);
				}
			} else {
				if (label.equals("positive")) {
					formatteddata.put(topic,
							new Pair<List<List<String>>, List<List<String>>>(
									sentenceList, null));
				} else {
					formatteddata.put(topic,
							new Pair<List<List<String>>, List<List<String>>>(
									null, sentenceList));
				}
			}
		}
		return formatteddata;
	}

	public static void main(String[] args) throws IOException,
			ClassNotFoundException {
		TweetVectorizer.initialize();

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

		if (argMap.containsKey("-test")) {
			testPath = argMap.get("-test");
			testdata = SC.reader(testPath);
		}

		if (argMap.containsKey("-path")) {
			basePath = argMap.get("-path");
			traindata = SC.reader(basePath);
		}

		if (argMap.containsKey("-serialize")) {
			HashMap<String, TweetSet> trainTweets = readTweets(basePath);
			serialize(trainTweets);
			System.exit(0);
		}

		if (argMap.containsKey("-testSerialize")) {
			HashMap<String, TweetSet> testTweets = unserialize();
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
				// get data
				HashMap<String, TweetSet> trainingTweets = readTweets(basePath);
				HashMap<String, TweetSet> testTweets = readTweets(testPath);

				System.out.println("finished reading");

				Double[] tempVector = TweetVectorizer
						.vectorize("hello my name is Bob");

				int numLayers, lstmLayerSize, inputDimension;
				inputDimension = tempVector.length;
				numLayers = 4;
				lstmLayerSize = 25;

				// create and train
				LstmRntnQuantifier lstm = new LstmRntnQuantifier(numLayers,
						lstmLayerSize, inputDimension);
				lstm.train(trainingTweets, 30);

				double totalLoss = 0.0;
				for (String topic : testTweets.keySet()) {
					double currentPrediction = lstm
							.predictProbability(testTweets.get(topic));
					double label = testTweets.get(topic).getpValue();
					double currentKLD = lstm.lossfunction(label,
							currentPrediction);
					System.out.println("For topic \"" + topic
							+ "\", prediction: " + currentPrediction
							+ ", KLD: " + label);
					totalLoss += currentKLD;
				}
				System.out.println("Average KLD loss: "
						+ (totalLoss / testTweets.size()));

				System.exit(0);
			} else if (model.equals("inter")) {
				HashMap<String, TweetSet> trainingTweets = readTweets(basePath);
				HashMap<String, TweetSet> testTweets = readTweets(testPath);
				HashMap<String,Pair<List<List<String>>,List<List<String>>>> testformatteddata = reformatdata(testdata);
				System.out.println("finished reading");

				Double[] tempVector = TweetVectorizer
						.vectorize("hello my name is Bob");

				int numLayers, lstmLayerSize, inputDimension;
				inputDimension = tempVector.length;
				numLayers = 4;
				lstmLayerSize = 25;

				// create and train
				LstmRntnQuantifier lstm = new LstmRntnQuantifier(numLayers,
						lstmLayerSize, inputDimension);
				lstm.train(trainingTweets, 30);
				MaxentTesterModel1 Mxmodel = new MaxentTesterModel1();
				Mxmodel.train(traindata);
				
				double totalLoss = 0.0;
				for (String topic : testTweets.keySet()) {
					double currentPrediction = lstm
							.predictProbability(testTweets.get(topic));
					double label = testTweets.get(topic).getpValue();
					double Maxentq = Mxmodel.getpredictProbability(testformatteddata.get(topic));
					double currentKLD = lstm.lossfunction(label,
							(currentPrediction+Maxentq)/2);
					System.out.println("For topic \"" + topic
							+ "\", prediction: " + currentPrediction
							+ ", KLD: " + label);
					totalLoss += currentKLD;
				}
				System.out.println("Average KLD loss: "
						+ (totalLoss / testTweets.size()));

				System.exit(0);
			}

		}

		LM.train(traindata);
		System.out.println(LM.predictProbability(testdata));
	}
}
