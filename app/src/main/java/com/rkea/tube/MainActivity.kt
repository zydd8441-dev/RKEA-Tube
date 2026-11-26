package com.rkea.tube

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.Gravity
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import java.io.File

class MainActivity:AppCompatActivity(){
 private val bg=0xFF101014.toInt();private val fg=0xFFF5F3FA.toInt();private val panel=0xFF1B1B22.toInt();private lateinit var content:FrameLayout;private var web:WebView?=null;private var exo:ExoPlayer?=null;private var browser=false;private val dm by lazy{getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager}
 override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);window.statusBarColor=bg;window.navigationBarColor=bg;val root=LinearLayout(this).apply{orientation=1;setBackgroundColor(bg)};root.addView(TextView(this).apply{text="RKEA TUBE";textSize=23f;setTextColor(fg);gravity=Gravity.CENTER;setPadding(16,18,16,18)},LinearLayout.LayoutParams(-1,-2));content=FrameLayout(this);root.addView(content,LinearLayout.LayoutParams(-1,0,1f));val nav=LinearLayout(this);listOf("الرئيسية","المتصفح","التنزيلات","المكتبة").forEachIndexed{i,s->nav.addView(Button(this).apply{text=s;isAllCaps=false;textSize=11f;setTextColor(fg);setBackgroundColor(panel);setOnClickListener{when(i){0->home();1->browserPage();2->downloadsPage();else->libraryPage()}}},LinearLayout.LayoutParams(0,58,1f))};root.addView(nav);setContentView(root);home()}
 private fun page():LinearLayout{content.removeAllViews();return LinearLayout(this).apply{orientation=1;setPadding(14,12,14,12);content.addView(this,FrameLayout.LayoutParams(-1,-1))}}
 private fun label(s:String,size:Float=16f)=TextView(this).apply{text=s;textSize=size;setTextColor(fg);setPadding(0,8,0,8)}
 private fun button(s:String,action:()->Unit)=Button(this).apply{text=s;isAllCaps=false;setOnClickListener{action()}}
 private fun home(){browser=false;val p=page();p.addView(label("أهلاً بيك بـ RKEA Tube",23f));p.addView(label("متصفح • تنزيل روابط مباشرة • مشغل محلي"));p.addView(button("فتح المتصفح"){browserPage()});p.addView(button("التنزيلات"){downloadsPage()});p.addView(button("المكتبة"){libraryPage()});p.addView(label("التنزيل للروابط المباشرة المسموح بها فقط. لا يتم تجاوز DRM أو حماية المواقع.",12f))}
 private fun browserPage(){browser=true;val p=page();val url=EditText(this).apply{hint="رابط موقع أو ملف مباشر";setTextColor(fg);singleLine=true};p.addView(url);p.addView(button("فتح الرابط"){var u=url.text.toString().trim();if(u.isNotEmpty()){if(!u.startsWith("http://")&&!u.startsWith("https://"))u="https://$u";web?.loadUrl(u)}});p.addView(button("تنزيل الرابط المباشر"){val u=url.text.toString().trim();if(u.startsWith("http://")||u.startsWith("https://"))enqueue(u)else toast("أدخل رابط مباشر")});web=WebView(this).apply{settings.javaScriptEnabled=true;settings.domStorageEnabled=true;webViewClient=object:WebViewClient(){override fun onPageFinished(v:WebView?,u:String?){url.setText(u.orEmpty())}};webChromeClient=WebChromeClient();setDownloadListener{u,_,_,_,_->enqueue(u)};loadUrl("https://m.youtube.com")};p.addView(web,LinearLayout.LayoutParams(-1,0,1f))}
 private fun enqueue(u:String){try{val uri=Uri.parse(u);val name=(uri.lastPathSegment?:"media.bin").substringAfterLast("/").take(90).ifBlank{"media.bin"};val req=DownloadManager.Request(uri).setTitle(name).setDescription("RKEA Tube").setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED).setDestinationInExternalFilesDir(this,Environment.DIRECTORY_MOVIES,"RKEA Tube/$name");dm.enqueue(req);toast("تمت إضافة التنزيل")}catch(ex:Exception){toast("تعذر بدء التنزيل: "+ex.message)}}
 private fun downloadsPage(){val p=page();p.addView(label("التنزيلات",22f));p.addView(button("تحديث"){downloadsPage()});val c=dm.query(DownloadManager.Query());if(c==null||!c.moveToFirst()){c?.close();p.addView(label("لا توجد تنزيلات حالياً"));return};do{val id=c.getLong(c.getColumnIndexOrThrow(DownloadManager.COLUMN_ID));val title=c.getString(c.getColumnIndexOrThrow(DownloadManager.COLUMN_TITLE));val st=c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS));val done=c.getLong(c.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));val total=c.getLong(c.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));val state=when(st){DownloadManager.STATUS_SUCCESSFUL->"اكتمل";DownloadManager.STATUS_FAILED->"فشل";DownloadManager.STATUS_RUNNING->"ينزل";else->"بالانتظار"};p.addView(label("$title\n$state • "+if(total>0)"${done*100/total}%" else "الحجم غير معروف",14f));p.addView(button("إلغاء"){dm.remove(id);downloadsPage()})}while(c.moveToNext());c.close()}
 private fun libraryPage(){val p=page();p.addView(label("المكتبة المحلية",22f));val d=File(getExternalFilesDir(Environment.DIRECTORY_MOVIES),"RKEA Tube");val files=d.listFiles()?.filter{it.isFile}?:emptyList();if(files.isEmpty())p.addView(label("الملفات المكتملة تظهر هنا"));files.forEach{f->p.addView(button("▶ "+f.name){play(f)});p.addView(button("مشاركة"){share(f)})}}
 private fun play(f:File){val p=page();p.addView(label(f.name));exo?.release();exo=ExoPlayer.Builder(this).build();val pv=PlayerView(this).apply{player=exo};p.addView(pv,LinearLayout.LayoutParams(-1,0,1f));exo?.setMediaItem(MediaItem.fromUri(Uri.fromFile(f)));exo?.prepare();exo?.play();p.addView(button("تشغيل / إيقاف"){exo?.let{if(it.isPlaying)it.pause()else it.play()}});p.addView(button("تقديم 10 ثواني"){exo?.seekTo((exo?.currentPosition?:0)+10000)});p.addView(button("رجوع 10 ثواني"){exo?.seekTo((exo?.currentPosition?:0)-10000)})}
 private fun share(f:File){try{val u=FileProvider.getUriForFile(this,packageName+".files",f);val i=Intent(Intent.ACTION_SEND).apply{type="video/*";putExtra(Intent.EXTRA_STREAM,u);addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)};startActivity(Intent.createChooser(i,"مشاركة"))}catch(ex:Exception){toast("تعذرت المشاركة")}}
 private fun toast(s:String)=Toast.makeText(this,s,Toast.LENGTH_LONG).show()
 override fun onBackPressed(){if(browser&&web?.canGoBack()==true)web?.goBack()else super.onBackPressed()}
 override fun onDestroy(){exo?.release();web?.destroy();super.onDestroy()}
}