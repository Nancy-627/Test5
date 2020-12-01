package test_5;

import org.litepal.crud.DataSupport;


public class Memo extends DataSupport {

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public Integer getImageId() {
        return imageId;
    }

    public void setImageId(Integer imageId) {
        this.imageId = imageId;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public boolean isTipsChecked() {
        return isTipsChecked;
    }

    public void setTipsChecked(boolean tipsChecked) {
        isTipsChecked = tipsChecked;
    }

    private Integer id;

    private String title;

    private String text;

    private String createTime;

    private boolean isTipsChecked;

    private Integer imageId;

}
