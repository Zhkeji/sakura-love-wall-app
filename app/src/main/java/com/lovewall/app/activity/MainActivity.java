package com.lovewall.app.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.*;
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
    private String searchKeyword = "";
    private boolean hasMore = true;
    private boolean isLoading = false;

    // 排序按钮
    private TextView tabLatest, tabHot, tabViews, tabComments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        prefs = new Prefs(this);

        recyclerView = findViewById(R.id.recyclerPosts);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        tvEmpty = findViewById(R.id.tvEmpty);
        FloatingActionButton fab = findViewById(R.id.fabCreate);

        // 排序标签
        tabLatest = findViewById(R.id.tabLatest);
        tabHot = findViewById(R.id.tabHot);
        tabViews = findViewById(R.id.tabViews);
        tabComments = findViewById(R.id.tabComments);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PostAdapter(posts, new PostAdapter.OnPostClickListener() {
            @Override
            public void onPostClick(Post post) {
                Intent intent = new Intent(MainActivity.this, PostDetailActivity.class);
                intent.putExtra("postId", post.id);
                startActivity(intent);
            }
            @Override
            public void onLikeClick(Post post, int position) {
                likePost(post, position);
            }
            @Override
            public void onExpandClick(Post post, int position) {
                adapter.toggleExpand(position);
            }
        });
        recyclerView.setAdapter(adapter);

        swipeRefresh.setColorSchemeColors(getResources().getColor(R.color.colorPrimary));
        swipeRefresh.setOnRefreshListener(this::refresh);

        fab.setOnClickListener(v -> startActivity(new Intent(this, CreatePostActivity.class)));

        // 搜索
        EditText etSearch = findViewById(R.id.etSearch);
        findViewById(R.id.btnSearch).setOnClickListener(v -> {
            searchKeyword = etSearch.getText().toString().trim();
            refresh();
        });
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            searchKeyword = etSearch.getText().toString().trim();
            refresh();
            return true;
        });

        // 排序标签点击
        setupSortTabs();

        // 侧边栏按钮
        findViewById(R.id.btnChat).setOnClickListener(v -> startActivity(new Intent(this, ChatListActivity.class)));
        findViewById(R.id.btnProfile).setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));

        refresh();
    }

    private void setupSortTabs() {
        View.OnClickListener tabClick = v -> {
            resetTabColors();
            v.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            ((TextView) v).setTextColor(getResources().getColor(R.color.white));

            int id = v.getId();
            if (id == R.id.tabLatest) currentSort = "latest";
            else if (id == R.id.tabHot) currentSort = "hot";
            else if (id == R.id.tabViews) currentSort = "views";
            else if (id == R.id.tabComments) currentSort = "comments";

            refresh();
        };

        tabLatest.setOnClickListener(tabClick);
        tabHot.setOnClickListener(tabClick);
        tabViews.setOnClickListener(tabClick);
        tabComments.setOnClickListener(tabClick);

        // 默认选中最新
        tabLatest.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        tabLatest.setTextColor(getResources().getColor(R.color.white));
    }

    private void resetTabColors() {
        int bg = getResources().getColor(R.color.white);
        int textColor = getResources().getColor(R.color.gray);
        tabLatest.setBackgroundColor(bg); tabLatest.setTextColor(textColor);
        tabHot.setBackgroundColor(bg); tabHot.setTextColor(textColor);
        tabViews.setBackgroundColor(bg); tabViews.setTextColor(textColor);
        tabComments.setBackgroundColor(bg); tabComments.setTextColor(textColor);
    }

    private void refresh() {
        currentPage = 1;
        hasMore = true;
        posts.clear();
        adapter.notifyDataSetChanged();
        loadPosts();
    }

    private void loadPosts() {
        if (isLoading || !hasMore) return;
        isLoading = true;
        swipeRefresh.setRefreshing(true);

        new Thread(() -> {
            try {
                String url = "/api/posts?page=" + currentPage + "&sort=" + currentSort;
                if (!searchKeyword.isEmpty()) {
                    url += "&search=" + java.net.URLEncoder.encode(searchKeyword, "UTF-8");
                }
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

    private void likePost(Post post, int position) {
        new Thread(() -> {
            try {
                String resp = ApiClient.post("/api/posts/" + post.id + "/like", "{}", prefs.getToken());
                JsonObject json = JsonParser.parseString(resp).getAsJsonObject();
                boolean liked = json.get("liked").getAsBoolean();
                int likes = json.get("likes").getAsInt();
                runOnUiThread(() -> {
                    post.isLiked = liked;
                    post.likes = likes;
                    adapter.notifyItemChanged(position);
                });
            } catch (Exception e) {}
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }
}
