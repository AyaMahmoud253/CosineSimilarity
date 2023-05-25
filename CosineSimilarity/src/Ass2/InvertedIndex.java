package Ass2;

import java.io.*;
import java.util.*;

 class DictionaryEntry {
    private String term;
    private PostingList postingList;

    public DictionaryEntry(String term) {
        this.term = term;
        postingList = new PostingList();
    }

    public PostingList getPostingList() {
        return postingList;
    }
}
 class PostingList {
    private List<String> files;
    private Map<String, Integer> termFrequencies;

    public PostingList() {
        files = new ArrayList<>();
        termFrequencies = new HashMap<>();
    }

    public void addFile(String file) {
        if (!files.contains(file)) {
            files.add(file);
        }
        termFrequencies.put(file, termFrequencies.getOrDefault(file, 0) + 1);
    }

    public List<String> getFiles() {
        return files;
    }

    public int getDocumentFrequency() {
        return files.size();
    }

    public double getTermFrequency(String term, String file) {
        return (double) termFrequencies.getOrDefault(file, 0) / (double) files.size();
    }
}
public class InvertedIndex {
    private Map<String, DictionaryEntry> index;

    public InvertedIndex() {
        index = new HashMap<>();
    }
    public class Pair<K, V> {
        private K key;
        private V value;

        public Pair(K key, V value) {
            this.key = key;
            this.value = value;
        }

        public K getKey() {
            return key;
        }

        public void setKey(K key) {
            this.key = key;
        }

        public V getValue() {
            return value;
        }

        public void setValue(V value) {
            this.value = value;
        }
    }

    public void buildIndex(String[] files) {
        for (String file : files) {
            try (Scanner scanner = new Scanner(new File(file))) {
                while (scanner.hasNext()) {
                    String word = scanner.next().toLowerCase();
                    if (!index.containsKey(word)) {
                        index.put(word, new DictionaryEntry(word));
                    }
                    index.get(word).getPostingList().addFile(file);
                }
            } catch (FileNotFoundException e) {
                System.out.println("File not found: " + file);
            }
        }
    }

    public Pair<List<String>, Map<String, Double>> search(String[] query) {
        Map<String, Integer> queryVector = new HashMap<>();
        for (String word : query) {
            word = word.toLowerCase();
            if (queryVector.containsKey(word)) {
                queryVector.put(word, queryVector.get(word) + 1);
            } else {
                queryVector.put(word, 1);
            }
        }

        Map<String,Double> cosineSimilarities = new HashMap<>();
        for (String file : index.values().stream().flatMap(entry -> entry.getPostingList().getFiles().stream()).distinct().toArray(String[]::new)) {
            Map<String, Integer> documentVector = new HashMap<>();
            try (Scanner scanner = new Scanner(new File(file))) {
                while (scanner.hasNext()) {
                    String word = scanner.next().toLowerCase();
                    if (documentVector.containsKey(word)) {
                        documentVector.put(word, documentVector.get(word) + 1);
                    } else {
                        documentVector.put(word, 1);
                    }
                }
            } catch (FileNotFoundException e) {
                System.out.println("File not found: " + file);
            }

            double dotProduct = 0.0;
            double queryMagnitude = 0.0;
            double documentMagnitude = 0.0;

            for (Map.Entry<String, Integer> entry : queryVector.entrySet()) {
                String word = entry.getKey();
                int queryFrequency = entry.getValue();
                int documentFrequency = documentVector.getOrDefault(word, 0);
                dotProduct += queryFrequency * documentFrequency;
                queryMagnitude += queryFrequency * queryFrequency;
            }

            for (Map.Entry<String, Integer> entry : documentVector.entrySet()) {
                int frequency = entry.getValue();
                documentMagnitude += frequency * frequency;
            }

            double cosineSimilarity = dotProduct / (Math.sqrt(queryMagnitude) * Math.sqrt(documentMagnitude));
            cosineSimilarities.put(file, cosineSimilarity);
        }

        List<String> rankedFiles = new ArrayList<>(cosineSimilarities.keySet());
        rankedFiles.sort((file1, file2) -> Double.compare(cosineSimilarities.get(file2), cosineSimilarities.get(file1)));

        return new Pair<>(rankedFiles, cosineSimilarities);
    }
    
    public void tfIdf(String query) {
        String[] terms = query.toLowerCase().split("\\W+");
        int N = 4;
        for (String term : terms) {
            DictionaryEntry entry = index.get(term.toLowerCase());
            if (entry != null) {
                PostingList postingList = entry.getPostingList();
                System.out.println("\n_Tf-Idf_");
                for (String file : postingList.getFiles()) {
                    double tf = calculateTermFrequency(term, file);
                    double idf = calculateInverseDocumentFrequency(term, N);
                    double tfIdf = tf * idf;
                    System.out.println("Between '" + term + "' and file " + file + ": " + tfIdf);
                }
            } else {
                System.out.println("Term not found in any files: " + term);
            }
        }
    }
    private double calculateTermFrequency(String term, String file) {
        double termFrequency = 0;
        double n=0;
        try (Scanner scanner = new Scanner(new File(file))) {
            while (scanner.hasNext()) {
                String word = scanner.next().toLowerCase();
                n++;
                if (term.equals(word)) {
                    termFrequency++;
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + file);
        }
        double TF=termFrequency/n;
        return (double) TF;
    }


    private double calculateInverseDocumentFrequency(String term, int N) {
        DictionaryEntry entry = index.get(term);
        int documentFrequency = (entry != null) ? entry.getPostingList().getDocumentFrequency() : 0;
        return Math.log10((double) N / (double) documentFrequency);
    }
    
    public static void main(String[] args) {
        String[] files = {"0.txt", "1.txt", "2.txt", "3.txt","4.txt", "5.txt","6.txt", "7.txt","8.txt", "9.txt"};
        //String[] files = {"4.txt", "5.txt"};
       // String[] files = {"0.txt", "1.txt", "2.txt", "3.txt"};
        InvertedIndex index = new InvertedIndex();
        index.buildIndex(files);

        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter query: ");
        String userInput = scanner.nextLine();
        String[] query = userInput.split(" ");
        Pair<List<String>, Map<String, Double>> result = index.search(query);
        System.out.println("---------------------------------------------------");
    	System.out.println( "Ranked files &&  CosineSimilarity: ");
        for (String file : result.getKey()) {
            try (Scanner fileScanner = new Scanner(new File(file))) {
                while (fileScanner.hasNextLine()) {
                    String line = fileScanner.nextLine();
                    if (line.toLowerCase().contains(userInput.toLowerCase())) {
                        System.out.println(file +"        &&        "+ String.format("%.3f", result.getValue().get(file))); 
                    	break;
                    }
                }
            } catch (FileNotFoundException e) {
                System.out.println("File not found: " + file);
            }
        }
        /*System.out.println("---------------------------------------------------");
        System.out.println("Ranked files:");
        for (String f : result.getKey()) {
        	
        	System.out.println(f + " CosineSimilarity: " + String.format("%.3f", result.getValue().get(f)));
        }
        
        System.out.println("---------------------------------------------------");*/
        
        index.tfIdf(userInput);
    }
}