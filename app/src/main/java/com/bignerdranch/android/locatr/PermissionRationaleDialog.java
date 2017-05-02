package com.bignerdranch.android.locatr;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;

/**
 * Created by Chris on 5/2/2017.
 */

public class PermissionRationaleDialog extends DialogFragment {
    private String[] mPermissions;
    private int mRequestCode;

    public static PermissionRationaleDialog newInstance(String[] permissions, int requestCode) {
        
        Bundle args = new Bundle();
        args.putStringArray(LocatrFragment.PERMISSION_ARG, permissions);
        args.putInt(LocatrFragment.REQUEST_CODE_INT, requestCode);
        
        PermissionRationaleDialog fragment = new PermissionRationaleDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();

        mPermissions = args.getStringArray(LocatrFragment.PERMISSION_ARG);
        mRequestCode = args.getInt(LocatrFragment.REQUEST_CODE_INT);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        return new AlertDialog.Builder(getActivity())
                .setMessage(getString(R.string.permission_rationale))
                .setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
//                      // Send to Fragment, and not host activity
                        getTargetFragment().requestPermissions(mPermissions, mRequestCode);
                    }
                })
                .create();

    }
}
