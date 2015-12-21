package nlp.util;

import java.util.List;

public class LabelTweet {
	public String label;
	public List<String> tweets;

	public LabelTweet(String label, List<String> tweets) {
		this.label = label;
		this.tweets = tweets;
	}

	public String getlabel() {
		return label;
	}

	public List<String> gettweets() {
		return tweets;
	}
	
}
