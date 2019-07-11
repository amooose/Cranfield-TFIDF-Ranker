// Created by:
// Dylan Ogrin

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.*;


public class tfidfRanker {

    static int CRAN_SIZE = 1;
    static final int docWeightVectorSize = 750;
    static final int indexTableTABLESIZE = 90449;

    private static HashMap<Integer, HashMap<String, Double>> docVectorMap = new HashMap<>(CRAN_SIZE);
    private static HashMap<String, LinkedList<WordItem>> invertedIndex = new HashMap<>(indexTableTABLESIZE);


    // Stems a string via porter-stemming.
    public static String stemWord(String word){
        Stemmer s = new Stemmer();
        for(int i = 0; i<=word.length()-1;i++){
            s.add(word.charAt(i));
        }
        s.stem();
        return s.toString();
    }

    // Take a multi-line entry, sanitize, and build a new string and return it.
    public static String getSanitizedMultiLine(BufferedReader br, String stopAt, LinkedList<String> titleList){
        StringBuilder sb = new StringBuilder();
        StringBuilder title = new StringBuilder();
        try {
            TextIterator readFile = new TextIterator();
            for (String line; (line = br.readLine()) != null && !line.startsWith(stopAt); ) {
                readFile.readTextString(line);
                while (readFile.hasNext()) // Cycle+sanitize through entire text file.
                {
                    String word = readFile.next();

                    sb.append(stemWord(word) + " ");
                    if(stopAt.equals(".A")){
                        title.append(word + " ");
                    }
                }
            }
        }
        catch(Exception e){}
        if(stopAt.equals(".A")) {
            titleList.add(title.toString());
        }
        return sb.toString();
    }


    // Puts the info into a document object, and adds that to the document array.
    public static void addDocument(int index, String title, String author, String publisher, String writtenContent,Document[] docArray){
        Document doc = new Document(index,title,author,publisher,writtenContent);
        docArray[index] = doc;

    }

    // Reads in the cran, and puts it into a a document object.
    public static void processDocLines(String path, Document[] docArray, LinkedList<String> titleList){
        try{
            // Load cran into buffered reader.
            BufferedReader br = new BufferedReader(new FileReader(path));
            int index = 0; String title = ""; String author = ""; String publisher = ""; String writtenContent = "";

            // Read cran line by line
            for(String line; (line = br.readLine()) != null; ) {

                // TITLE + AUTHOR
                if(line.startsWith(".T")) {
                    title = getSanitizedMultiLine(br, ".A", titleList);

                    //buffered reader returns, now sitting at the line
                    // .A, time to read the author.
                    // Read the next line to get the author.
                    line = br.readLine();

                    //Check if author exists
                    if(!line.startsWith(".B")) {
                        author = line;
                    }
                    else{ // no authors!
                        author = "";
                    }
                }

                // PUBLISHERS
                if(line.startsWith(".B")) {
                    // now sitting at the line .B,
                    // time to read the publishers.
                    // Read the next line to get the publishers.
                    line = br.readLine();
                    if(!line.startsWith(".W")) {
                        publisher = line;
                    }
                    else{ // no publishers!
                        publisher = "";
                    }
                }

                // WRITTEN CONTENT
                if(line.startsWith(".W")) {
                    writtenContent = getSanitizedMultiLine(br, ".I", titleList);

                    // Encountered '.I' to know when to stop.
                    // Now we've read a whole document. Submit the document.
                    index++;
                    addDocument(index, title, author, publisher, writtenContent, docArray);
                }
            }
        }
        catch(Exception e) {
            System.out.println("Error! File not found!");
            return;
        }
    }

