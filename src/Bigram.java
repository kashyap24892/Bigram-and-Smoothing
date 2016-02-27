import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Bigram {
	private static HashMap<String, Double> unigram_count = new HashMap<>();
	private static File CORPUS = new File("NLPCorpusTreebank2Parts.txt");

	public static void main(String[] args) throws IOException {

		String str1 = "The president has relinquished his control of the company's board.";
		String str2 = "The chief executive officer said the last year revenue was good.";

		str1 = str1.replaceAll("[.!?@,`]", "");
		str2 = str2.replaceAll("[.!?@,`]", "");

		getUnigramCount();
		getBigramTable(str1, true);
		getBigramTable(str2, true);

		getTotalProbability(getBigramProb(str1), str1, "Total bigram probability for sentence:");
		getTotalProbability(getBigramProb(str2), str2, "Total bigram probability for sentence:");

		getTotalProbability(getAddOneSmoothingProbTable(str1), str1, "Total bigram probability with add one soothing for sentence:");
		getTotalProbability(getAddOneSmoothingProbTable(str2), str2, "Total bigram probability with add one soothing for sentence:");

		getTotalProbability(getGoodTuringTable(str1), str1, "Total bigram probability with good turing discounting for sentence:");
		getTotalProbability(getGoodTuringTable(str2), str2, "Total bigram probability with good turing discounting for sentence:");
	}

	private static void getTotalProbability(double[][] table, String str1, String str2) {
		double total = 1;
		System.out.println(str2);
		System.out.println(str1);
		for(int i = 0; i < table.length - 1; i++) {
			for(int j = i+1; j <= i+1; j++) {
				total *= (double)table[i][j];
			}
		}
		System.out.println((double)total);
		System.out.println("--------------------------------------------------------------------------------------------------");
	}

	public static void getUnigramCount() throws IOException{
		String line;
		String[] lines;
		BufferedReader br = new BufferedReader(new FileReader(CORPUS));
		while((line = br.readLine())!=null){
			line = line.toLowerCase();
			lines = line.split(" ");
			for (int i = 0; i < lines.length; i++) {
				if (!unigram_count.containsKey(lines[i])) 
					unigram_count.put(lines[i], (double)1);
				else 
					unigram_count.put(lines[i], unigram_count.get(lines[i]) + 1);					
			}
		}
		br.close();		
	}

	public static int getBigramCount(String word1, String word2) throws IOException {		
		String regex = word1.toLowerCase() + "\\s+" + word2.toLowerCase();
		BufferedReader br = new BufferedReader(new FileReader(CORPUS));
		int count = 0;
		String line;
		while((line=br.readLine())!=null) {
			Pattern pattern = Pattern.compile(regex);
			Matcher  matcher = pattern.matcher(line);
			while(matcher.find()) {
				count++;
			}
		}
		br.close();
		return count;
	}

	public static double[][] getBigramTable(String sent, boolean print) throws IOException{
		if(print) System.out.println("Bigram table for sentence: " + sent + " is::");
		sent = sent.toLowerCase();
		String[] words = sent.split(" ");
		double[][] table = new double[words.length][words.length];
		for (int i = 0; i < words.length; i++) {
			for (int j = 0; j < words.length; j++) {
				table[i][j] = getBigramCount(words[i],words[j]);
				if(print)
					System.out.print(table[i][j] + "\t");
			}
			if(print) System.out.println();
		}
		if(print) System.out.println("--------------------------------------------------------------------------------------------------");
		return table;
	}

	public static double[][] getBigramProb(String sent) throws IOException{
		System.out.println("Bigram probability table for sentence: " + sent + " is::");
		sent = sent.toLowerCase();
		String[] words = sent.split(" ");
		double[][] prob_table = new double[words.length][words.length];
		for (int i = 0; i < words.length; i++) {
			for (int j = 0; j < words.length; j++) {
				if(unigram_count.containsKey(words[i]))
					prob_table[i][j] = (double)getBigramCount(words[i],words[j])/unigram_count.get(words[i]);
				else
					prob_table[i][j] = 0;
				System.out.print(new DecimalFormat("#.00000").format(prob_table[i][j]) + "\t");
			}
			System.out.println();
		}
		System.out.println("--------------------------------------------------------------------------------------------------");
		return prob_table;
	}

	public static double[][] getAddOneSmoothingProbTable(String sent) throws IOException{
		System.out.println("Add One Smoothing table for sentence: " + sent + " is::");
		sent = sent.toLowerCase();
		String[] words = sent.split(" ");
		double[][] addOneProb_table = new double[words.length][words.length];
		for (int i = 0; i < words.length; i++) {
			for (int j = 0; j < words.length; j++) {
				if(unigram_count.containsKey(words[i])) 
					addOneProb_table[i][j] = (double)(getBigramCount(words[i], words[j]) + 1 ) / (unigram_count.get(words[i]) + unigram_count.size());
				else 
					addOneProb_table[i][j] = (double)(getBigramCount(words[i], words[j]) + 1 ) / unigram_count.size();

				System.out.print(new DecimalFormat("#.00000").format(addOneProb_table[i][j]) + " ");
			}
			System.out.println();
		}		
		System.out.println("--------------------------------------------------------------------------------------------------");
		return addOneProb_table;
	}

	public static double[][] getGoodTuringTable(String sent) throws IOException{
		System.out.println("Good Turing Discounting table for sentence: " + sent + " is::");
		double [][] b = getBigramTable(sent, false);
		double totalWords = getTotalWords();
		Collection<Double> c = unigram_count.values();
		double [][] goodTuring_table = new double[b.length][b.length];
		for(int i = 0; i < b.length; i++) {
			for(int j = 0; j < b.length; j++) {
				double Nc1 = Collections.frequency(c, b[i][j] + 1);
				double Nc = Collections.frequency(c, b[i][j]);
				if(Nc == 0) {
					double count_zero = Collections.frequency(c, 1);
					goodTuring_table[i][j] = count_zero / totalWords;
				}
				else {
					double count_star = (b[i][j] + 1) * (Nc1 / Nc);
					goodTuring_table[i][j] = count_star / totalWords;
				}
				System.out.print(new DecimalFormat("#.00000").format(goodTuring_table[i][j]) + " ");
			}
			System.out.println();
		}
		System.out.println("--------------------------------------------------------------------------------------------------");
		return goodTuring_table;
	}

	private static int getTotalWords() throws IOException {
		int count = 0;
		BufferedReader br = new BufferedReader(new FileReader(CORPUS));
		String line;
		while((line = br.readLine())!=null){
			line = line.replaceAll("[.!?@,`]", "");
			String [] s = line.split(" "); 
			for (int i = 0; i < s.length; i++) {
				if (!s[i].isEmpty()){
					count++;
				}   
			}           
		}
		br.close();
		return count;
	}
}