package onitsuma.com.twear.model;

import java.util.Comparator;

/**
 * Created by csuay on 08/04/15.
 */
public class Tuit implements Comparator<Long> {

    private String userName;
    private String text;
    private byte[] image;
    private byte[] userImage;
    private long timestamp;
    private long id;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }


    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public byte[] getUserImage() {
        return userImage;
    }

    public void setUserImage(byte[] userImage) {
        this.userImage = userImage;
    }

    @Override
    public int compare(Long lhs, Long rhs) {
        return (int) (lhs - rhs);
    }
}
