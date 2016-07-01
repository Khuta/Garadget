package volpis.com.garadget.models;

public class BottomPanelItem {
    public int getType() {
        return type;
    }

    private String title;
    private int imageId;
    private int type = -1;

    public BottomPanelItem(String text1, int imageId) {
        this.title = text1;
        this.imageId = imageId;
        this.type = 1;
    }

    public BottomPanelItem() {
        this.type = 0;
    }

    public String getTitle() {
        return title;
    }

    public int getImageId() {
        return imageId;
    }
}
