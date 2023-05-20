package Ass2;

import java.io.*;
import java.util.*;


public class InvertedIndex {
    private Map<String, List<String>> index;

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
                        index.put(word, new ArrayList<>());
                    }
                    if (!index.get(word).contains(file)) {
                        index.get(word).add(file);
                    }
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
            if (queryVector.containsKey(word)){
                queryVector.put(word, queryVector.get(word) + 1);
            } else {
                queryVector.put(word, 1);
            }
        }
       
        Map<String, Double> cosineSimilarities = new HashMap<>();
        for (String file : index.values().stream().flatMap(List::stream).distinct().toArray(String[]::new)) {
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
                documentMagnitude +=frequency * frequency;
            }

            double cosineSimilarity = dotProduct / (Math.sqrt(queryMagnitude) * Math.sqrt(documentMagnitude));
            cosineSimilarities.put(file, cosineSimilarity);
        }

        List<String> rankedFiles = new ArrayList<>(cosineSimilarities.keySet());
        rankedFiles.sort((file1, file2) -> Double.compare(cosineSimilarities.get(file2), cosineSimilarities.get(file1)));

        return new Pair<>(rankedFiles, cosineSimilarities);
    }
    public Map<Pair<String, String>, Double> calculateAllSimilarities() {
        Map<Pair<String, String>, Double> similarities = new HashMap<>();

        // Calculate similarity for each pair of documents
        for (String file1 : index.values().stream().flatMap(List::stream).distinct().toArray(String[]::new)) {
            for (String file2 : index.values().stream().flatMap(List::stream).distinct().toArray(String[]::new)) {
                if (file1.equals(file2)) {
                    continue; // Skip the same document
                }

                // Calculate similarity between file1 and file2
                double similarity = calculateSimilarity(file1, file2);
                similarities.put(new Pair<>(file1, file2), similarity);
            }
        }

        return similarities;
    }

    private double calculateSimilarity(String file1, String file2) {
        Map<String, Integer> document1Vector = getDocumentVector(file1);
        Map<String, Integer> document2Vector = getDocumentVector(file2);

        double dotProduct = 0.0;
        double document1Magnitude = 0.0;
        double document2Magnitude = 0.0;

        // Calculate dot product and magnitudes
        for (Map.Entry<String, Integer> entry : document1Vector.entrySet()) {
            String word = entry.getKey();
            int document1Frequency = entry.getValue();
            int document2Frequency = document2Vector.getOrDefault(word, 0);
            dotProduct += document1Frequency * document2Frequency;
            document1Magnitude += document1Frequency * document1Frequency;
        }

        for (Map.Entry<String, Integer> entry : document2Vector.entrySet()) {
            int frequency = entry.getValue();
            document2Magnitude += frequency * frequency;
        }

        // Calculate cosine similarity
        return dotProduct / (Math.sqrt(document1Magnitude) * Math.sqrt(document2Magnitude));
    }

    private Map<String, Integer> getDocumentVector(String file) {
        Map<String, Integer> documentVector = new HashMap<>();

        try (Scanner scanner = new Scanner(new File(file))) {
            while (scanner.hasNext()) {
                String word = scanner.next().toLowerCase();
                documentVector.put(word, documentVector.getOrDefault(word, 0) + 1);
            }
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + file);
        }

        return documentVector;
    }


    public static void main(String[] args) {
        //String[] files = {"0.txt", "1.txt", "2.txt","3.txt", "4.txt", "5.txt", "6.txt", "7.txt", "8.txt", "9.txt"};
    	String[] files = {"0.txt", "1.txt"};
        InvertedIndex index = new InvertedIndex();
        index.buildIndex(files);
       
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter query: ");
        String userInput = scanner.nextLine();

        String[] query = userInput.split(" ");
     // ...
        Pair<List<String>, Map<String, Double>> result = index.search(query);
        System.out.println("Ranked files:");
        for (String file : result.getKey()) {
            System.out.println(file );
        }

        // Calculate all document similarities
        Map<Pair<String, String>, Double> allSimilarities = index.calculateAllSimilarities();
        System.out.println("All document similarities:");
        for (Map.Entry<Pair<String, String>, Double> entry : allSimilarities.entrySet()) {
            Pair<String, String> documentPair = entry.getKey();
            double similarity = entry.getValue();
            System.out.println(documentPair.getKey() + " and " + documentPair.getValue() + " Similarity: " + similarity);
        }

    }
}