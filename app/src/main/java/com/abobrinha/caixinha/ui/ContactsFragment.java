package com.abobrinha.caixinha.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.abobrinha.caixinha.R;
import com.abobrinha.caixinha.network.SocialUtils;


public class ContactsFragment extends Fragment {

    public ContactsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_contacts, container, false);
        RecyclerView contactsView = (RecyclerView) rootView.findViewById(R.id.rv_contacs);

        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(),
                getActivity().getResources().getInteger(R.integer.grid_columns));

        contactsView.setLayoutManager(layoutManager);
        contactsView.setNestedScrollingEnabled(false);
        contactsView.setHasFixedSize(true);

        ContactsAdapter adapter = new ContactsAdapter(getActivity());
        contactsView.setAdapter(adapter);
        return rootView;
    }

    private class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ContactsViewHolder> {
        private Context mContext;
        private float mOffset;
        private int lastPosition = -1;

        public ContactsAdapter(Context context) {
            mContext = context;
            mOffset = mContext.getResources().getDimensionPixelSize(R.dimen.grid_animation_offset_y);
        }

        @Override
        public ContactsViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            int layoutIdForListItem = R.layout.fragment_contacts_item;
            LayoutInflater inflater = LayoutInflater.from(mContext);

            View view = inflater.inflate(layoutIdForListItem, viewGroup, false);
            final ContactsViewHolder holder = new ContactsViewHolder(view);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SocialUtils.openExternalLink(mContext, holder.getAdapterPosition(), null);
                }
            });
            return holder;
        }

        @Override
        public void onBindViewHolder(ContactsViewHolder holder, int position) {
            String socialName = SocialUtils.SOCIAL_NAMES[position];
            int imageResId = SocialUtils.SOCIAL_IMAGE_IDS[position];

            holder.socialTitle.setText(socialName);
            holder.socialImage.setImageResource(imageResId);
            holder.socialImage.setContentDescription(socialName);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                animateViewsIn(holder.cardView, position);
            }
        }

        @Override
        public int getItemCount() {
            return SocialUtils.SOCIAL_NAMES.length;
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        private void animateViewsIn(View viewToAnimate, int position) {
            if (position > lastPosition) {
                Interpolator interpolator =
                        AnimationUtils.loadInterpolator(mContext,
                                android.R.interpolator.linear_out_slow_in);

                viewToAnimate.setVisibility(View.VISIBLE);
                viewToAnimate.setTranslationY(mOffset);
                viewToAnimate.setAlpha(0.85f);
                viewToAnimate.animate()
                        .translationY(0f)
                        .alpha(1f)
                        .setInterpolator(interpolator)
                        .setDuration(1000L)
                        .start();

                mOffset *= 1.5f;
                lastPosition = position;
            }
        }

        public class ContactsViewHolder extends RecyclerView.ViewHolder {

            public CardView cardView;
            public ImageView socialImage;
            public TextView socialTitle;

            public ContactsViewHolder(View itemView) {
                super(itemView);
                cardView = (CardView) itemView.findViewById(R.id.card_view);
                socialImage = (ImageView) itemView.findViewById(R.id.thumbnail);
                socialTitle = (TextView) itemView.findViewById(R.id.title_text_view);
            }
        }
    }
}
