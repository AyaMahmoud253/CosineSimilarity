package Ass2;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
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

class InvertedIndex {
    Map<String, DictEntry> index;

    public InvertedIndex() {
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

public class CosineSimilarity {

	public static void main(String[] args) throws IOException {
	    String[] filenames = {"0.txt","1.txt","2.txt","3.txt","4.txt","5.txt","6.txt","7.txt","8.txt","9.txt"};
	    InvertedIndex index = new InvertedIndex();
	    index.buildIndex(filenames);

	    Scanner sc= new Scanner(System.in);
	    System.out.print("Enter a query: ");
	    String query = sc.nextLine();

	    Map<Integer, Double> resultMap = index.cosineSimilarity(query);
	    int k = 3;
	    int[] sortedIndices = getTopK(resultMap, k);
	    for (int i = 0; i < k; i++) {
	        System.out.println((i+1) + ". " + filenames[sortedIndices[i]]);
	    }
	}

    public static int[] getTopK(Map<Integer, Double> scores, int k) {
        int[] sortedIndices = new int[scores.size()];
        int i = 0;
        for (Map.Entry<Integer, Double> entry : scores.entrySet()) {
            sortedIndices[i++] = entry.getKey();
        }
        for (i = 0; i < sortedIndices.length - 1; i++) {
            for (int j = i + 1; j < sortedIndices.length; j++) {
                if (scores.get(sortedIndices[i]) < scores.get(sortedIndices[j])) {
                    int temp = sortedIndices[i];
                    sortedIndices[i] = sortedIndices[j];
                    sortedIndices[j] = temp;
                }
            }
        }
        return sortedIndices.length > k ? Arrays.copyOfRange(sortedIndices, 0, k) : sortedIndices;
    }
}