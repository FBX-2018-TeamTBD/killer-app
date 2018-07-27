package com.example.cassandrakane.goalz.models;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;
import java.util.List;


@ParseClassName("Image")
public class Image extends ParseObject{

    public Image() {
        super();
    }

    public Image(ParseFile image, String caption, ParseUser user) {
        super();
        setImage(image);
        setCaption(caption);
        setUser(user);
    }

    public ParseFile getImage() {
        return getParseFile("image");
    }

    public String getCaption() {
        return getString("caption");
    }

    public List<ParseUser> getViewdBy() { return getList("viewedBy"); }

    public void setImage(ParseFile image) {
        put("image", image);
    }

    public void setCaption(String caption) { put("caption", caption); }

    public void setViewedBy(List<ParseUser> viewedBy) { put("viewedBy", viewedBy); }

    public void setUser(ParseUser user) {
        put("user", user);
    }
}
