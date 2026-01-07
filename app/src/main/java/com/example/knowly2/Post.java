package com.example.knowly2;

public class Post {
    public String username, time, content, tag1, tag2;

    public int likeCount;
    public int dislikeCount;
    public int commentCount;

    public boolean isSaved;

    public Post(String u, String t, String c, String tg1, String tg2,
                int like, int dislike, int comment) {
        username = u;
        time = t;
        content = c;
        tag1 = tg1;
        tag2 = tg2;

        likeCount = like;
        dislikeCount = dislike;
        commentCount = comment;

        isSaved = false;
    }
}