    // Creates an inverted index for all documents
    public static void createInvertedIndex(Document[] docArray){
        TextIterator readWords = new TextIterator();
        String text = "";
        for(int i = 1; i<CRAN_SIZE; i++){

            // combine title+content into one string
            StringBuilder sb = new StringBuilder();
            sb.append(docArray[i].getTitle() + " ");
            sb.append(docArray[i].getWrittenContent());
            text = sb.toString();
            readWords.readTextString(text);

            while (readWords.hasNext())
            {
                // Grab a word from the doc (pre-stemmed).
                String grabWord = readWords.next();

                // It doesnt exist in the inverted index, add it.
                if (invertedIndex.get(grabWord) == null) {
                    WordItem tempWI = new WordItem(1,i);
                    LinkedList<WordItem> temp = new LinkedList<>();
                    temp.add(tempWI);
                    invertedIndex.put(grabWord, temp);
                }

                // It exists in the inverted index.
                else {
                    // if the word is in the index, but doesn't already
                    // have the document in its list, add the doc.
                    boolean exists = false;
                    for (WordItem tempWord : invertedIndex.get(grabWord)) {
                        if(tempWord.getDocID() == i){
                            tempWord.incCount();
                            exists = true;
                        }
                    }
                    // Word is in inverted index, but docID isnt in its list yet, add it.
                    if(!exists){
                        WordItem tempWI = new WordItem(1,i);
                        invertedIndex.get(grabWord).add(tempWI);
                    }

                }
            }
        }
    }

    public static double log2(double n)
    {
        return (Math.log10(n) / Math.log10(2));
    }

    // return the document freq of a word.
    public static int getDocFreq(String word){
        if(invertedIndex.get(word) == null){
            return 0;
        }
        return invertedIndex.get(word).size();
    }

    // return the term freq of a word in a specific document.
    public static int getTermFreqInDoc(String word, int docID){
        LinkedList<WordItem> temp = invertedIndex.get(word);
        for (WordItem tempWord : temp) {
            if(tempWord.getDocID() == docID){
                return tempWord.getCount();
            }
        }
        return -1;
    }

    // generates tf-idf matrix for all words in all docs
    public static void computeWeights(Document[] docArray) {
        TextIterator s = new TextIterator();
        for(int i =1; i < CRAN_SIZE; i++){
            HashMap<String, Double> docWeightVector = new HashMap<>(docWeightVectorSize);


            StringBuilder sb = new StringBuilder();
            sb.append(docArray[i].getTitle() + " ");
            sb.append(docArray[i].getWrittenContent());
            s.readTextString(sb.toString());

            while(s.hasNext()) {
                String word = s.next();
                double tf = getTermFreqInDoc(word, i);
                double df = getDocFreq(word);
                double idf = log2((double)(CRAN_SIZE-1)/df);
                double weight = tf * idf;
                docWeightVector.put(word,weight);
            }

            docVectorMap.put(i,docWeightVector);
        }

    }

    // return the magnitude of the vector.
    public static double computeMagnitude(HashMap<String, Double> docWeightVector){
        double result = 0.0;
        for (Double value : docWeightVector.values()) {
            result+=Math.pow(value,2);
        }
        return Math.sqrt(result);
    }

    // normalize all weights in docVectorMap to remove document length bias.
    public static void euclidNormalizeDocVector(){
        for(int i = 1; i<CRAN_SIZE;i++){
            HashMap<String, Double> docWeightVector = docVectorMap.get(i);
            double denom = computeMagnitude(docWeightVector);
            for (String key : docWeightVector.keySet()) {
                docWeightVector.put(key,(docWeightVector.get(key)/denom));
            }
            docVectorMap.put(i,docWeightVector);
        }
    }

    // compute query magnitude for similarity calculations
    public static double computeQueryMagnitude(HashMap<String, QueryItem> queryList){
        double result = 0.0;
        for (String key  : queryList.keySet()) {
            result+=Math.pow(queryList.get(key).getWeight(),2);
        }
        return Math.sqrt(result);
    }

