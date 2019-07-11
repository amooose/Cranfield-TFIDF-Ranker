
public class WordItem implements Comparable<WordItem> {
    //private String word;
    private int docID;
    private int count;

    public WordItem() {
        this.count = 0;
        this.docID = 0;
    }

    public WordItem( int count, int docID) {
        this.count = count;
        this.docID = docID;
    }


    public int getCount() {
        return this.count;
    }

    public int getDocID() {
        return this.docID;
    }

    public void setCount(int var1) {
        this.count = var1;
    }

    public void incCount() {
        ++this.count;
    }

    public int compareTo(WordItem var1) {
        return this.count < var1.getCount()?-1:(this.count > var1.getCount()?1:0);
    }
}
