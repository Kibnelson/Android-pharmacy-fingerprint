package mkumbusha.android.main;

import mkumbusha.android.main.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

public class FingernameUsed extends Activity {
    // Return Intent extra
    public static String RET_FINGER_NAME = "user_name";
    //
	private String mFingername = null;
	private Button mButtonOK;
	private Button mButtonCancel;
	private Spinner mSpinnerFinger;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
        setContentView(R.layout.fingername);
        // Set result CANCELED in case the user backs out
        setResult(Activity.RESULT_CANCELED);

        mButtonOK = (Button) findViewById(R.id.buttonOK);
        mButtonCancel = (Button) findViewById(R.id.buttonCancel);
        
        mSpinnerFinger = (Spinner) findViewById(R.id.spinnerFinger);    
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this, R.array.fingers_array, android.R.layout.simple_spinner_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerFinger.setAdapter(adapter2);
        mSpinnerFinger.setSelection(0);
        
        mFingername = null;
        mButtonOK.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Create the result Intent and include the MAC address
            	mFingername = mSpinnerFinger.getItemAtPosition(mSpinnerFinger.getSelectedItemPosition()).toString();
                Intent intent = new Intent();
                intent.putExtra(RET_FINGER_NAME, mFingername);
                // Set result and finish this Activity
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });
        
        mButtonCancel.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	mFingername = null;
            	finish();
            }
        });
	}
}
