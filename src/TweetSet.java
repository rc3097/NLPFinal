import java.util.Iterator;
import java.util.List;

public class TweetSet implements Iterable<LabeledTweet>{
		private List<LabeledTweet> tweetSet;
		private Double pValue;
		public TweetSet(List<LabeledTweet> tweets) {
			this.tweetSet = tweets;
			Double numPos = 0.0;
			for (LabeledTweet tweet : this.tweetSet) {
				if (tweet.getLabel()) {
					numPos += 1.0;
				}
			}
			this.pValue = numPos / this.tweetSet.size();
		}
		public List<LabeledTweet> getTweetSet() {
			return tweetSet;
		}
		public Double getpValue() {
			return pValue;
		}
		public int size() {
			return this.tweetSet.size();
		}
		@Override
		public Iterator<LabeledTweet> iterator() {
			return this.tweetSet.iterator();
		}
		
}