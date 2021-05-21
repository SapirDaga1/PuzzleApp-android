package il.org.puzzeling;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class SingleChoiceDialog extends DialogFragment {

    int position=0; //default selected position



    public interface  SingleChoiceListener{
        void onPositiveButtonClicked(String[]list, int position);
    }

    SingleChoiceListener singleChoiceListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        singleChoiceListener=(SingleChoiceListener)context;
        try{
            singleChoiceListener=(SingleChoiceListener)context;
        }catch (Exception e){
           throw new ClassCastException(getActivity().toString()+"SingleChoiceListener must implemented");
        }

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder= new AlertDialog.Builder(getActivity());
        String[] list=getActivity().getResources().getStringArray(R.array.levels);
        builder.setTitle(R.string.level_hint).setSingleChoiceItems(list, position, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
position=1;
            }
        }).setPositiveButton(R.string.select_btn, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
singleChoiceListener.onPositiveButtonClicked(list,position);
            }
        });
        return builder.create();
    }

}


