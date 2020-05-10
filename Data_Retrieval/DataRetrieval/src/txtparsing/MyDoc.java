package txtparsing;

//Αποτυπώνει την δομή τον κειμένων της συλλογής IR2020(documents.txt),που βρήσκετε στο φάκελο docs
public class MyDoc {

    private String docID;
    private String title;
    private String text;

    //constructor
    public MyDoc(String docID, String title, String text) {
        this.docID = docID;
        this.title = title;
        this.text = text;
    }

    @Override
    public String toString() {
        String ret = "MyDoc{"
                + "\n\tDocID: " + docID
                + "\n\tTitle: " + title
                + "\n\tText: " + text;
        return ret + "\n}";
    }

    //---- Getters & Setters definition ----

    public String getDocID() {
        return docID;
    }

    public void setDocID(String docID) {
        this.docID = docID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
