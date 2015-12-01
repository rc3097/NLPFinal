package nlp.langmodel;

import java.util.HashMap;
import java.util.List;

import nlp.util.LabelTweet;

/**
 * Language models assign probabilities to sentences and generate sentences.
 */
public abstract class LanguageModel {
	public void train(HashMap<String, List<LabelTweet>> traindata) {
		
	}
	public double predictProbability(HashMap<String, List<LabelTweet>> traindata) {
		return 0;
	}
	
}
