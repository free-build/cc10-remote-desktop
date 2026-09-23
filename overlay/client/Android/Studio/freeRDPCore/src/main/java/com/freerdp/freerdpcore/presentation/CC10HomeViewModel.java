package com.freerdp.freerdpcore.presentation;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.freerdp.freerdpcore.data.AppDatabase;
import com.freerdp.freerdpcore.domain.BookmarkBase;
import com.freerdp.freerdpcore.domain.ConnectionReference;
import com.freerdp.freerdpcore.services.ManualBookmarkGateway;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CC10HomeViewModel extends AndroidViewModel
{
	public static class Action {
		public final String reference; public final boolean connect;
		Action(String reference, boolean connect) { this.reference = reference; this.connect = connect; }
	}
	private final MutableLiveData<BookmarkBase> profile = new MutableLiveData<>();
	private final MutableLiveData<Action> action = new MutableLiveData<>();
	private final MutableLiveData<String> error = new MutableLiveData<>();
	private final ExecutorService executor = Executors.newSingleThreadExecutor();
	private final ManualBookmarkGateway gateway;

	public CC10HomeViewModel(@NonNull Application app) {
		super(app); gateway = new ManualBookmarkGateway(AppDatabase.getInstance(app).bookmarkDao());
	}
	public LiveData<BookmarkBase> getProfile() { return profile; }
	public LiveData<Action> getAction() { return action; }
	public LiveData<String> getError() { return error; }
	public void loadProfile() { executor.execute(() -> {
		List<BookmarkBase> all = gateway.findAll();
		BookmarkBase bm = all.isEmpty() ? defaults() : all.get(0);
		configureNativeScreen(bm);
		if (!all.isEmpty()) gateway.update(bm);
		profile.postValue(bm);
	}); }
	public void save(String host, int port, String user, String password, boolean connect) {
		executor.execute(() -> { try {
			List<BookmarkBase> all = gateway.findAll();
			BookmarkBase bm = all.isEmpty() ? defaults() : all.get(0);
			bm.setType(BookmarkBase.TYPE_MANUAL); bm.setLabel(host); bm.setHostname(host);
			bm.setPort(port); bm.setUsername(user); bm.setPassword(password); bm.setDirectConnect(true);
			bm.getPerformanceFlags().setH264(false);
			configureNativeScreen(bm);
			if (bm.getId() > 0) gateway.update(bm); else gateway.insert(bm);
			gateway.deleteAllExcept(bm.getId()); profile.postValue(bm);
			action.postValue(new Action(ConnectionReference.getBookmarkReference(bm.getId()), connect));
		} catch (Exception e) { error.postValue("保存连接失败：" + e.getMessage()); } });
	}
	private static BookmarkBase defaults() {
		BookmarkBase bm = new BookmarkBase(); bm.setType(BookmarkBase.TYPE_MANUAL); bm.setPort(3389);
		bm.setDirectConnect(true); bm.getPerformanceFlags().setH264(false);
		configureNativeScreen(bm); return bm;
	}
	private static void configureNativeScreen(BookmarkBase bm) {
		// CC10 is a native 1280x800 panel. Request that exact desktop size during the
		// initial RDP negotiation; "fitscreen" discards the supplied dimensions and
		// may reconnect to an xrdp session using a different framebuffer size.
		bm.getScreenSettings().setResolution("1280x800", 1280, 800);
		bm.getScreenSettings().setScale("100", 100, 100);
	}
	@Override protected void onCleared() { executor.shutdown(); }
}
