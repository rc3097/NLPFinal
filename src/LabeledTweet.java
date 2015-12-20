import java.io.Serializable;

import main.TweetVectorizer;

public class LabeledTweet implements Serializable {
		/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
		Double Vector[];
		private String tweet;
		private boolean label;
		
		public String getTweet() {
			return tweet;
		}

		public boolean isPositive() {
			return label;
		}

		public LabeledTweet(String tweet2) {
			tweet = tweet2;
			
		}
		
		public Double[] getVector() {
			return Vector;
		}

		public LabeledTweet(String tweet2, boolean label) {
			this.tweet = tweet2;
			this.Vector = TweetVectorizer.vectorize(this.tweet);
			this.label = label;
		}
	}