	public class LabeledTweet {
		
		private String tweet;
		private boolean label;
		
		public String getTweet() {
			return tweet;
		}

		public boolean getLabel() {
			return label;
		}

		public LabeledTweet(String tweet2) {
			tweet = tweet2;
			
		}
		
		public LabeledTweet(String tweet2, boolean label) {
			this.tweet = tweet2;
			this.label = label;
		}
	}