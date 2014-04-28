package mkumbusha.android.main;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;

import mobihealth.android.data.Patienttem;
import mobihealth.android.util.Base64;
import mobihealth.android.view.CustomDialog;
import mobihealth.android.view.CustomProgressDialog;
import mobihealth.android.view.ProfileUpdateDialog;
import mobihealth.android.view.ResponseDialog;
import mkumbusha.android.main.R;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.MonthDisplayHelper;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.futronictech.SDKHelper.FTR_PROGRESS;
import com.futronictech.SDKHelper.FtrIdentifyRecord;
import com.futronictech.SDKHelper.FtrIdentifyResult;
import com.futronictech.SDKHelper.FutronicEnrollment;
import com.futronictech.SDKHelper.FutronicException;
import com.futronictech.SDKHelper.FutronicIdentification;
import com.futronictech.SDKHelper.FutronicSdkBase;
import com.futronictech.SDKHelper.FutronicVerification;
import com.futronictech.SDKHelper.IEnrollmentCallBack;
import com.futronictech.SDKHelper.IIdentificationCallBack;
import com.futronictech.SDKHelper.IVerificationCallBack;
import com.futronictech.SDKHelper.UsbDeviceDataExchangeImpl;
import com.futronictech.SDKHelper.VersionCompatible;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class FingerprintScannerActivity extends Activity implements
		IEnrollmentCallBack, IVerificationCallBack, IIdentificationCallBack {
	/** Called when the activity is first created. */
	private Button mButtonEnroll;
	private Button mButtonVerify;
	private Button mButtonIdentify;
	private Button mButtonStop;
	private Button mButtonExit;
	private CheckBox mChkboxLFD;
	private CheckBox mChkboxDisableMIOT;
	private TextView mTxtMessage;
	private ImageView mFingerImage;
	private Spinner mSpinnerFARN;
	private Spinner mSpinnerMaxFrames;
	private Spinner mSpinnerSdkVersion;
	private Bundle extras;
	byte[] fingerbyte;
	String m[][];
	Vector<FingerprintRecord> patients;
	Vector<DbRecord> patt;

	public static final int MESSAGE_SHOW_MSG = 1;
	public static final int MESSAGE_SHOW_IMAGE = 2;
	public static final int MESSAGE_ENROLL_FINGER = 3;
	public static final int MESSAGE_ENABLE_CONTROLS = 4;

	// Intent request codes
	private static final int REQUEST_INPUT_USERNAME = 1;
	private static final int REQUEST_SELECT_USERNAME = 2;

	// Pending operations
	private static final int OPERATION_ENROLL = 1;
	private static final int OPERATION_IDENTIFY = 2;
	private static final int OPERATION_VERIFY = 3;

	private static Bitmap mBitmapFP = null;
	/**
	 * A database directory name.
	 */
	public static String m_DbDir;

	private String mStrFingerName = null;
	/**
	 * Contain reference for current operation object
	 */
	private FutronicSdkBase m_Operation;

	/**
	 * The type of this parameter is depending from current operation. For
	 * enrollment operation this is DbRecord.
	 */
	private Object m_OperationObj;

	private int mPendingOperation = 0;

	private UsbDeviceDataExchangeImpl usb_host_ctx = null;

	SharedPreferences app_preference;
	private TextView spinnerMaxLayout;
	private Spinner spinnerMaxFrames;
	private MobiHealth mobiHealth;
	private Button proceed;
	private Button helpButton;
	private Button home_button;
	private Button proceedtwo;
	private ArrayList<Patienttem> homeItemList;
	private Button proceedthree;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fingerprintentry);

		mobiHealth = (MobiHealth) getApplication();
		mobiHealth.setCurrentActivity(this);
		extras = getIntent().getExtras();
		app_preference = PreferenceManager.getDefaultSharedPreferences(this);

		mButtonEnroll = (Button) findViewById(R.id.buttonEnroll);
		mButtonVerify = (Button) findViewById(R.id.buttonVerify);
		mButtonIdentify = (Button) findViewById(R.id.buttonIdentify);
		mButtonStop = (Button) findViewById(R.id.buttonStop);
		mButtonExit = (Button) findViewById(R.id.buttonExit);
		proceed = (Button) findViewById(R.id.proceed);
		proceedtwo = (Button) findViewById(R.id.proceedtwo);
		proceedthree = (Button) findViewById(R.id.proceedthree);

		// spinnerMaxLayout =(TextView) findViewById(R.id.spinnerMaxLayout);
		// spinnerMaxLayout.setVisibility(View.INVISIBLE);

		// mButtonStop.setVisibility(View.INVISIBLE);
		// mButtonExit.setVisibility(View.INVISIBLE);
		proceed.setVisibility(View.INVISIBLE);
		proceedtwo.setVisibility(View.INVISIBLE);
		proceedthree.setVisibility(View.INVISIBLE);
		helpButton = (Button) findViewById(R.id.help);

		helpButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				StopOperation();
			}
		});

		mobiHealth.setVerfication(false);

		home_button = (Button) findViewById(R.id.home_button);

		home_button.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				openHome();
			}
		});
		if (extras.getInt("action") == 0) {
			mButtonVerify.setVisibility(View.INVISIBLE);
			mButtonIdentify.setVisibility(View.INVISIBLE);
			proceedthree.setVisibility(View.VISIBLE);

		} else if (extras.getInt("action") == 1) {
			mButtonEnroll.setVisibility(View.INVISIBLE);
			mButtonIdentify.setVisibility(View.INVISIBLE);
			proceedtwo.setVisibility(View.VISIBLE);
			proceed.setVisibility(View.INVISIBLE);
		} else if (extras.getInt("action") == 2) {
			mButtonEnroll.setVisibility(View.INVISIBLE);
			mButtonVerify.setVisibility(View.INVISIBLE);
			proceedtwo.setVisibility(View.INVISIBLE);
			proceed.setVisibility(View.VISIBLE);
		}

		mChkboxLFD = (CheckBox) findViewById(R.id.cbLFD);
		mChkboxDisableMIOT = (CheckBox) findViewById(R.id.cbDisableMIOT);

		mTxtMessage = (TextView) findViewById(R.id.txtMessage);
		mFingerImage = (ImageView) findViewById(R.id.imageFinger);

		mSpinnerMaxFrames = (Spinner) findViewById(R.id.spinnerMaxFrames);
		ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(
				this, R.array.maxframes_array,
				android.R.layout.simple_spinner_item);
		adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSpinnerMaxFrames.setAdapter(adapter1);
		mSpinnerMaxFrames.setSelection(2); // Max Frames = 5;
		mSpinnerMaxFrames.setVisibility(View.INVISIBLE);

		mSpinnerFARN = (Spinner) findViewById(R.id.spinnerFARN);
		ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(
				this, R.array.farn_array, android.R.layout.simple_spinner_item);
		adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSpinnerFARN.setAdapter(adapter2);
		mSpinnerFARN.setSelection(2); // FARN = 245;

		mSpinnerSdkVersion = (Spinner) findViewById(R.id.spinnerSdkVersion);
		ArrayAdapter<CharSequence> adapter3 = ArrayAdapter.createFromResource(
				this, R.array.sdkversion_array,
				android.R.layout.simple_spinner_item);
		adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSpinnerSdkVersion.setAdapter(adapter3);
		mSpinnerSdkVersion.setSelection(2); // Current version

		usb_host_ctx = new UsbDeviceDataExchangeImpl(this, mHandler);

		mButtonEnroll.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {

				try {
					// StartEnroll();
					if (usb_host_ctx.OpenDevice(0, true)) {
						StartEnroll();

					} else {
						if (usb_host_ctx.IsPendingOpen()) {
							mPendingOperation = OPERATION_ENROLL;
						} else {
							mobiHealth
									.setNextScreen(MobiHealthConstants.DONOTHING);
							mobiHealth.setCurrentDialogTitle("Response");
							mobiHealth
									.setCurrentDialogMsg("Cannot start enrollment operation.\nCan't open scanner device");
							mobiHealth
									.showDialog(Main.DIALOG_SERVICE_RESPONSE_ID);
							mobiHealth
									.setDialogType(Main.DIALOG_SERVICE_RESPONSE_ID);
							// mTxtMessage
							// .setText("Cannot start enrollment operation.\nCan't open scanner device");
						}
					}
				} catch (Exception e) {

					mobiHealth.setNextScreen(MobiHealthConstants.DONOTHING);
					mobiHealth.setCurrentDialogTitle("Response");
					mobiHealth
							.setCurrentDialogMsg("Sorry there is no fingerprint device connected");
					mobiHealth.showDialog(Main.DIALOG_SERVICE_RESPONSE_ID);
					mobiHealth.setDialogType(Main.DIALOG_SERVICE_RESPONSE_ID);
				}
			}
		});

		mButtonVerify.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				try {
					if (usb_host_ctx.OpenDevice(0, true)) {
						StartVerify();
					} else {
						if (usb_host_ctx.IsPendingOpen()) {
							mPendingOperation = OPERATION_VERIFY;
						} else {
							mobiHealth
									.setNextScreen(MobiHealthConstants.DONOTHING);
							mobiHealth.setCurrentDialogTitle("Response");
							mobiHealth
									.setCurrentDialogMsg("Cannot start enrollment operation.\nCan't open scanner device");
							mobiHealth
									.showDialog(Main.DIALOG_SERVICE_RESPONSE_ID);
							mobiHealth
									.setDialogType(Main.DIALOG_SERVICE_RESPONSE_ID);
							//
							// mTxtMessage
							// .setText("Can not start verify operation.\nCan't open scanner device");
						}
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

		mButtonIdentify.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				try {
					if (usb_host_ctx.OpenDevice(0, true)) {
						LoadAllFingerprints();
					} else {
						if (usb_host_ctx.IsPendingOpen()) {
							mPendingOperation = OPERATION_IDENTIFY;
						} else {
							mobiHealth
									.setNextScreen(MobiHealthConstants.DONOTHING);
							mobiHealth.setCurrentDialogTitle("Response");
							mobiHealth
									.setCurrentDialogMsg("Cannot start enrollment operation.\nCan't open scanner device");
							mobiHealth
									.showDialog(Main.DIALOG_SERVICE_RESPONSE_ID);
							mobiHealth
									.setDialogType(Main.DIALOG_SERVICE_RESPONSE_ID);
							//
							// mTxtMessage
							// .setText("Cannot start identify operation.\nCan't open scanner device");
						}
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

		mButtonStop.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				StopOperation();
			}
		});

		homeItemList = new ArrayList<Patienttem>();

		mButtonExit.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				ExitActivity();
			}
		});
		proceed.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (mobiHealth.getVerfication()) {
					mobiHealth.setVerfication(false);

					// Toast.makeText(FingerprintScannerActivity.this,
					// mobiHealth.getSelectPosition()+">>>"+mobiHealth.getSearchList(),
					// Toast.LENGTH_LONG)
					// .show();
					openDashboard();

				} else {
					mobiHealth.setNextScreen(MobiHealthConstants.DONOTHING);
					mobiHealth.setCurrentDialogTitle("Response");
					mobiHealth
							.setCurrentDialogMsg("No Patient has been successfully indentified/verified");
					mobiHealth.showDialog(Main.DIALOG_SERVICE_RESPONSE_ID);
					mobiHealth.setDialogType(Main.DIALOG_SERVICE_RESPONSE_ID);

				}
			}
		});
		proceedtwo.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (mobiHealth.getVerfication()) {
					mobiHealth.setVerfication(false);
					openDashboard();

				} else {
					mobiHealth.setNextScreen(MobiHealthConstants.DONOTHING);
					mobiHealth.setCurrentDialogTitle("Response");
					mobiHealth
							.setCurrentDialogMsg("No Patient has been successfully indentified/verified");
					mobiHealth.showDialog(Main.DIALOG_SERVICE_RESPONSE_ID);
					mobiHealth.setDialogType(Main.DIALOG_SERVICE_RESPONSE_ID);

				}
			}
		});

		proceedthree.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (mobiHealth.getFingerPrintData() != null) {
					if (m_Operation != null) {
						m_Operation.Dispose();
					}
					finish();

				} else {
					mobiHealth.setNextScreen(MobiHealthConstants.DONOTHING);
					mobiHealth.setCurrentDialogTitle("Response");
					mobiHealth
							.setCurrentDialogMsg("Patient Finger Print has not been captured");
					mobiHealth.showDialog(Main.DIALOG_SERVICE_RESPONSE_ID);
					mobiHealth.setDialogType(Main.DIALOG_SERVICE_RESPONSE_ID);

				}
			}
		});

	}

	public void openDashboard() {

		try {
			Intent iE = new Intent(this, MobieHealthDemographicsFormEntry.class);
			startActivity(iE);
		} catch (Exception e) {

			Toast.makeText(FingerprintScannerActivity.this,
					"Sorry. No patient(s) found." + e.getMessage(),
					Toast.LENGTH_LONG).show();
		}
	}

	public void openHome() {

		Intent i = new Intent(this, MkumbushaHomePage.class);
		i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(i);
	}

	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_SHOW_MSG:
				String showMsg = (String) msg.obj;
				mTxtMessage.setText(showMsg);
				break;
			case MESSAGE_SHOW_IMAGE:
				mFingerImage.setImageBitmap(mBitmapFP);
				break;
			case MESSAGE_ENROLL_FINGER:
				StartEnrollWithUsername(mStrFingerName);
				break;
			case MESSAGE_ENABLE_CONTROLS:
				EnableControls(true);
				break;

			case UsbDeviceDataExchangeImpl.MESSAGE_ALLOW_DEVICE: {
				if (usb_host_ctx.ValidateContext()) {
					switch (mPendingOperation) {
					case OPERATION_ENROLL:
						StartEnroll();
						break;

					case OPERATION_IDENTIFY:
						LoadAllFingerprints();// StartVerify()
						break;

					case OPERATION_VERIFY:
						StartVerify();
						break;
					}
				} else {
					mTxtMessage.setText("Can't open scanner device");
				}

				break;
			}

			case UsbDeviceDataExchangeImpl.MESSAGE_DENY_DEVICE: {
				mTxtMessage.setText("User deny scanner device");
				break;
			}

			}
		}
	};

	static private String GetDatabaseDir() throws AppException {
		String szDbDir;
		File extStorageDirectory = Environment.getExternalStorageDirectory();
		File Dir = new File(extStorageDirectory, "Android//FtrSdkDb");
		if (Dir.exists()) {
			if (!Dir.isDirectory())
				throw new AppException("Can not create database directory "
						+ Dir.getAbsolutePath()
						+ ". File with the same name already exist.");
		} else {
			try {
				Dir.mkdirs();
			} catch (SecurityException e) {
				throw new AppException("Can not create database directory "
						+ Dir.getAbsolutePath() + ". Access denied.");
			}
		}
		szDbDir = Dir.getAbsolutePath();
		return szDbDir;
	}

	/**
	 * The "Put your finger on the scanner" event.
	 * 
	 * @param Progress
	 *            the current progress data structure.
	 */
	public void OnPutOn(FTR_PROGRESS Progress) {
		mHandler.obtainMessage(MESSAGE_SHOW_MSG, -1, -1,
				"Put finger into device, please ...").sendToTarget();
	}

	/**
	 * The "Take off your finger from the scanner" event.
	 * 
	 * @param Progress
	 *            the current progress data structure.
	 */
	public void OnTakeOff(FTR_PROGRESS Progress) {
		mHandler.obtainMessage(MESSAGE_SHOW_MSG, -1, -1,
				"Take off finger from device, please ...").sendToTarget();
	}

	/**
	 * The "Show the current fingerprint image" event.
	 * 
	 * @param Bitmap
	 *            the instance of Bitmap class with fingerprint image.
	 */
	public void UpdateScreenImage(Bitmap Image) {
		mBitmapFP = Image;
		mHandler.obtainMessage(MESSAGE_SHOW_IMAGE).sendToTarget();
	}

	/**
	 * The "Fake finger detected" event.
	 * 
	 * @param Progress
	 *            the fingerprint image.
	 * 
	 * @return <code>true</code> if the current indetntification operation
	 *         should be aborted, otherwise is <code>false</code>
	 */
	public boolean OnFakeSource(FTR_PROGRESS Progress) {
		mHandler.obtainMessage(MESSAGE_SHOW_MSG, -1, -1, "Fake source detected")
				.sendToTarget();
		return false;
		// if want to cancel, return true
	}

	// //////////////////////////////////////////////////////////////////
	// ICallBack interface implementation
	// //////////////////////////////////////////////////////////////////

	/**
	 * The "Enrollment operation complete" event.
	 * 
	 * @param bSuccess
	 *            <code>true</code> if the operation succeeds, otherwise is
	 *            <code>false</code>.
	 * @param The
	 *            Futronic SDK return code (see FTRAPI.h).
	 */
	public void OnEnrollmentComplete(boolean bSuccess, int nResult) {
		if (bSuccess) {

	
			mobiHealth.setFingerPrintData(Base64
					.encodeBytes(((FutronicEnrollment) m_Operation)
							.getTemplate()));
			
			mobiHealth.setFingerPrintDataFinger(((DbRecord) m_OperationObj)
					.getUserName());
			

			mHandler.obtainMessage(
					MESSAGE_SHOW_MSG,
					-1,
					-1,
					"Finger print data captured successfully. Quality: "
							+ ((FutronicEnrollment) m_Operation).getQuality()
							+ "\n Tap Continue to proceed").sendToTarget();

		} else {

			mobiHealth.setNextScreen(MobiHealthConstants.DONOTHING);
			mobiHealth.setCurrentDialogTitle("Response");
			mobiHealth
					.setCurrentDialogMsg("Enrollment failed. Error description");
			mobiHealth.showDialog(Main.DIALOG_SERVICE_RESPONSE_ID);
			mobiHealth.setDialogType(Main.DIALOG_SERVICE_RESPONSE_ID);
		}

		// check();
	}

	public void check() {
		mobiHealth.setNextScreen(MobiHealthConstants.DONOTHING);
		mobiHealth.setCurrentDialogTitle("Response");
		mobiHealth
				.setCurrentDialogMsg("Cannot start enrollment operation.\nCan't open scanner device");
		mobiHealth.showDialog(Main.DIALOG_SERVICE_RESPONSE_ID);
		mobiHealth.setDialogType(Main.DIALOG_SERVICE_RESPONSE_ID);
	}

	/**
	 * The "Verification operation complete" event.
	 * 
	 * @param bSuccess
	 *            <code>true</code> if the operation succeeds, otherwise is
	 *            <code>false</code>
	 * @param nResult
	 *            the Futronic SDK return code.
	 * @param bVerificationSuccess
	 *            if the operation succeeds (bSuccess is <code>true</code>),
	 *            this parameters shows the verification operation result.
	 *            <code>true</code> if the captured from the attached scanner
	 *            template is matched, otherwise is <code>false</code>.
	 */
	public void OnVerificationComplete(boolean bSuccess, int nResult,
			boolean bVerificationSuccess) {
		StringBuffer szResult = new StringBuffer();
		if (bSuccess) {
			if (bVerificationSuccess) {
				szResult.append("Verification is successful.");
				szResult.append("Patient Name: ");

				szResult.append(mobiHealth.getPname());
				mobiHealth.setVerfication(true);
			} else
				szResult.append("Verification failed.");
		} else {
			szResult.append("Verification failed.");
			szResult.append("Error description: ");
			szResult.append(FutronicSdkBase.SdkRetCode2Message(nResult));
		}
		mHandler.obtainMessage(MESSAGE_SHOW_MSG, -1, -1, szResult.toString())
				.sendToTarget();
		m_Operation = null;
		m_OperationObj = null;

		usb_host_ctx.CloseDevice();
		mPendingOperation = 0;

		mHandler.obtainMessage(MESSAGE_ENABLE_CONTROLS).sendToTarget();

	}

	/**
	 * The "Get base template operation complete" event.
	 * 
	 * @param bSuccess
	 *            <code>true</code> if the operation succeeds, otherwise is
	 *            <code>false</code>.
	 * @param nResult
	 *            The Futronic SDK return code.
	 */
	public void OnGetBaseTemplateComplete(boolean bSuccess, int nResult) {
		StringBuffer szMessage = new StringBuffer();
		if (bSuccess) {
			mHandler.obtainMessage(MESSAGE_SHOW_MSG, -1, -1,
					"Starting identification...").sendToTarget();

			Vector<DbRecord> vpatients = patt;
			FtrIdentifyRecord[] rgRecords = new FtrIdentifyRecord[vpatients
					.size()];
			for (int iPatients = 0; iPatients < vpatients.size(); iPatients++)
				rgRecords[iPatients] = vpatients.get(iPatients)
						.getFtrIdentifyRecord();

			FtrIdentifyResult result = new FtrIdentifyResult();
			nResult = ((FutronicIdentification) m_Operation).Identification(
					rgRecords, result);

			if (nResult == FutronicSdkBase.RETCODE_OK) {
				szMessage.append("Identification complete. Patient: ");
				if (result.m_Index != -1) {
					szMessage.append(patients.get(result.m_Index).getName());
					szMessage.append(" Tap Continue to Proceed");
					mobiHealth.setVerfication(true);
					mobiHealth.setSelectPosition(result.m_Index);

				} else
					szMessage.append("not found");
			} else {
				szMessage.append("Identification failed.");
				szMessage.append(FutronicSdkBase.SdkRetCode2Message(nResult));
			}

			mHandler.obtainMessage(MESSAGE_SHOW_MSG, -1, -1,
					"Fingerprint Captured Tap Continue").sendToTarget();

		} else {
			szMessage.append("Cannot retrieve base template.");
			szMessage.append("Error description: ");
			szMessage.append(FutronicSdkBase.SdkRetCode2Message(nResult));
		}
		mHandler.obtainMessage(MESSAGE_SHOW_MSG, -1, -1, szMessage.toString())
				.sendToTarget();
		m_Operation = null;

		m_OperationObj = null;
		usb_host_ctx.CloseDevice();
		mPendingOperation = 0;
		mHandler.obtainMessage(MESSAGE_ENABLE_CONTROLS).sendToTarget();

		//
	}

	/*
	 * Start enrollment. First get user name, then check user name, finally call
	 * SDK enrollment.
	 */
	private void StartEnroll() {
		// Get user name
		// Intent usernameIntent = new Intent(this, FingernameUsed.class);
		// startActivityForResult(usernameIntent, REQUEST_INPUT_USERNAME);
		//

		CheckFingerName("Right Thumb");
	}

	// enrollment - check user name
	private void CheckFingerName(String szFingerName) {
		if (szFingerName == null || szFingerName.length() == 0) {
			mTxtMessage.setText("You must finger to use.");
			return;
		}
		mStrFingerName = szFingerName;
		// send message to start enrollment
		mHandler.obtainMessage(MESSAGE_ENROLL_FINGER).sendToTarget();
	}

	// enrollment - start enrollment
	private void StartEnrollWithUsername(String szFingerName) {
		try {
			if (!usb_host_ctx.ValidateContext()) {
				throw new Exception("Can't open USB device");
			}

			// CreateFile( szFingerName );

			m_OperationObj = new DbRecord();
			((DbRecord) m_OperationObj).setUserName(szFingerName);

			m_Operation = new FutronicEnrollment((Object) usb_host_ctx);
			// Set control properties
			m_Operation.setFakeDetection(mChkboxLFD.isChecked());
			m_Operation.setFFDControl(true);
			// m_Operation.setFARN( Integer.parseInt(
			// (String)mSpinnerFARN.getSelectedItem()) );
			((FutronicEnrollment) m_Operation)
					.setMIOTControlOff(mChkboxDisableMIOT.isChecked());
			((FutronicEnrollment) m_Operation).setMaxModels(Integer
					.parseInt((String) mSpinnerMaxFrames.getSelectedItem()));

			switch (mSpinnerSdkVersion.getSelectedItemPosition()) {
			case 0:
				m_Operation.setVersion(VersionCompatible.ftr_version_previous);
				break;

			case 1:
				m_Operation.setVersion(VersionCompatible.ftr_version_current);
				break;

			default:
				m_Operation
						.setVersion(VersionCompatible.ftr_version_compatible);
				break;
			}
			EnableControls(false);
			// start enrollment process
			((FutronicEnrollment) m_Operation).Enrollment(this);

		} catch (Exception e) {
			mTxtMessage
					.setText("Cannot start enrollment operation.\nError description: "
							+ e.getMessage());
			m_Operation = null;
			m_OperationObj = null;
			usb_host_ctx.CloseDevice();
		}
	}

	/*
	 * Start verify. - first select user, then call SDK Verification
	 */
	private void StartVerify() {
		// select user
		StartVerifyWithUserName();
	}

	// verify - start verification with selected user
	private void StartVerifyWithUserName() {
		try {
			fingerbyte = Base64.decode(mobiHealth.getScan()); // extras.getString("fingerString").getBytes("UTF-8");

			if (!usb_host_ctx.ValidateContext()) {
				throw new Exception("Can't open USB device");
			}
			// Toast.makeText(FingerprintScannerActivity.this, "1<<",
			// Toast.LENGTH_SHORT).show();
			m_Operation = new FutronicVerification(fingerbyte, usb_host_ctx);
			// Toast.makeText(FingerprintScannerActivity.this, "2<<",
			// Toast.LENGTH_SHORT).show();
			// Set control properties
			m_Operation.setFakeDetection(mChkboxLFD.isChecked());
			m_Operation.setFFDControl(true);
			m_Operation.setFARN(Integer.parseInt((String) mSpinnerFARN
					.getSelectedItem()));
			switch (mSpinnerSdkVersion.getSelectedItemPosition()) {
			case 0:
				m_Operation.setVersion(VersionCompatible.ftr_version_previous);
				break;

			case 1:
				m_Operation.setVersion(VersionCompatible.ftr_version_current);
				break;

			default:
				m_Operation
						.setVersion(VersionCompatible.ftr_version_compatible);
				break;
			}
			EnableControls(false);
			// start verification process
			((FutronicVerification) m_Operation).Verification(this);
		} catch (Exception e) {
			usb_host_ctx.CloseDevice();
			mHandler.obtainMessage(MESSAGE_SHOW_MSG, -1, -1, e.getMessage())
					.sendToTarget();
		}
	}

	/*
	 * Start identify
	 */
	private void StartIdentify() {
		// System.out.println("NN "+pat.size());
		// LoadAllFingerprints();
		// Toast.makeText(FingerprintScannerActivity.this, patients.size()+"<<",
		// Toast.LENGTH_LONG).show();
		if (patt.size() == 0) {
			mTxtMessage
					.setText("Patients not found. Please, run enrollment process first.");
			return;
		}
		m_OperationObj = patt;

		try {
			if (!usb_host_ctx.ValidateContext()) {
				throw new Exception("Can't open USB device");
			}

			m_Operation = new FutronicIdentification(usb_host_ctx);
			// Set control properties
			m_Operation.setFakeDetection(mChkboxLFD.isChecked());
			m_Operation.setFFDControl(true);
			m_Operation.setFARN(Integer.parseInt((String) mSpinnerFARN
					.getSelectedItem()));

			switch (mSpinnerSdkVersion.getSelectedItemPosition()) {
			case 0:
				m_Operation.setVersion(VersionCompatible.ftr_version_previous);
				break;

			case 1:
				m_Operation.setVersion(VersionCompatible.ftr_version_current);
				break;

			default:
				m_Operation
						.setVersion(VersionCompatible.ftr_version_compatible);
				break;
			}

			EnableControls(false);
			// start verification process
			((FutronicIdentification) m_Operation).GetBaseTemplate(this);

		} catch (FutronicException e) {
			mTxtMessage
					.setText("Cannot start identification operation.\nError description: "
							+ e.getMessage());
			usb_host_ctx.CloseDevice();
			m_Operation = null;
			m_OperationObj = null;
		} catch (Exception e) {
			usb_host_ctx.CloseDevice();
			mHandler.obtainMessage(MESSAGE_SHOW_MSG, -1, -1, e.getMessage())
					.sendToTarget();
		}
	}

	/*
	 * Stop button pressed
	 */
	private void StopOperation() {
		if (m_Operation != null) {
			m_Operation.Dispose();
		}
		finish();
	}

	/*
	 * Exit button pressed
	 */
	private void ExitActivity() {
		if (m_Operation != null) {
			m_Operation.Dispose();
		}
		finish();
	}

	/*
	 * try to create file before enrollment.
	 */
	private void CreateFile(String szFileName) throws AppException {
		File f = new File(m_DbDir, szFileName);
		try {
			f.createNewFile();
			f.delete();
		} catch (IOException e) {
			throw new AppException("Can not create file " + szFileName
					+ " in database.");
		} catch (SecurityException e) {
			throw new AppException("Can not create file " + szFileName
					+ " in database. Access denied");
		}
	}

	/*
	 * EnableControls
	 */
	private void EnableControls(boolean bEnable) {
		mButtonEnroll.setEnabled(bEnable);
		mButtonIdentify.setEnabled(bEnable);
		mButtonVerify.setEnabled(bEnable);
		mButtonExit.setEnabled(bEnable);
		mChkboxLFD.setEnabled(bEnable);
		mChkboxDisableMIOT.setEnabled(bEnable);
		mSpinnerFARN.setEnabled(bEnable);
		mSpinnerMaxFrames.setEnabled(bEnable);
		mSpinnerSdkVersion.setEnabled(bEnable);
		mButtonStop.setEnabled(!bEnable);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onActivityResult(int, int,
	 * android.content.Intent)
	 */
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_INPUT_USERNAME:
			if (resultCode == Activity.RESULT_OK) {
				String szFingerName = data.getExtras().getString(
						FingernameUsed.RET_FINGER_NAME);
				if (szFingerName == null) {
					mTxtMessage.setText("You must choose finger to use.");
					return;
				}
				CheckFingerName(szFingerName);
			} else {
				mTxtMessage.setText("Canceled.");
			}
			break;
		case REQUEST_SELECT_USERNAME:
			if (resultCode == Activity.RESULT_OK) {
				String szUserName = data.getExtras().getString(
						SelectUser.RET_SELECTED_USER);
				if (szUserName == null) {
					mTxtMessage.setText("Not user selected.");
					return;
				}
				// (szUserName);
			} else {
				mTxtMessage.setText("Canceled.");
			}
			break;
		}
	}

	@Override
	protected void onDestroy() {
		if (m_Operation != null) {
			m_Operation.Dispose();
		}
		super.onDestroy();
	}

	private void LoadAllFingerprints() {
		patients = new Vector<FingerprintRecord>();
		patt = new Vector<DbRecord>();
		
		JSONArray response = mobiHealth.getPatientData();
		JSONObject firstEvent;
		FingerprintRecord fr;
		DbRecord dr;
		try {
			for (int i = 0; i < response.length(); i++) {

				JSONObject data = new JSONObject(response.getString(i));

				mobiHealth.setFname(data.getString("fname"));
				mobiHealth.setLname(data.getString("lname"));

				mobiHealth.setNextvisitDate1(data.getString("nextvisitDate"));
				mobiHealth.setPatientId(data.getString("patientId"));
				mobiHealth.setPhone(data.getString("phone"));
				mobiHealth.setDuration(data.getString("duration"));
				mobiHealth.setEditView(1);

				homeItemList.add(new Patienttem(0, data.getString("uuidp"),
						data.getString("name"), 1, true, data
								.getString("identifier"),
						data.getString("age"), data.getString("gender")));

				// suggest.add(SuggestKey);

			}
			mobiHealth.setSearchList(homeItemList);

			// Toast.makeText(FingerprintScannerActivity.this,
			// ">>>"+mobiHealth.getSearchList(), Toast.LENGTH_LONG)
			// .show();
		} catch (Exception e) {

			e.printStackTrace();
		}

		for (int i = 0; i < response.length(); i++) {
			try {
				firstEvent = (JSONObject) response.get(i);
				dr = new DbRecord();
				dr.setTemplate(Base64.decode(firstEvent.getString("finger")));
				fr = new FingerprintRecord(firstEvent.getString("uuid"),
						firstEvent.getString("finger"));
				fr.setName(firstEvent.getString("name"));
				fr.setIdentifier(firstEvent.getString("identifier"));
				fr.setAge(firstEvent.getString("age"));

				patients.add(fr);
				patt.add(dr);
			} catch (Exception e) {
				Toast.makeText(FingerprintScannerActivity.this,
						"Sorry. No patient(s) found." + e.getMessage(),
						Toast.LENGTH_LONG).show();
				e.printStackTrace();
			}
		}
		if (response.length() == 0) {
			Toast.makeText(FingerprintScannerActivity.this,
					"Sorry. No patient(s) found.", Toast.LENGTH_LONG).show();
		} else {
			// Toast.makeText(FingerprintScannerActivity.this,
			// patients.size()+"<><", Toast.LENGTH_LONG).show();

			StartIdentify();

		}
	}

	protected Dialog onCreateDialog(int id) {
		Dialog dialog = null;
		switch (id) {
		case Main.DIALOG_SERVICE_RESPONSE_ID:
			ResponseDialog rd = new ResponseDialog(this);
			rd.setCustomTitle(mobiHealth.getCurrentDialogTitle());
			rd.setMessage(mobiHealth.getCurrentDialogMsg());
			dialog = rd;
			break;
		case Main.DIALOG_MSG_ID:
			CustomDialog cd = new CustomDialog(this);
			cd.setCustomTitle(mobiHealth.getCurrentDialogTitle());
			cd.setMessage(mobiHealth.getCurrentDialogMsg());
			dialog = cd;
			break;
		case Main.DIALOG_ERROR_ID:
			cd = new CustomDialog(this);
			cd.setCustomTitle(mobiHealth.getCurrentDialogTitle());
			cd.setMessage(mobiHealth.getCurrentDialogMsg());
			dialog = cd;
			break;
		case Main.DIALOG_PROFILE_UPDATE_MSG_ID:
			ProfileUpdateDialog pud = new ProfileUpdateDialog(this);
			pud.setCustomTitle(mobiHealth.getCurrentDialogTitle());
			pud.setMessage(mobiHealth.getCurrentDialogMsg()
					+ "\nThe application will restart when you click OK");
			pud.setSuccessful(true);
			dialog = pud;
			break;
		case Main.DIALOG_PROFILE_UPDATE_ERROR_ID:
			pud = new ProfileUpdateDialog(this);
			pud.setCustomTitle(mobiHealth.getCurrentDialogTitle());
			pud.setMessage(mobiHealth.getCurrentDialogMsg());
			pud.setSuccessful(false);
			dialog = pud;
			break;
		case Main.DIALOG_PROGRESS_ID:
			// builder = new AlertDialog.Builder(this);

			CustomProgressDialog pd = new CustomProgressDialog(this);
			dialog = pd;

			break;

		default:
			dialog = null;
		}
		return dialog;
	}

	protected void onPrepareDialog(int id, Dialog dialog) {
		// AlertDialog ad = (AlertDialog) dialog;
		switch (id) {
		case Main.DIALOG_SERVICE_RESPONSE_ID:
			ResponseDialog rd = (ResponseDialog) dialog;
			rd.setCustomTitle(mobiHealth.getCurrentDialogTitle());
			rd.setMessage(mobiHealth.getCurrentDialogMsg());
			break;
		case Main.DIALOG_MSG_ID:
			CustomDialog cd = (CustomDialog) dialog;
			cd.setCustomTitle(mobiHealth.getCurrentDialogTitle());
			cd.setMessage(mobiHealth.getCurrentDialogMsg());
			break;
		case Main.DIALOG_ERROR_ID:
			cd = (CustomDialog) dialog;
			cd.setCustomTitle(mobiHealth.getCurrentDialogTitle());
			cd.setMessage(mobiHealth.getCurrentDialogMsg());
			break;
		case Main.DIALOG_PROFILE_UPDATE_MSG_ID:
			ProfileUpdateDialog pud = (ProfileUpdateDialog) dialog;
			pud.setCustomTitle(mobiHealth.getCurrentDialogTitle());
			pud.setMessage(mobiHealth.getCurrentDialogMsg()
					+ "\nThe application will restart when you click OK");
			pud.setSuccessful(true);
			break;
		case Main.DIALOG_PROFILE_UPDATE_ERROR_ID:
			pud = (ProfileUpdateDialog) dialog;
			pud.setCustomTitle(mobiHealth.getCurrentDialogTitle());
			pud.setMessage(mobiHealth.getCurrentDialogMsg());
			pud.setSuccessful(false);

			break;
		case Main.DIALOG_PROGRESS_ID:
			CustomProgressDialog pd = (CustomProgressDialog) dialog;
			ProgressBar pb = (ProgressBar) pd
					.findViewById(R.id.progressbar_default);
			pb.setVisibility(View.GONE);
			pb.setVisibility(View.VISIBLE);
			break;

		default:
			dialog = null;
		}
	}

}
