package com.lovewall.app.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.gson.*;
import com.lovewall.app.R;
import com.lovewall.app.api.ApiClient;
import com.lovewall.app.model.Comment;
import com.lovewall.app.model.Post;
import com.lovewall.app.utils.*;
import java.util.List;

public class PostDetailActivity extends AppCompatActivity {
    private Prefs prefs;
    private String postId;
    private Post post;
    private LinearLayout commentsContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);
        prefs = new Prefs(this);
        postId = getIntent().getStringExtra("postId");
        commentsContainer = findViewById(R.id.commentsContainer);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        loadPost();
    }

    private void loadPost() {
        new Thread(() -> {
            try {
                String resp = ApiClient.get("/api/posts/" + postId, prefs.getToken());
                JsonObject json = JsonParser.parseString(resp).getAsJsonObject();
                Gson gson = new Gson();
                post = gson.fromJson(json.getAsJsonObject("post"), Post.class);
                JsonArray commentsArr = json.getAsJsonArray("comments");

                runOnUiThread(() -> {
                    ((TextView) findViewById(R.id.tvTitle)).setText(post.title);
                    ((TextView) findViewById(R.id.tvContent)).setText(post.content);
                    ((TextView) findViewById(R.id.tvAuthor)).setText(post.author_name != null ? post.author_name : "匿名用户");
                    ((TextView) findViewById(R.id.tvTime)).setText(TimeUtil.getTimeAgo(post.created_at));

                    // Tags
                    List<String> tags = post.getTagList();
                    TextView tvTags = findViewById(R.id.tvTags);
                    if (!tags.isEmpty()) {
                        tvTags.setVisibility(View.VISIBLE);
                        StringBuilder sb = new StringBuilder();
                        for (String t : tags) sb.append("#").append(t).append("  ");
                        tvTags.setText(sb.toString());
                    }

                    // Like button
                    TextView btnLike = findViewById(R.id.btnLike);
                    btnLike.setText((post.isLiked ? "❤️ " : "🤍 ") + post.likes);
                    btnLike.setOnClickListener(v -> likePost());

                    ((TextView) findViewById(R.id.tvCommentCount)).setText("💬 " + post.comments_count);

                    // Chat button
                    if (post.author_id != null && !post.author_id.equals(prefs.getUserId())) {
                        Button btnChat = findViewById(R.id.btnChat);
                        btnChat.setVisibility(View.VISIBLE);
                        btnChat.setOnClickListener(v -> startChat());
                    }

                    // Comments
                    renderComments(commentsArr);
                });
            } catch (Exception e) {
                runOnUiThread(() -> ToastUtil.show(this, "加载失败"));
            }
        }).start();
    }

    private void renderComments(JsonArray arr) {
        commentsContainer.removeAllViews();
        Gson gson = new Gson();
        for (JsonElement el : arr) {
            Comment c = gson.fromJson(el, Comment.class);
            View item = getLayoutInflater().inflate(R.layout.item_comment, commentsContainer, false);
            ((TextView) item.findViewById(R.id.tvAuthor)).setText(c.author_name);
            ((TextView) item.findViewById(R.id.tvContent)).setText(c.content);
            ((TextView) item.findViewById(R.id.tvTime)).setText(TimeUtil.getTimeAgo(c.created_at));
            TextView tvLike = item.findViewById(R.id.tvLikes);
            tvLike.setText((c.isLiked ? "❤️" : "🤍") + " " + c.likes);
            commentsContainer.addView(item);
        }
        findViewById(R.id.tvNoComments).setVisibility(arr.size() == 0 ? View.VISIBLE : View.GONE);

        // Send comment
        EditText etComment = findViewById(R.id.etComment);
        findViewById(R.id.btnSend).setOnClickListener(v -> {
            String content = etComment.getText().toString().trim();
            if (content.isEmpty()) return;
            sendComment(content, etComment);
        });
    }

    private void sendComment(String content, EditText et) {
        new Thread(() -> {
            try {
                JsonObject body = new JsonObject();
                body.addProperty("content", content);
                ApiClient.post("/api/posts/" + postId + "/comments", body.toString(), prefs.getToken());
                runOnUiThread(() -> {
                    et.setText("");
                    ToastUtil.show(this, "评论成功");
                    loadPost();
                });
            } catch (Exception e) {
                runOnUiThread(() -> ToastUtil.show(this, "评论失败"));
            }
        }).start();
    }

    private void likePost() {
        new Thread(() -> {
            try {
                String resp = ApiClient.post("/api/posts/" + postId + "/like", "{}", prefs.getToken());
                JsonObject json = JsonParser.parseString(resp).getAsJsonObject();
                runOnUiThread(() -> {
                    post.isLiked = json.get("liked").getAsBoolean();
                    post.likes = json.get("likes").getAsInt();
                    TextView btnLike = findViewById(R.id.btnLike);
                    btnLike.setText((post.isLiked ? "❤️ " : "🤍 ") + post.likes);
                });
            } catch (Exception e) {}
        }).start();
    }

    private void startChat() {
        new Thread(() -> {
            try {
                JsonObject body = new JsonObject();
                body.addProperty("userId", post.author_id);
                String resp = ApiClient.post("/api/chat/conversations", body.toString(), prefs.getToken());
                JsonObject json = JsonParser.parseString(resp).getAsJsonObject();
                JsonObject conv = json.getAsJsonObject("conversation");
                String convId = conv.get("id").getAsString();
                runOnUiThread(() -> {
                    Intent intent = new Intent(this, ChatActivity.class);
                    intent.putExtra("conversationId", convId);
                    intent.putExtra("otherName", conv.get("other_name").getAsString());
                    startActivity(intent);
                });
            } catch (Exception e) {
                runOnUiThread(() -> ToastUtil.show(this, "无法发起私信"));
            }
        }).start();
    }
}
