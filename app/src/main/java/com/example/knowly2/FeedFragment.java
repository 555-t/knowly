package com.example.knowly2;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class FeedFragment extends Fragment {
    private static final String ARG_TYPE = "type";

    public static FeedFragment newInstance(String type) {
        FeedFragment f = new FeedFragment();
        Bundle b = new Bundle();
        b.putString(ARG_TYPE, type);
        f.setArguments(b);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_feed, container, false);
        RecyclerView rv = v.findViewById(R.id.rvFeed);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        String type = getArguments() != null ? getArguments().getString(ARG_TYPE) : "for_you";

        List<Post> posts = new ArrayList<>();
        if ("following".equals(type)) {
            posts.add(new Post("alex_chen", "5h ago",
                    "Key tips for acing your calculus exam:\n1. Master the fundamentals\n2. Practice daily\n3. Understand, don't memorize\n4. Use visual aids\n5. Form study groups 📚",
                    "Mathematics", "",
                    256, 8, 0));

        } else {
            posts.add(new Post("sarah_smith", "3h ago",
                    "Just learned about quantum entanglement! Mind-blowing how particles can be connected across vast distances. 🚀",
                    "Physics", "Science",
                    142, 3, 1));

            posts.add(new Post("john_doe", "10h ago",
                    "The French Revolution wasn't just about politics - it reshaped European culture, art, and philosophy forever...",
                    "History", "Philosophy",
                    203, 12, 0));

        }

        rv.setAdapter(new PostAdapter(requireContext(), posts));
        return v;
    }
}
