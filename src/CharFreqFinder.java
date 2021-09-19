public class CharFreqFinder {

  //constructor//
  public CharFreqFinder(){
    super();
  }
  
  /**
   * search alphabet whose position is the closest to the given alphabet
   * @param n the given alphabet's position
   * @return the closest alphabet 
   */
  private static char alpha(final int n) {
    char alpha =(char)((n + (int)'a') - 1);
    return alpha;
  }

  /**
   * get the position of the given alphabet 
   * @param alphabet
   * @return the position of the english alphabet
   */
  private static int posOfAlphabet(final char alphabet) {
    int position = ((int)alphabet - (int)'a') + 1; 
    return position;
  }

  /**
   * find the most frequent occuring character in a given string, 
   * using algorithm that it is not that accurate
   * @param T the string
   * @return the most frequent occured in a the given string
   */
  public char frequentCharacterFinder(final String T) {
    final String t = T.replaceAll("[^a-zA-Z]", "").toLowerCase();
    final int l = t.length();
    int sum = 0;

    for (final char c : t.toCharArray()) {
      sum += posOfAlphabet(c);
    }
    final int n = (int) Math.round((double) sum / (double) l);
    return alpha(n);
  }
}