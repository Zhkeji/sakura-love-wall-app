package com.lovewall.app.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lovewall.app.R;
import com.lovewall.app.adapter.PostAdapter;
import com.lovewall.app.api.ApiClient;
import com.lovewall.app.model.Post;
import com.lovewall.app.utils.Prefs;
import com.lovewall.app.utils.ToastUtil;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private PostAdapter adapter;
    private SwipeRefreshLayout swipeRefresh;
    private TextView tvEmpty;
    private List<Post> posts = new ArrayList<>();
    private Prefs prefs;
    private int currentPage = 1;
    private String currentSort = "latest";
    private boolean hasMore = true;
    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        prefs = new Prefs(this);

        recyclerView = findViewById(R.id.recyclerPosts);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        tvEmpty = findViewById(R.id.tvEmpty);
        FloatingActionButton fab = findViewById(R.id.fabCreate);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PostAdapter(posts, post -> {
            Intent intent = new Intent(this, PostDetailActivity.class);
            intent.putExtra("postId", post.id);
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        swipeRefresh.setColorSchemeColors(getResources().getColor(R.color.colorPrimary));
        swipeRefresh.setOnRefreshListener(this::refresh);

        fab.setOnClickListener(v -> startActivity(new Intent(this, CreatePostActivity.class)));

        findViewById(R.id.btnChat).setOnClickListener(v -> startActivity(new Intent(this, ChatListActivity.class)));
        findViewById(R.id.btnProfile).setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));

        setupSortTabs();
        refresh();
    }

    private void setupSortTabs() {
        android.widget.LinearLayout tabs = findViewById(R.id.sortTabs);
        String[] sorts = {"最新", "最热", "评论"};
        String[] sortKeys = {"latest", "hot", "comments"};
        for (int i = 0; i < sorts.length; i++) {
            TextView tv = new TextView(this);
            tv.setText(sorts[i]);
            tv.setTextSize(13);
            tv.setPadding(32, 16, 32, 16);
            tv.setBackgroundResource(R.drawable.bg_tag);
            tv.setTextColor(getResources().getColor(i == 0 ? R.color.colorPrimary : R.color.gray));
            final int idx = i;
            tv.setOnClickListener(v -> {
                currentSort = sortKeys[idx];
                for (int j = 0; j < tabs.getChildCount(); j++) {
                    ((TextView) tabs.getChildAt(j)).setTextColor(getResources().getColor(j == idx ? R.color.colorPrimary : R.color.gray));
                }
                refresh();
            });
            tabs.addView(tv);
        }
    }

    private void refresh() {
        currentPage = 1;
        hasMore = true;
        posts.clear();
        loadPosts();
    }

    private void loadPosts() {
        if (isLoading || !hasMore) return;
        isLoading = true;
        swipeRefresh.setRefreshing(true);

        new Thread(() -> {
            try {
                String url = "/api/posts?page=" + currentPage + "&sort=" + currentSort;
                String resp = ApiClient.get(url, prefs.getToken());
                JsonObject json = JsonParser.parseString(resp).getAsJsonObject();
                JsonArray arr = json.getAsJsonArray("posts");
                JsonObject pag = json.getAsJsonObject("pagination");

                List<Post> newPosts = new ArrayList<>();
                Gson gson = new Gson();
                for (JsonElement el : arr) {
                    Post p = gson.fromJson(el, Post.class);
                    newPosts.add(p);
                }

                boolean more = currentPage < pag.get("totalPages").getAsInt();

                runOnUiThread(() -> {
                    posts.addAll(newPosts);
                    adapter.notifyDataSetChanged();
                    hasMore = more;
                    currentPage++;
                    tvEmpty.setVisibility(posts.isEmpty() ? View.VISIBLE : View.GONE);
                    recyclerView.setVisibility(posts.isEmpty() ? View.GONE : View.VISIBLE);
                    swipeRefresh.setRefreshing(false);
                    isLoading = false;
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    swipeRefresh.setRefreshing(false);
                    isLoading = false;
                    if (posts.isEmpty()) ToastUtil.show(this, "加载失败");
                });
            }
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }
}
