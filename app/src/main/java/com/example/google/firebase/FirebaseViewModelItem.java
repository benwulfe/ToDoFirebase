package com.example.google.firebase;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.view.View;
import android.widget.TextView;

import com.example.google.todofirebase.BR;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;

/**
 * Represents a ViewModel for a single Firebase child in a list.
 * You may subclass this Type to provide additional functionality.
 * If you subclass the Type, set the attribute:
 *
 *   app:viewModelClass="@{`package.qualified.classname`}
 *
 * on your RecyclerView so that the FirebaseViewAdapter creates the correct viewmodel instances.
 */
public class FirebaseViewModelItem extends BaseObservable {
    Firebase firebase;
    DataSnapshot dataSnapshot;

    public FirebaseViewModelItem() {
    }

    @Bindable
    public Firebase getFirebase() {
        return firebase;
    }

    public View.OnFocusChangeListener updateOnLostFocus(final String fieldName) {

        return new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (v instanceof TextView) {
                    if (hasFocus) {
                        CharSequence charSequence = ((TextView) v).getText();
                        v.setTag(charSequence != null ? charSequence.toString() : "");
                    } else {
                        CharSequence charSequence = ((TextView) v).getText();
                        String newValue = charSequence != null ? charSequence.toString() : "";
                        String oldValue = v.getTag() != null ? (String) v.getTag() : "";
                        if (!oldValue.equals(newValue)) {
                            onFieldChanged(fieldName, newValue);
                        }
                    }
                } else {
                    //add support for your control here by calling
                    //firebase.child(fieldName).setValue( [newvalue] );
                    assert false;
                }
            }
        };
    }

    protected void onFieldChanged(String fieldName, String newValue) {
        if (firebase != null) {
            firebase.child(fieldName).setValue(newValue);
        }
    }

    @Bindable
    public DataSnapshot getDataSnapshot() {
        return dataSnapshot;
    }

    public void setDataSnapshot(DataSnapshot snapshot) {
        dataSnapshot = snapshot;

        if (firebase == null) {
            firebase = snapshot.getRef();
        }
        notifyPropertyChanged(BR.dataSnapshot);
    }
}
