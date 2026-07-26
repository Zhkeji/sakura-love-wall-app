package com.lovewall.app.model;

import java.util.ArrayList;
import java.util.List;

public class Post {
    public String id;
    public String user_id;
    public String title;
    public String content;
    public String images = "[]";
    public int likes;
    public int comments_count;
    public Integer views = 0;
    public String status;
    public int is_anonymous;
    public int is_pinned = 0;
    public String tags = "[]";
    public String category = "confession";
    public String created_at;
    public String author_name;
    public String author_avatar;
    public String author_id;
    public boolean isLiked;
    public boolean isBookmarked;

    public List<String> getImageList() {
        List<String> list = new ArrayList<>();
        try {
            if (images != null && !images.equals("[]")) {
                String cleaned = images.replace("[", "").replace("]", "").replace("\"", "").replace(" ", "");
                if (!cleaned.isEmpty()) {
                    for (String p : cleaned.split(",")) {
                        if (!p.trim().isEmpty()) list.add(p.trim());
                    }
                }
            }
        } catch (Exception e) {}
        return list;
    }

    public List<String> getTagList() {
        List<String> list = new ArrayList<>();
        try {
            if (tags != null && !tags.equals("[]")) {
                String cleaned = tags.replace("[", "").replace("]", "").replace("\"", "").replace(" ", "");
                if (!cleaned.isEmpty()) {
                    for (String p : cleaned.split(",")) {
                        if (!p.trim().isEmpty()) list.add(p.trim());
                    }
                }
            }
        } catch (Exception e) {}
        return list;
    }
}
