package classes.example.drawingame.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.view.ViewManager;
import android.widget.CheckBox;
import android.widget.TextView;

import classes.example.drawingame.activities.DrawingActivity;
import classes.example.drawingame.R;

/**
 * Shows about_app
 */

public class InstructionDialog extends DialogFragment {

    private DrawingActivity drawingActivity;
    private View view;
    private TextView textView;
    private CheckBox checkBox;
    private boolean isCalledOnStart;
    private String instruction;

    public static InstructionDialog create(boolean isCalledOnStart, String instruction) {
        InstructionDialog instructionDialog = new InstructionDialog();
        instructionDialog.isCalledOnStart = isCalledOnStart;
        instructionDialog.instruction = instruction;
        return instructionDialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle bundle) {
        drawingActivity = (DrawingActivity) getActivity();
        view = drawingActivity.getLayoutInflater().inflate(R.layout.dialog_instruction, null);

        textView = (TextView) view.findViewById(R.id.textViewInstructionDialog);
        textView.setText(instruction);
        checkBox = (CheckBox) view.findViewById(R.id.checkBoxInstructionDialog);
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(drawingActivity);
        if (!isCalledOnStart)
            ((ViewManager) checkBox.getParent()).removeView(checkBox);

        AlertDialog.Builder builder = new AlertDialog.Builder(drawingActivity)
                .setView(view)
                .setPositiveButton(getString(R.string.ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                if (checkBox.isChecked())
                                    sharedPreferences.edit().putBoolean(drawingActivity.showInstruction, false).apply();
                                dialog.dismiss();
                                if (isCalledOnStart)
                                    drawingActivity.showDialogFragment(new InitialDialog());
                            }
                        }
                );
        return builder.create();
    }
}

