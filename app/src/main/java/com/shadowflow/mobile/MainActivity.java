package com.shadowflow.mobile;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.net.Uri;
import android.webkit.ValueCallback;
import androidx.webkit.WebViewAssetLoader;
import androidx.webkit.WebViewClientCompat;
import java.util.Arrays;

public class MainActivity extends Activity {
 private WebView web;
 private ValueCallback<Uri[]> files;
 private PermissionRequest micRequest;
 private static final int FILE_REQUEST = 1302, MIC_REQUEST = 1303;
 private WebViewAssetLoader assetLoader;
 @Override public void onCreate(Bundle state){ super.onCreate(state);
  web = new WebView(this);setContentView(web);
  assetLoader = new WebViewAssetLoader.Builder().addPathHandler("/assets/",new WebViewAssetLoader.AssetsPathHandler(this)).build();
  WebSettings s=web.getSettings();s.setJavaScriptEnabled(true);s.setDomStorageEnabled(true);
  s.setMediaPlaybackRequiresUserGesture(false);s.setAllowFileAccess(false);s.setAllowContentAccess(true);
  web.setWebViewClient(new WebViewClientCompat(){
   @Override public android.webkit.WebResourceResponse shouldInterceptRequest(WebView v, android.webkit.WebResourceRequest r){return assetLoader.shouldInterceptRequest(r.getUrl());}
   @Override public boolean shouldOverrideUrlLoading(WebView v, android.webkit.WebResourceRequest r){
    String url=r.getUrl().toString();
    if(url.startsWith("https://appassets.androidplatform.net/"))return false;
    // External links are opened outside the learning app; embedded YouTube stays in the web player.
    if(r.isForMainFrame()){try{startActivity(new Intent(Intent.ACTION_VIEW,r.getUrl()));}catch(Exception e){}return true;}
    return false;
   }
  });
  web.setWebChromeClient(new WebChromeClient(){
   @Override public boolean onShowFileChooser(WebView w, ValueCallback<Uri[]> callback, FileChooserParams params){
    if(files!=null)files.onReceiveValue(null);files=callback;
    try { Intent i=new Intent(Intent.ACTION_GET_CONTENT);i.addCategory(Intent.CATEGORY_OPENABLE);
     i.setType("*/*");i.putExtra(Intent.EXTRA_MIME_TYPES,new String[]{"audio/*","video/*","text/plain","application/x-subrip","application/json"});
     startActivityForResult(Intent.createChooser(i,"파일 선택"),FILE_REQUEST);
    }catch(Exception e){ files.onReceiveValue(null);files=null; }
    return true;
   }
   @Override public void onPermissionRequest(PermissionRequest request){
    runOnUiThread(()->{boolean audio=Arrays.asList(request.getResources()).contains(PermissionRequest.RESOURCE_AUDIO_CAPTURE);
     if(!audio){request.deny();return;}
     if(checkSelfPermission(Manifest.permission.RECORD_AUDIO)==PackageManager.PERMISSION_GRANTED){request.grant(new String[]{PermissionRequest.RESOURCE_AUDIO_CAPTURE});}
     else{micRequest=request;requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},MIC_REQUEST);}
    });
   }
   @Override public void onPermissionRequestCanceled(PermissionRequest request){if(micRequest==request)micRequest=null;}
  });
  web.loadUrl("https://appassets.androidplatform.net/assets/index.html");
 }
 @Override protected void onActivityResult(int code,int result,Intent data){super.onActivityResult(code,result,data);
  if(code==FILE_REQUEST&&files!=null){Uri[] out=null;if(result==RESULT_OK&&data!=null&&data.getData()!=null)out=new Uri[]{data.getData()};files.onReceiveValue(out);files=null;}
 }
 @Override public void onRequestPermissionsResult(int code,String[] perms,int[] grants){super.onRequestPermissionsResult(code,perms,grants);
  if(code==MIC_REQUEST&&micRequest!=null){if(grants.length>0&&grants[0]==PackageManager.PERMISSION_GRANTED)micRequest.grant(new String[]{PermissionRequest.RESOURCE_AUDIO_CAPTURE});else micRequest.deny();micRequest=null;}
 }
 @Override public void onBackPressed(){if(web.canGoBack())web.goBack();else super.onBackPressed();}
 @Override protected void onDestroy(){if(web!=null){web.destroy();web=null;}super.onDestroy();}
}