    public static void userInputQuery(LinkedList<String> titleList){
        HashMap<String, QueryItem> queryList = new HashMap<>(100);

        TextIterator readWords = new TextIterator();
        Scanner s = new Scanner(System.in);

        System.out.println("Enter a query: ");
        String query = s.nextLine();
        readWords.readTextString(query);

        System.out.println("Enter # of search results to see: ");
        int resultAmount = s.nextInt();
        if(!(resultAmount > 0 && resultAmount < CRAN_SIZE)){
            System.out.println("Invalid amount of results!");
            userInputQuery(titleList);
        }

        double maxFreq = 1.0;
        while(readWords.hasNext()){
            String stemmedQueryWord = stemWord(readWords.next());

            if(queryList.containsKey(stemmedQueryWord)){
                double tf = queryList.get(stemmedQueryWord).getTF();
                queryList.get(stemmedQueryWord).setTF(queryList.get(stemmedQueryWord).getTF()+1);
                if(maxFreq<tf+1){
                    maxFreq=tf+1;
                }
            }
            else{
                // calculate tf and idf for all words in query
                QueryItem temp = new QueryItem();
                temp.setWord(stemmedQueryWord);
                temp.setTF(1);
                double df = getDocFreq(stemmedQueryWord);
                double idf = log2((double)(CRAN_SIZE-1)/df);

                temp.setIDF(idf);
                queryList.put(stemmedQueryWord,temp);
            }

        }

        // generate tf-idf for query
        for (String key  : queryList.keySet()) {
            double tempTF = queryList.get(key).getTF();
            double tempIDF = queryList.get(key).getIDF();
            queryList.get(key).setWeight((tempIDF)*(tempTF/maxFreq));
        }

        // calc query magnitude
        double queryMag = computeQueryMagnitude(queryList);

        // normalize query weights
        for (String key  : queryList.keySet()) {
            if(getDocFreq(key) != 0) {
                queryList.get(key).setWeight(queryList.get(key).getWeight() / queryMag);
            }
            else{
                queryList.get(key).setWeight(0);
            }
        }

        //calculate similarity
        querySimilarity(queryList,resultAmount,titleList);

        System.out.println("New query? y/n");
        Scanner nq = new Scanner(System.in);
        if(nq.next().toLowerCase().equals("y")){
            userInputQuery(titleList);
        }


    }

    public static void querySimilarity(HashMap<String, QueryItem> queryList, int resultAmount, LinkedList<String> titleList){
        QueryItem[] scoreArray = new QueryItem[CRAN_SIZE];
        QueryItem zeroIndex = new QueryItem();
        scoreArray[0] = zeroIndex;

        // calculate similarity between query and every document
        for(int i = 1; i<CRAN_SIZE; i++){
            double simSum = 0.0;
            for (String word  : queryList.keySet()) {
                if(queryList.get(word).getWeight() != 0.0 && docVectorMap.get(i).containsKey(word)){
                    HashMap<String, Double> docWeightVector = docVectorMap.get(i);
                    simSum+= docWeightVector.get(word)*queryList.get(word).getWeight();
                }
            }

            QueryItem tempQ = new QueryItem();
            scoreArray[i] = tempQ;
            scoreArray[i].setWeight(simSum);
            scoreArray[i].setDocNum(i);
        }

        // sort by weight (score) descending, higher score = better match
        WeightComparator wc = new WeightComparator();
        Arrays.sort(scoreArray, wc);

        // print 'resultAmount' number of relevant documents
        int j = 1;
        for(int i = CRAN_SIZE-1; i>(CRAN_SIZE-1)-resultAmount; i--) {
            System.out.println("RESULT #"+(j++));
            System.out.println("Index["+scoreArray[i].getDocNum()+
                    "] Document Title: "+titleList.get(scoreArray[i].getDocNum()-1)+
                    "\n Score: "+scoreArray[i].getWeight());
            System.out.println();
        }

    }


    public static void main(String[] args)  {

        if(args.length >= 2) {
            System.out.println("Indexing...");
            CRAN_SIZE = Integer.parseInt(args[1])+1;
            Document[] docArray = new Document[CRAN_SIZE];
            LinkedList<String> titleList = new LinkedList<String>();
            processDocLines(args[0],docArray,titleList);
            createInvertedIndex(docArray);
            computeWeights(docArray);
            euclidNormalizeDocVector();
            userInputQuery(titleList);
        }
        else{
            System.out.println("ERROR: Provide the cran filepath in the arguments and its size.");
            System.out.println("Example: <path> <size>");
        }
    }
}
