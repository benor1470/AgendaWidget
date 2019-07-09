package benor.MLog;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.logging.Handler;
import java.util.logging.LogRecord;


import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.StatFs;
import android.provider.Settings.Secure;
import android.util.Log;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


public class MLog extends Handler implements Thread.UncaughtExceptionHandler {
	protected static final String SERVER_URL = "http://www.ben-or.net/android/reportError.php";
	public static boolean reporting;
	private static String TAG = null;
	String VersionName;
	private int VersionCode;
	String PackageName;
	String FilePath;
	String PhoneModel;
	String AndroidVersion;
	String Board;
	String Brand;
	String Device;
	String Display;
	String FingerPrint;
	String Host;
	String ID;
	String Manufacturer;
	String Model;
	String Product;
	String Tags;
	long Time;
	String Type;
	String User;
	public String uniqueId;

	private List<String> loging;
	private HashMap<String, String> customParameters = new HashMap<>();

	private Thread.UncaughtExceptionHandler PreviousHandler;
	private static MLog S_mInstance;
	private Context context;
	private static boolean sendTologcat;

	/**
	 * @param context         the activity context, used for ui thread
	 * @param TAG             used for Log.i(TAG
	 * @param autoSendForThis auto send errors to server, disable while debugging
	 */
	public void Init(Context context, String TAG, boolean autoSendForThis, boolean sendTologcat) {
		if (MLog.TAG == null) {
			MLog.TAG = TAG;
			PreviousHandler = Thread.getDefaultUncaughtExceptionHandler();
			Thread.setDefaultUncaughtExceptionHandler(this);
			this.context = context;
			reporting = false;
			MLog.sendTologcat = sendTologcat;
			loging = new LinkedList<>();
			S_mInstance = this;
			if (autoSendForThis == true) {
				checkErrorAndSend(false);
			}
		}
	}

	/**
	 * @return the Mlog object, if there no object was created the it will create one
	 */
	public static MLog getInstance() {
		if (S_mInstance == null) {
			S_mInstance = new MLog();
		}
		return S_mInstance;
	}

	private String CreateCustomInfoString() {
		String CustomInfo = "";
		Iterator<String> iterator = customParameters.keySet().iterator();
		while (iterator.hasNext()) {
			String CurrentKey = iterator.next();
			String CurrentVal = customParameters.get(CurrentKey);
			CustomInfo += CurrentKey + " = " + CurrentVal + "\n";
		}
		return CustomInfo;
	}

	private long getAvailableInternalMemorySize() {
		File path = Environment.getDataDirectory();
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSize();
		long availableBlocks = stat.getAvailableBlocks();
		return availableBlocks * blockSize;
	}

	private long getTotalInternalMemorySize() {
		File path = Environment.getDataDirectory();
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSize();
		long totalBlocks = stat.getBlockCount();
		return totalBlocks * blockSize;
	}

