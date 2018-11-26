package com.invengo.train.xc2002.entity;

import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Environment;
import android.webkit.JavascriptInterface;

import com.invengo.train.rfid.EmCb;
import com.invengo.train.rfid.InfCallBack;
import com.invengo.train.rfid.tag.BaseTag;
import com.invengo.train.rfid.tag.TagUn;
import com.invengo.train.rfid.xc2002.Rd;
import com.invengo.train.xc2002.Ma;
import com.invengo.train.xc2002.R;
import com.invengo.train.xc2002.dao.DbLocal;
import com.invengo.train.xc2002.enums.EmUh;
import com.invengo.train.xc2002.enums.EmUrl;

import java.io.File;

import static com.invengo.train.rfid.tag.BaseTag.confAble;
import static com.invengo.train.xc2002.Ma.sdDir;

/**
 * 读写器
 * Created by ziniulian on 2017/10/17.
 */

public class Web {
	private static final String confPath = "conf.xml";	// 配置文件路径

	private Rd rfd = new Rd();
	private Ma ma;
	private String appNam;
	private DbLocal db;

	// 声音
	private SoundPool sp = null;
	private int music_success;
	private int music_err;
	private int music_null;
	private int music_tag;

	public void init (Ma m) {
		ma = m;

		// 声音
		sp = new SoundPool(3, AudioManager.STREAM_SYSTEM, 5);
		music_success = sp.load(m, R.raw.ok, 1);
		music_err = sp.load(m, R.raw.error, 1);
		music_null = sp.load(m, R.raw.click, 1);
		music_tag = sp.load(m, R.raw.tag, 1);

		// 设置监听
		rfd.setCallBackListenter(new InfCallBack() {
			@Override
			public void onReadTag(BaseTag tag) {
				int b = 0;
				if (tag instanceof TagUn) {
					if (tag.getCod().startsWith("位标序号")) {
						sp.play(music_tag, 1, 1, 0, 0, 1);	// 位标匹配的声音
					} else {
						sp.play(music_null, 1, 1, 0, 0, 1);	// 发出类型不符的声音
						b = 1;	// 位标类型不符
					}
				} else {
					sp.play(music_success, 1, 1, 0, 0, 1);	// 发出扫描成功的声音
				}
				if (b == 1) {
					ma.sendUrl(EmUrl.TrtErr);
				} else {
					ma.sendUrl(EmUrl.HdRead, tag.toJson());
				}
			}

			@Override
			public void cb(EmCb e, String[] args) {
				switch (e) {
					case ErrInit:
					case ErrConnect:
						ma.sendUrl(EmUrl.Err);
						break;
					case ErrRead:
					case ReadNull:
						sp.play(music_err, 1, 1, 0, 0, 1);	// 发出扫描失败的声音
						ma.sendUrl(EmUrl.ReadNull);
						break;
					case Reading:
						ma.sendUrl(EmUrl.Reading);
						break;
				}
			}
		});

		try {
			// TODO: 内存卡被拔出时触发 ErrInit 事件
			db = new DbLocal(m);
			appNam = confAble(new File(Environment.getExternalStorageDirectory(), sdDir + confPath), ma.getAssets().open(confPath));
			rfd.init(ma);
		} catch (Exception e) {
//			e.printStackTrace();
			rfd.cb(EmCb.ErrInit, e.getMessage());
		}
	}

	public void open() {
		rfd.open();
	}

	public void close() {
		rfd.close();
	}

/*----------------------------------------*/

	@JavascriptInterface
	public void read() {
		rfd.read();
	}

	@JavascriptInterface
	public String getAppNam() {
		return appNam;
	}

	@JavascriptInterface
	public void dbSav(String cod, String xiu) {
		db.sav(new String[] {ma.getTim(), cod, xiu});
	}

	@JavascriptInterface
	public void dbDel(String ids) {
		db.del(new String[] {ids});
	}

	@JavascriptInterface
	public void dbClear() {
		db.del(null);
	}

	@JavascriptInterface
	public void dbSet(long id, String xiu) {
		db.set(new String[] {id + "", xiu});
	}

	@JavascriptInterface
	public String dbGet(long p, long len) {
		return db.get(p + "", len + "");
	}

	@JavascriptInterface
	public long dbCount() {
		return db.count();
	}

	@JavascriptInterface
	public void flashlight() {
		ma.fl.flashlight(false);
	}

	@JavascriptInterface
	public boolean getFlashlight() {
		return ma.fl.isFlb();
	}

	@JavascriptInterface
	public void startTim() {
		ma.sendUh(EmUh.StartTim);
	}

	@JavascriptInterface
	public void flushTim() {
		ma.sendUh(EmUh.Tim);
	}

	@JavascriptInterface
	public String getTim() {
		return ma.getTimV();
	}

	@JavascriptInterface
	public void callTim() {
		ma.callTim();
	}

	@JavascriptInterface
	public void exit() {
		ma.finish();
	}

}
