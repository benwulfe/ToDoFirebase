package com.example.google.firebase;

import android.databinding.BindingAdapter;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.example.google.todofirebase.BR;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.FirebaseException;
import com.firebase.client.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * This Adapter updates a RecyclerView based on Firebase changes.
 * It can be bound to a RecyclerView by using custom Android binding attributes:
 *         app:firebaseQuery="@{firebaseQuery}"  <== this is a Firebase Query object
 *         app:itemlayoutid="@{@layout/item_view}" <== this is a layout that will hold a child
 *
 * The item's layout should have a Binding Variable named "currentItem"  As in:
 *     <data>
 *      <variable
 *       name="currentItem"
 *       type="com.example.google.firebase.FirebaseViewModelItem"/>
 *     </data>
 *
 * After declaring the variable, you can use regular Firebase methods to display data from it's
 * snapshot property, which will always contain the latest data.  For example, this sets the
 * value of the "message" child in the EditText's text property:
 *             android:text="@{currentItem.dataSnapshot.child(`message`).getValue()}"
 *
 * You can also trigger updates back to Firebase on LostFocus in a single line placed on the EditText:
 *             app:onFocusChangeListener="@{currentItem.updateOnLostFocus(`message`)}"
 * This updates the firebase "message" child with the EditText's text on lostfocus if it has changed.
 */
public class FirebaseViewAdapter extends RecyclerView.Adapter<FirebaseViewAdapter.ViewHolderImpl> {

    private final Query firebaseQuery;
    private final int layoutId;
    private final Class<?> viewModelClass;
    private LayoutInflater inflater;
    private ArrayList<String> childKeys = new ArrayList<>();
    private Map<String, Object> childItems = new HashMap<>();

    public FirebaseViewAdapter(Query query, int layoutId, final Class<?> viewModelClass) {
        this.firebaseQuery = query;
        this.layoutId = layoutId;
        this.viewModelClass = viewModelClass;

        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                int index = childKeys.size();
                childKeys.add(index, dataSnapshot.getKey());

                Object viewModelItem = null;
                if (FirebaseViewModelItem.class.isAssignableFrom(
                        FirebaseViewAdapter.this.viewModelClass)) {
                    try {
                        viewModelItem = FirebaseViewAdapter.this.viewModelClass.newInstance();
                    } catch (InstantiationException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }

                if (viewModelItem == null) {
                    try {
                        viewModelItem = dataSnapshot.getValue(FirebaseViewAdapter.this.viewModelClass);
                    } catch (FirebaseException ex) {
                        ex.printStackTrace();
                    }
                }

                if (viewModelItem instanceof FirebaseViewModelItem) {
                    ((FirebaseViewModelItem) viewModelItem).setDataSnapshot(dataSnapshot);
                }
                if (viewModelItem != null) {
                    childItems.put(dataSnapshot.getKey(), viewModelItem);
                    FirebaseViewAdapter.this.notifyItemInserted(index);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Object modelItem = childItems.get(dataSnapshot.getKey());
                if (modelItem instanceof FirebaseViewModelItem) {
                    ((FirebaseViewModelItem) modelItem).setDataSnapshot(dataSnapshot);
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                childItems.remove(dataSnapshot.getKey());

                for (int index = 0; index < childKeys.size(); index++) {
                    if (childKeys.get(index).equals(dataSnapshot.getKey())) {
                        childKeys.remove(index);
                        FirebaseViewAdapter.this.notifyItemRemoved(index);
                        break;
                    }
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });
    }

    @BindingAdapter({"firebaseQuery", "itemlayoutid", "viewModelClass"})
    public static void setFirebaseQuery(RecyclerView view, Query firebaseQuery, int itemlayoutid,
                                      String viewModelClassName) {
        try {
            FirebaseViewAdapter adapter = new FirebaseViewAdapter(firebaseQuery, itemlayoutid,
                    Class.forName(viewModelClassName));
            view.setAdapter(adapter);

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @BindingAdapter({"firebaseQuery", "itemlayoutid"})
    public static void setFirebaseQuery(RecyclerView view, Query firebaseQuery, int itemlayoutid) {
        FirebaseViewAdapter adapter = new FirebaseViewAdapter(firebaseQuery, itemlayoutid,
                FirebaseViewModelItem.class);
        view.setAdapter(adapter);
    }

    @BindingAdapter({"firebaseQuery", "itemlayoutid"})
    public static void setFirebaseQuery(RecyclerView view, String firebaseQuery, int itemlayoutid) {
        FirebaseViewAdapter adapter = new FirebaseViewAdapter(new Firebase(firebaseQuery), itemlayoutid,
                FirebaseViewModelItem.class);
        view.setAdapter(adapter);
    }

    @Override
    public ViewHolderImpl onCreateViewHolder(ViewGroup viewGroup, int i) {
        if (inflater == null) {
            inflater = LayoutInflater.from(viewGroup.getContext());
        }

        ViewDataBinding binding = DataBindingUtil.inflate(inflater, layoutId, viewGroup, false);
        return new ViewHolderImpl(binding);
    }

    @Override
    public void onBindViewHolder(ViewHolderImpl viewHolder, int i) {
        Object item;
        if (i < childKeys.size()) {
            String key = childKeys.get(i);
            item = childItems.get(key);
        }
        else {
            item = new FirebaseViewModelItem();
            ((FirebaseViewModelItem)item).setFirebase(firebaseQuery.getRef());
        }

        viewHolder.binding.setVariable(BR.currentItem, item);
        viewHolder.binding.executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return childKeys.size() + 1;
    }

    public static class ViewHolderImpl extends RecyclerView.ViewHolder {
        final ViewDataBinding binding;

        ViewHolderImpl(ViewDataBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
