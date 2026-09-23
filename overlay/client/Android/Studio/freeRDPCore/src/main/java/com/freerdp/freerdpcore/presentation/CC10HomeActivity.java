package com.freerdp.freerdpcore.presentation;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.freerdp.freerdpcore.databinding.Cc10HomeBinding;
import com.freerdp.freerdpcore.domain.BookmarkBase;

public class CC10HomeActivity extends AppCompatActivity
{
	private Cc10HomeBinding binding; private CC10HomeViewModel vm;
	private ExternalDisplayManager displayManager; private boolean populated;
	@Override public void onCreate(Bundle state) {
		supportRequestWindowFeature(Window.FEATURE_NO_TITLE); super.onCreate(state);
		// Appliance defaults: let the RDP surface use every physical pixel of CC10.
		ApplicationSettingsActivity.get(this).edit()
			.putBoolean(getString(com.freerdp.freerdpcore.R.string.preference_key_ui_hide_status_bar), true)
			.putBoolean(getString(com.freerdp.freerdpcore.R.string.preference_key_ui_hide_navigation_bar), true)
			.putBoolean(getString(com.freerdp.freerdpcore.R.string.preference_key_power_keep_screen_on_when_connected), true)
			.apply();
		binding = Cc10HomeBinding.inflate(getLayoutInflater()); setContentView(binding.getRoot()); immersive();
		displayManager = new ExternalDisplayManager(this); vm = new ViewModelProvider(this).get(CC10HomeViewModel.class);
		vm.getProfile().observe(this, this::showProfile);
		vm.getAction().observe(this, event -> { if (event == null) return; binding.buttonConnect.setEnabled(true);
			if (event.connect) { binding.textConnectionStatus.setText("正在连接…"); displayManager.launchSessionWithDisplayPicker(event.reference); }
			else { binding.textConnectionStatus.setText("设置已保存"); android.content.Intent i = new android.content.Intent(this, BookmarkActivity.class);
				i.putExtra(BookmarkActivity.PARAM_CONNECTION_REFERENCE, event.reference); startActivity(i); } });
		vm.getError().observe(this, msg -> { binding.buttonConnect.setEnabled(true); if (!TextUtils.isEmpty(msg)) Toast.makeText(this,msg,Toast.LENGTH_LONG).show(); });
		binding.buttonConnect.setOnClickListener(v -> save(true)); binding.buttonAdvanced.setOnClickListener(v -> save(false));
		getOnBackPressedDispatcher().addCallback(this,new OnBackPressedCallback(true){@Override public void handleOnBackPressed(){finish();}});
		vm.loadProfile();
	}
	private void showProfile(BookmarkBase bm) { if (bm == null || populated) return; populated=true;
		binding.editHostname.setText(bm.getHostname()); binding.editPort.setText(String.valueOf(bm.getPort()>0?bm.getPort():3389));
		binding.editUsername.setText(bm.getUsername()); binding.editPassword.setText(bm.getPassword());
		binding.textConnectionStatus.setText(bm.getHostname().isEmpty()?"请输入远程主机信息":"已加载上次连接"); }
	private void save(boolean connect) { String host=binding.editHostname.getText().toString().trim();
		if(host.isEmpty()){binding.editHostname.setError("请输入 IP 地址或域名");return;} int port;
		try{port=Integer.parseInt(binding.editPort.getText().toString().trim());}catch(Exception e){binding.editPort.setError("端口无效");return;}
		if(port<1||port>65535){binding.editPort.setError("端口范围 1–65535");return;} binding.buttonConnect.setEnabled(false);
		binding.textConnectionStatus.setText("正在保存…"); vm.save(host,port,binding.editUsername.getText().toString().trim(),binding.editPassword.getText().toString(),connect); }
	private void immersive(){if(getSupportActionBar()!=null)getSupportActionBar().hide();getWindow().getDecorView().setSystemUiVisibility(
		View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY|View.SYSTEM_UI_FLAG_FULLSCREEN|View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|
		View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_LAYOUT_STABLE);}
	@Override public void onWindowFocusChanged(boolean focus){super.onWindowFocusChanged(focus);if(focus)immersive();}
	@Override protected void onResume(){super.onResume();if(binding!=null)binding.buttonConnect.setEnabled(true);immersive();}
}