	private void RecoltInformations(Context context) {
		try {
			PackageManager pm = context.getPackageManager();
			PackageInfo pi;
			// Version
			pi = pm.getPackageInfo(context.getPackageName(), 0);
			VersionName = pi.versionName;
			//versionNumber
			VersionCode = pi.versionCode;

			// Package name
			PackageName = pi.packageName;
			// Device model
			PhoneModel = android.os.Build.MODEL;
			// Android version
			AndroidVersion = android.os.Build.VERSION.RELEASE;

			Board = android.os.Build.BOARD;
			Brand = android.os.Build.BRAND;
			Device = android.os.Build.DEVICE;
			Display = android.os.Build.DISPLAY;
			FingerPrint = android.os.Build.FINGERPRINT;
			Host = android.os.Build.HOST;
			ID = android.os.Build.ID;
			Model = android.os.Build.MODEL;
			Product = android.os.Build.PRODUCT;
			Tags = android.os.Build.TAGS;
			Time = android.os.Build.TIME;
			Type = android.os.Build.TYPE;
			User = android.os.Build.USER;
			try {
				uniqueId = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
			} catch (Exception e) {
				uniqueId = "other";
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String CreateInformationString() {
		RecoltInformations(context);

		String ReturnVal = "";
		ReturnVal += "programs : " + TAG;
		ReturnVal += "\n";
		ReturnVal += "VersionCode : " + VersionCode;
		ReturnVal += "\n";
		ReturnVal += "VersionName : " + VersionName;
		ReturnVal += "\n";
		ReturnVal += "Package : " + PackageName;
		ReturnVal += "\n";
		ReturnVal += "FilePath : " + FilePath;
		ReturnVal += "\n";
		ReturnVal += "Phone Model" + PhoneModel;
		ReturnVal += "\n";
		ReturnVal += "Android Version : " + AndroidVersion;
		ReturnVal += "\n";
		ReturnVal += "Board : " + Board;
		ReturnVal += "\n";
		ReturnVal += "Brand : " + Brand;
		ReturnVal += "\n";
		ReturnVal += "Device : " + Device;
		ReturnVal += "\n";
		ReturnVal += "Display : " + Display;
		ReturnVal += "\n";
		ReturnVal += "Finger Print : " + FingerPrint;
		ReturnVal += "\n";
		ReturnVal += "Host : " + Host;
		ReturnVal += "\n";
		ReturnVal += "ID : " + ID;
		ReturnVal += "\n";
		ReturnVal += "Model : " + Model;
		ReturnVal += "\n";
		ReturnVal += "Product : " + Product;
		ReturnVal += "\n";
		ReturnVal += "Tags : " + Tags;
		ReturnVal += "\n";
		ReturnVal += "Time : " + Time;
		ReturnVal += "\n";
		ReturnVal += "Type : " + Type;
		ReturnVal += "\n";
		ReturnVal += "User : " + User;
		ReturnVal += "\n";
		ReturnVal += "unique id : " + uniqueId;
		ReturnVal += "\n";
		ReturnVal += "Total Internal memory : " + getTotalInternalMemorySize();
		ReturnVal += "\n";
		ReturnVal += "Available Internal memory : " + getAvailableInternalMemorySize();
		ReturnVal += "\n";

		try {
			ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
			ActivityManager.MemoryInfo mInfo = new ActivityManager.MemoryInfo();
			am.getMemoryInfo(mInfo);
			ReturnVal += " app availMem " + mInfo.availMem;
			ReturnVal += "\n";
			ReturnVal += " is it on low memory? " + mInfo.lowMemory;
			ReturnVal += "\n";
			ReturnVal += " threshold " + mInfo.threshold;
			ReturnVal += "\n";

		} catch (Exception e) {
			ReturnVal += MLog.e("had error geting memory info", e);
		}

		return ReturnVal;
	}

	/**
	 * log an error on Thread, used for global try catch
	 *
	 * @return
	 */
	public void uncaughtException(Thread t, Throwable e) {
		MLog.e("error", e);
		String Report = "Stack : \n" + "======= \n";

		final Writer result = new StringWriter();
		final PrintWriter printWriter = new PrintWriter(result);
		e.printStackTrace(printWriter);
		String stacktrace = result.toString();
		Report += stacktrace;

		Report += "\n";
		Report += "Cause : \n";
		Report += "======= \n";
		// If the exception was thrown in a background thread inside
		// AsyncTask, then the actual exception can be found with getCause
		Throwable cause = e.getCause();
		while (cause != null) {
			cause.printStackTrace(printWriter);
			Report += result.toString();
			cause = cause.getCause();
		}
		printWriter.close();
		Report += "****  End of current Report ***";
		Report += createReportString();
		SaveAsFile(Report);
		PreviousHandler.uncaughtException(t, e);

	}

	private String createReportString() {
		String Report = "";
		Date CurDate = new Date();
		Report += "Error Report collected on : " + CurDate.toString();
		Report += "\n";
		Report += "\n";
		Report += "Informations :";
		Report += "\n";
		Report += "==============";
		Report += "\n";
		Report += "\n";
		Report += CreateInformationString();

		Report += "Custom Informations :\n";
		Report += "=====================\n";
		Report += CreateCustomInfoString();

		Report += "Log Stream :\n";
		Report += "=====================\n";
		Report += getLogString();

		return Report;
	}

	private String getLogString() {
		String logs = "\n\nLogCat:\n";
		try {
			String[] log = loging.toArray(new String[1]);
			for (int i = 0; i < log.length; ++i) {
				logs += log[i] + "\n";
			}
		} catch (Exception e) {
			logs += "\n\nLoging error " + e.getMessage() + " loging=" + loging.size();
		}
		return logs;
	}

	private void createErrorDialog(String error) {
		final String errorMsg = error;
		final MLog self = this;
		Log.i(TAG, "created Dialog");

		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		TextView tv = new TextView(context);
		builder.setTitle("The game had a fc, what happend?");
		final EditText et = new EditText(context);
		LinearLayout ll = new LinearLayout(context);
		ll.setOrientation(LinearLayout.VERTICAL);
		ll.addView(tv);
		//ll.addView(et);
		builder.setView(et);
		builder.setPositiveButton("Send report", new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				Toast.makeText(context, "thank you", Toast.LENGTH_SHORT).show();
				//self.sendErrorServer(et.getText().toString(), errorMsg);
				MLog.i("dialog clicked " + et.getText().toString());
			}
		});
		MLog.i("created dialog");
		try {
			Dialog dialog = builder.create();
			dialog.show();
		} catch (Exception e) {
			MLog.e("dialog error", e);
		}
		MLog.i("dialog is showen");
	}

	/**
	 * @param userText the text the user wrote (can be empty string)
	 * @param errorMsg the error message with log
	 */
	private void SaveAsFile(String ErrorContent) {
		try {
			Random generator = new Random();
			int random = generator.nextInt(99999);
			String FileName = "stack-" + random + ".stacktrace";
			FileOutputStream trace = context.openFileOutput(FileName, Context.MODE_PRIVATE);
			trace.write(ErrorContent.getBytes());
			trace.close();
		} catch (Exception e) {
		}
	}

	private String[] GetErrorFileList() {
		File dir = new File(FilePath + "/");
		// Try to create the files folder if it doesn't exist
		dir.mkdir();
		// Filter for ".stacktrace" files
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".stacktrace");
			}
		};
		return dir.list(filter);
	}

	private void add2Log(String msg) {
		try {
			getInstance().loging.add(msg);
		} catch (Exception e) {
			Log.e(TAG, "can't log");
		}
	}

	/**
	 * @param useDialog if true sends the log after asking the user for more data
	 * @return true if had error and there for log to send
	 */
	public boolean checkErrorAndSend(boolean useDialog) {
		try {
			FilePath = context.getFilesDir().getAbsolutePath();
			Log.i(TAG, "errors: " + GetErrorFileList().length);
			if (GetErrorFileList().length > 0) {
				String WholeErrorText = "";
				String[] ErrorFileList = GetErrorFileList();
				int curIndex = 0;
				final int MaxSendMail = 1;
				for (String curString : ErrorFileList) {
					if (curIndex++ <= MaxSendMail) {
						WholeErrorText += "New Trace collected :\n";
						WholeErrorText += "=====================\n ";
						String filePath = FilePath + "/" + curString;
						BufferedReader input = new BufferedReader(new FileReader(filePath));
						String line;
						while ((line = input.readLine()) != null) {
							WholeErrorText += line + "\n";
						}
						input.close();
					}
					// DELETE FILES
					File curFile = new File(FilePath + "/" + curString);
					curFile.delete();
				}
				reporting = true;
				Log.i(TAG, "found error");
				if (useDialog) {
					createErrorDialog(WholeErrorText);
				} else {
					//	sendErrorServer("auto sending, no dialog",WholeErrorText);
				}
				return true;
			}
			return false;
		} catch (Exception e) {
			MLog.e("failed to read errors log", e);
			return true;
		}
	}

	public void sendReportWithNoError() {
		MLog.i("sending report with out error");
		//getInstance().sendErrorServer("auto sending, no dialog",getInstance().createReportString());
	}

	@Override
	public void publish(LogRecord lr) {
		MLog.i(lr.getMessage());
	}

	@Override
	public void close() {
	}

	@Override
	public void flush() {
	}


	public static String e(String string, Throwable e) {
		return inner_e(TAG, string, e);
	}

	public static String e(String string) {
		return inner_e(TAG, string, null);
	}

	public static String e(String tag, String string, Throwable e) {
		return inner_e(tag, string, e);
	}

	public static String e(String tag, String string) {
		return inner_e(tag, string, null);
	}

	private static String inner_e(String tag, String string, Throwable e) {
		StackTraceElement[] trace = Thread.currentThread().getStackTrace();
		String info = tag + "-" + string + " (" + trace[4].getClassName() + "." + trace[4].getMethodName() + ":" + trace[4].getLineNumber() + ") E";
		getInstance().add2Log(info);

		if (e != null) {
			final Writer result = new StringWriter();
			final PrintWriter printWriter = new PrintWriter(result);
			e.printStackTrace(printWriter);
			getInstance().add2Log(result.toString());
			info += "\n\n" + result.toString();
		}

		if (sendTologcat) {
			if (e == null) {
				Log.e(tag, info);
			} else {
				Log.e(tag, info, e);
			}
		}


		return info;

	}

	public static String i(String string) {
		return inner_i(TAG, string);
	}

	public static String i(String tag, String string) {    //with TAG not default
		return inner_i(tag, string);
	}

	private static String inner_i(String tag, String string) {
		StackTraceElement[] trace = Thread.currentThread().getStackTrace();
		String info = string + " (" + trace[4].getClassName() + "." + trace[4].getMethodName() + ":" + trace[4].getLineNumber() + ") I";
		getInstance().add2Log(tag + " " + info);
		if (sendTologcat) {
			Log.i(TAG, info);
		}

		return info;
	}
}