import java.util.Comparator;

public class QueryItem {
    private String word;
    private double tf;
    private double idf;
    private double weight;
    private int docNum;

    public QueryItem() {
        this.word = "";
        this.tf = 0.0;
        this.idf = 0.0;
        this.weight = 0.0;
        this.docNum = 1;
    }

    public String getWord() { return this.word; }

    public int getDocNum() {
        return this.docNum;
    }

    public double getTF() { return this.tf; }

    public double getWeight() { return this.weight; }

    public double getIDF() {
        return this.idf;
    }

    public void setDocNum(int val) {  this.docNum = val; }
    public void setWeight(double val) {  this.weight = val; }
    public void setTF(double val) { this.tf = val; }
    public void setIDF(double val) { this.idf = val;}
    public void setWord(String word) {
         this.word = word;
    }

}

class WeightComparator implements Comparator<QueryItem> {
    @Override
    public int compare(QueryItem word1, QueryItem word2) {
        return Double.compare(word1.getWeight(), word2.getWeight());
    }
}
