package Ass2;
import java.util.Comparator;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

class Posting {
    int docId;
    int tf;
    Posting next;

    public Posting(int docId, int tf) {
        this.docId = docId;
        this.tf = tf;
        this.next = null;
    }
}

class DictEntry {
    int docFreq;
    int termFreq;
    Posting pList;

    public DictEntry() {
        this.docFreq = 0;
        this.termFreq = 0;
        this.pList = null;
    }
}

class InvertedIndex1 {
    Map<String, DictEntry> index;

    public InvertedIndex1() {
        this.index = new HashMap<>();
    }

    public void buildIndex(String[] filenames) throws IOException {
        for (int i = 0; i < filenames.length; i++) {
            int docId = i;
            BufferedReader reader = new BufferedReader(new FileReader(filenames[i]));
            String line = null;
            while ((line = reader.readLine()) != null) {
                String[] terms = line.split("\\W+");
                for (String term : terms) {
                    term = term.toLowerCase();
                    if (term.length() == 0)
                        continue;
                    DictEntry entry = index.getOrDefault(term, new DictEntry());
                    entry.termFreq++;
                    Posting curr = entry.pList;
                    Posting prev = null;
                    boolean found = false;
                    while (curr != null) {
                        if (curr.docId == docId) {
                            curr.tf++;
                            found = true;
                            break;
                        }
                        prev = curr;
                        curr = curr.next;
                    }
                    if (!found) {
                        entry.docFreq++;
                        Posting posting = new Posting(docId, 1);
                        if (prev == null) {
                            entry.pList = posting;
                        } else {
                            prev.next = posting;
                        }
                    }
                    index.put(term, entry);
                }
            }
            reader.close();
        }
    }

    public Map<Integer, Double> cosineSimilarity(String query) throws IOException {
        String[] terms = query.split("\\W+");
        Set<String> uniqueTerms = new HashSet<>();
        for (String term : terms) {
            term = term.toLowerCase();
            if (term.length() > 0) {
                uniqueTerms.add(term);
            }
        }
        int numDocs = 10;
        double[] scores = new double[numDocs];
        for (String term : uniqueTerms) {
            if (index.containsKey(term)) {
                DictEntry entry = index.get(term);
                int docFreq = entry.docFreq;
                int termFreq = entry.termFreq;
                double idf = Math.log10((double) numDocs / docFreq);
                Posting p = entry.pList;
                while (p != null) {
                    double tf = 1 + Math.log10((double) p.tf);
                    double wtd = tf * idf;
                    scores[p.docId] += wtd;
                    p = p.next;
                }
            }
        }
        double[] docLengths = new double[numDocs];
        for (int i = 0; i < numDocs; i++) {
            BufferedReader reader = new BufferedReader(new FileReader(i + ".txt"));
            String line = null;
            while ((line = reader.readLine()) != null) {
                String[] termsInDoc = line.split("\\W+");
                for (String term : termsInDoc) {
                    term = term.toLowerCase();
                    if (term.length() == 0)
                        continue;
                    int ttf = index.getOrDefault(term, new DictEntry()).pList == null ? 0 : index.get(term).pList.tf;
                    double tf = 1 + Math.log10((double) ttf);
                    docLengths[i] += Math.pow(tf, 2);
                }
            }
            reader.close();
        }
        for (int i = 0; i < numDocs; i++) {
            if (docLengths[i] > 0) {
                scores[i] /= Math.sqrt(docLengths[i]);
            }
        }
        Map<Integer, Double> resultMap = new HashMap<>();
        for (int i = 0; i < numDocs; i++) {
            resultMap.put(i, scores[i]);
        }
        return resultMap;
    }
}

	public class InvertedIndex {
		public static void main(String[] args) throws IOException {
		    String[] filenames = {"0.txt","1.txt","2.txt","3.txt","4.txt","5.txt","6.txt","7.txt","8.txt","9.txt"};
		    InvertedIndex1 index = new InvertedIndex1();
		    index.buildIndex(filenames);

		    Scanner sc= new Scanner(System.in);
		    System.out.print("Enter a query: ");
		    String query = sc.nextLine();

		    Map<Integer, Double> resultMap = index.cosineSimilarity(query);
	        
	        // Sort the map by value in descending order
	        Map<Integer, Double> sortedMap = sortByValue(resultMap);
	        
		    int k = 3;
		    for (Map.Entry<Integer, Double> entry : sortedMap.entrySet()) {
		        if (k-- == 0) break; 
		        System.out.println((10 - k) + ". " + filenames[entry.getKey()]);
		    }
		}

	    public static Map<Integer, Double> sortByValue(Map<Integer, Double> map) {
	        List<Map.Entry<Integer, Double>> list = new LinkedList<>(map.entrySet());
	        Collections.sort(list, new Comparator<Map.Entry<Integer, Double>>() {
	            public int compare(Map.Entry<Integer, Double> o1, Map.Entry<Integer, Double> o2) {
	                return (o2.getValue()).compareTo(o1.getValue());
	            }
	        });

	       Map<Integer, Double> result = new LinkedHashMap<>();
	        for (Map.Entry<Integer, Double> entry : list) {
	            result.put(entry.getKey(), entry.getValue());
	        }
	        return result;
	    }
	}