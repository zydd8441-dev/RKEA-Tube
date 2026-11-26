package com.rkea.tube

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import java.io.File

class MainActivity : AppCompatActivity() {
    private val bg = 0xFF101014.toInt(); private val panel = 0xFF1B1B22.toInt(); private val accent = 0xFF9B7BFF.toInt(); private val fg = 0xFFF5F3FA.toInt(); private val muted = 0xFFB7B3C2.toInt()
    private lateinit var root: LinearLayout
    private lateinit var content: FrameLayout
    private var web: WebView? = null
    private var player: ExoPlayer? = null
    private var playerView: PlayerView? = null
    private var playingFile: File? = null
    private var browserVisible = false
    private val dm by lazy { getSystemService(DOWNLOAD_SERVICE) as DownloadManager }

    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); window.statusBarColor=bg; window.navigationBarColor=bg; buildShell(); showHome() }
    private fun buildShell() {
        root=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setBackgroundColor(bg) }
        val header=TextView(this).apply { text="RKEA  TUBE"; textSize=22f; setTextColor(fg); gravity=Gravity.CENTER_VERTICAL; setPadding(20,18,12,16); setTypeface(null,1) }
        root.addView(header, LinearLayout.LayoutParams(-1,-2))
        content=FrameLayout(this); root.addView(content,LinearLayout.LayoutParams(-1,0,1f))
        val nav=LinearLayout(this).apply { orientation=LinearLayout.HORIZONTAL; setBackgroundColor(panel) }
        listOf("الرئيسية","المتصفح","التنزيلات","المكتبة").forEachIndexed { i,t -> val b=Button(this).apply { text=t; textSize=11f; setTextColor(fg); isAllCaps=false; setBackgroundColor(panel); setOnClickListener { when(i){0->showHome();1->showBrowser();2->showDownloads();else->showLibrary()} } }; nav.addView(b,LinearLayout.LayoutParams(0,58,1f)) }
        root.addView(nav,LinearLayout.LayoutParams(-1,-2)); setContentView(root)
    }
    private fun page(): LinearLayout { content.removeAllViews(); return LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setPadding(16,12,16,12); setBackgroundColor(bg); content.addView(this,FrameLayout.LayoutParams(-1,-1)) } }
    private fun label(s:String,size:Float=16f):TextView=TextView(this).apply { text=s; textSize=size; setTextColor(fg); setPadding(0,10,0,10) }
    private fun button(s:String, action:()->Unit):Button=Button(this).apply { text=s; isAllCaps=false; setTextColor(fg); setBackgroundColor(panel); setOnClickListener { action() } }
    private fun input(hint:String):EditText=EditText(this).apply { this.hint=hint; setTextColor(fg); setHintTextColor(muted); setSingleLine(true); setPadding(14,8,14,8); backgroundTintList=android.content.res.ColorStateList.valueOf(accent) }
    private fun showHome(){ browserVisible=false; val p=page(); p.addView(label("مرحباً بـ RKEA Tube",24f)); p.addView(label("مشغل محلي • تنزيلات • متصفح",14f)); p.addView(button("فتح المتصفح") { showBrowser() }); p.addView(button("عرض التنزيلات") { showDownloads() }); p.addView(button("المكتبة المحلية") { showLibrary() }); p.addView(label("ملاحظة: التنزيل متاح للروابط المباشرة التي تسمح بذلك. بعض المواقع، ومنها YouTube، لا توفر رابط ملف مباشر داخل التطبيق.",13f)) }
    private fun showBrowser(){ browserVisible=true; val p=page(); val bar=LinearLayout(this).apply { orientation=LinearLayout.HORIZONTAL }; val url=input("رابط موقع أو فيديو"); val go=button("فتح") { var u=url.text.toString().trim(); if(u.isNotEmpty()){ if(!u.startsWith("http://")&&!u.startsWith("https://"))u="https://$u"; web?.loadUrl(u) } }; bar.addView(url,LinearLayout.LayoutParams(0,-2,1f)); bar.addView(go); p.addView(bar)
        val actions=LinearLayout(this).apply { orientation=LinearLayout.HORIZONTAL }; actions.addView(button("تنزيل الرابط") { val u=url.text.toString().trim(); if(u.startsWith("https://")||u.startsWith("http://")) enqueue(u) else toast("حط رابط ملف مباشر") }); actions.addView(button("الرئيسية") { web?.loadUrl("https://m.youtube.com") }); p.addView(actions)
        web=WebView(this).apply { settings.javaScriptEnabled=true; settings.domStorageEnabled=true; settings.mediaPlaybackRequiresUserGesture=false; webViewClient=object:WebViewClient(){override fun onPageFinished(v:WebView?,u:String?){url.setText(u?:"")}}; webChromeClient=WebChromeClient(); setDownloadListener { u,_,_,_,_-> enqueue(u) }; loadUrl("https://m.youtube.com") }
        p.addView(web,LinearLayout.LayoutParams(-1,0,1f)); p.addView(label("التصفح لا يعني أن كل محتوى الموقع قابل للتنزيل. استخدم تنزيل الروابط المباشرة فقط.",12f))
    }
    private fun enqueue(link:String){ try { val uri=Uri.parse(link); val name=(uri.lastPathSegment?:"video.mp4").substringAfterLast('/').take(100).ifBlank{"media.mp4"}; val req=DownloadManager.Request(uri).setTitle(name).setDescription("RKEA Tube download").setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED).setAllowedOverMetered(true).setAllowedOverRoaming(false).setDestinationInExternalFilesDir(this,Environment.DIRECTORY_MOVIES,"RKEA Tube/$name"); dm.enqueue(req); toast("انضاف للتنزيلات") }catch(e:Exception){toast("تعذر بدء التنزيل: ${e.localizedMessage}")} }
    private fun showDownloads(){val p=page(); p.addView(label("التنزيلات",22f)); p.addView(button("تحديث الحالة") { showDownloads() }); val q=DownloadManager.Query(); val c=dm.query(q); if(c==null||!c.moveToFirst()){c?.close();p.addView(label("ماكو تنزيلات حالياً"));return}; do { val id=c.getLong(c.getColumnIndexOrThrow(DownloadManager.COLUMN_ID)); val title=c.getString(c.getColumnIndexOrThrow(DownloadManager.COLUMN_TITLE)); val status=c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS)); val done=c.getLong(c.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)); val total=c.getLong(c.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)); val stat=when(status){DownloadManager.STATUS_SUCCESSFUL->"اكتمل";DownloadManager.STATUS_FAILED->"فشل";DownloadManager.STATUS_PAUSED->"متوقف مؤقتاً";DownloadManager.STATUS_RUNNING->"ينزل";else->"بالانتظار"}; p.addView(label("$title\n$stat • ${if(total>0)"${done*100/total}%" else "حجم غير معروف"}",14f)); p.addView(button("إلغاء / حذف التنزيل") { dm.remove(id); showDownloads() }) }while(c.moveToNext()); c.close() }
    private fun mediaFiles():List<File>{ val d=File(getExternalFilesDir(Environment.DIRECTORY_MOVIES),"RKEA Tube"); return (d.listFiles()?.toList()?: emptyList()).filter{it.isFile&&it.extension.lowercase() in listOf("mp4","mkv","webm","mp3","m4a","aac","wav","ogg","3gp")}.sortedByDescending{it.lastModified()} }
    private fun showLibrary(){val p=page(); p.addView(label("المكتبة المحلية",22f)); val files=mediaFiles(); if(files.isEmpty())p.addView(label("ملفات RKEA Tube تظهر هنا بعد اكتمال تنزيلها.")); files.forEach{f->p.addView(button("▶  ${f.name}") { play(f) }); p.addView(button("مشاركة ${f.name}") { share(f) })} }
    private fun play(file:File){ if(!file.exists()){toast("الملف غير موجود");return}; playingFile=file; val p=page(); p.addView(label(file.name,15f)); player=ExoPlayer.Builder(this).build(); playerView=PlayerView(this).apply{this.player=player; setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS)}; p.addView(playerView,LinearLayout.LayoutParams(-1,0,1f)); player?.setMediaItem(MediaItem.fromUri(Uri.fromFile(file))); player?.prepare(); player?.play(); val controls=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL}; controls.addView(button("-10 ث") { player?.seekTo((player?.currentPosition?:0)-10000) }); controls.addView(button("تشغيل/إيقاف") { player?.let{if(it.isPlaying)it.pause()else it.play()} }); controls.addView(button("+10 ث") { player?.seekTo((player?.currentPosition?:0)+10000) }); p.addView(controls); val speeds=Spinner(this); speeds.adapter=ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,listOf("0.5x","0.75x","1x","1.25x","1.5x","2x")); speeds.setSelection(2); speeds.onItemSelectedListener=object:AdapterView.OnItemSelectedListener{override fun onNothingSelected(a:AdapterView<*>?){};override fun onItemSelected(a:AdapterView<*>?,v:View?,pos:Int,id:Long){player?.setPlaybackSpeed(listOf(.5f,.75f,1f,1.25f,1.5f,2f)[pos])}}; p.addView(speeds) }
    private fun share(f:File){try{val uri=FileProvider.getUriForFile(this,"$packageName.files",f);val i=Intent(Intent.ACTION_SEND).apply{type=when(f.extension.lowercase()){ "mp3","m4a","aac","wav","ogg"->"audio/*";else->"video/*"};putExtra(Intent.EXTRA_STREAM,uri);addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)};startActivity(Intent.createChooser(i,"مشاركة الملف"))}catch(e:Exception){toast("تعذرت المشاركة: ${e.localizedMessage}")}}
    private fun toast(s:String){Toast.makeText(this,s,Toast.LENGTH_LONG).show()}
    override fun onBackPressed(){ if(browserVisible&&web?.canGoBack()==true)web?.goBack() else super.onBackPressed() }
    override fun onDestroy(){ player?.release(); web?.destroy(); super.onDestroy() }
}
