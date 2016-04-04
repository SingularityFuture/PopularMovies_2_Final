package michael.popularmoviestest;

/**
 * Created by Michael on 4/3/2016.
 */
/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new DetailFragment())
                    .commit();
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class DetailFragment extends Fragment {
        private ArrayList<String> mMovieStr;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

            // The detail Activity called via intent.  Inspect the intent for movie data.
            Intent intent = getActivity().getIntent();
            if (intent != null && intent.hasExtra("movies_strings")) {
                mMovieStr = intent.getStringArrayListExtra("movies_strings");
                Picasso.with(getActivity()).load(mMovieStr.get(0)).into((ImageView) rootView.findViewById(R.id.detail_imageview));
                ((TextView) rootView.findViewById(R.id.detail_title))
                        .setText(mMovieStr.get(1));
                ((TextView) rootView.findViewById(R.id.detail_overview))
                        .setText(mMovieStr.get(2));
                ((TextView) rootView.findViewById(R.id.detail_voter_average))
                        .setText("Voter Average: " + mMovieStr.get(3));
                ((TextView) rootView.findViewById(R.id.detail_release_date))
                        .setText("Release Date: " + mMovieStr.get(4));
            }
            return rootView;
        }
    }
}