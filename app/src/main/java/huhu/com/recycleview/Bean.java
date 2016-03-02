package huhu.com.recycleview;

/**
 * Created by Huhu on 3/1/16.
 * 存储对象的Java Bean
 */
public class Bean {
    //图片网址
    private String url;

    //图片对应说明

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    private String text;
}
