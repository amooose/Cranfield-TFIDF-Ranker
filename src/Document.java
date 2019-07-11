
public class Document {
    private String title;
    private int index;
    private String author;
    private String publisher;
    private String writtenContent;

    public Document(int index, String title,  String author, String publisher, String writtenContent) {
        this.title = title;
        this.index = index;
        this.author = author;
        this.publisher = publisher;
        this.writtenContent = writtenContent;

    }

    public String getTitle() {
        return this.title;
    }

    public int getIndex() {
        return this.index;
    }

    public String getAuthor() { return this.author; }

    public String getBook() {
        return this.publisher;
    }

    public String getWrittenContent() {
        return this.writtenContent;
    }

}
