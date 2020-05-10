package txtparsing;

public class MyQuery {
    private String queryID;
    private String queryText;

    //constructor //kataskeuasteis
    public MyQuery(String queryID, String queryText) {
        this.queryID = queryID;
        this.queryText = queryText;
    }

    @Override
    public String toString() {
        String ret = "MyQuery {"
                + "\n\tqueryID: " + queryID
                + "\n\tQuery: " + queryText + "\n}";
        return ret;
    }

    //---- Getters & Setters definition ----
    public String getQueryID() { return queryID; }

    public void setQueryID(String queryID) {
        this.queryID = queryID;
    }

    public String getQueryText() {
        return queryText;
    }

    public void setQueryText(String queryText) {
        this.queryText = queryText;
    }
}
