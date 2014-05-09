package org.apache.cordova.plugin.sms;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.telephony.SmsManager;

public class Sms extends CordovaPlugin {
	public final String ACTION_SEND_SMS = "send";

	BroadcastReceiver receiver;
	private CallbackContext callbackContext;

	@Override
	public boolean execute(String action, JSONArray args,
			final CallbackContext callbackContext) throws JSONException {
		this.callbackContext = callbackContext;

		if (action.equals(ACTION_SEND_SMS)) {
			try {
				String phoneNumber = args.getJSONArray(0).join(";")
						.replace("\"", "");
				String message = args.getString(1);
				String method = args.getString(2);

				if (!checkSupport()) {
					callbackContext.sendPluginResult(new PluginResult(
							PluginResult.Status.ERROR,
							"SMS not supported on this platform"));
					return true;
				}

				if (method.equalsIgnoreCase("INTENT")) {
					invokeSMSIntent(phoneNumber, message);
					// the result is always false, so better pass no result for
					// clarity
					callbackContext.sendPluginResult(new PluginResult(
							PluginResult.Status.NO_RESULT));
				} else {
					sendSMS(phoneNumber, message);
				}

				callbackContext.sendPluginResult(new PluginResult(
						PluginResult.Status.OK));
				return true;
			} catch (JSONException ex) {
				callbackContext.sendPluginResult(new PluginResult(
						PluginResult.Status.JSON_EXCEPTION));
			}
		}
		return false;
	}

	private boolean checkSupport() {
		Activity ctx = this.cordova.getActivity();
		return ctx.getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_TELEPHONY);
	}

	@SuppressLint("NewApi")
	private void invokeSMSIntent(String phoneNumber, String message) {

		Intent sendIntent = new Intent(Intent.ACTION_VIEW);
		sendIntent.setData(Uri.parse("smsto:" + Uri.encode(phoneNumber)));
		sendIntent.putExtra("sms_body", message);

		this.cordova.getActivity().startActivity(sendIntent);
	}

	@Override
	public void onDestroy() {
		if (this.receiver != null) {
			try {
				this.cordova.getActivity().unregisterReceiver(this.receiver);
				this.receiver = null;
			} catch (Exception ignore) {
			}
		}
	}

	private void sendSMS(String phoneNumber, String message) {
		SmsManager manager = SmsManager.getDefault();
		PendingIntent sentIntent = PendingIntent.getActivity(
				this.cordova.getActivity(), 0, new Intent(), 0);
		manager.sendTextMessage(phoneNumber, null, message, sentIntent, null);
	}
}
