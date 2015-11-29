package com.lx.editor.ueditor.hunter;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.lx.editor.ueditor.PathFormat;
import com.lx.editor.ueditor.define.AppInfo;
import com.lx.editor.ueditor.define.BaseState;
import com.lx.editor.ueditor.define.MIMEType;
import com.lx.editor.ueditor.define.MultiState;
import com.lx.editor.ueditor.define.State;
import com.lx.editor.ueditor.upload.StorageManager;

/**
 * 图片抓取器
 * @author hancong03@baidu.com
 *
 */
public class ImageHunter {

	private String filename = null;
	private String savePath = null;
	private String rootPath = null;
	private List<String> allowTypes = null;
	private long maxSize = -1;
	
	private List<String> filters = null;
	
	public ImageHunter ( Map<String, Object> conf ) {
		
		this.filename = (String)conf.get( "filename" );
		this.savePath = (String)conf.get( "savePath" );
		this.rootPath = (String)conf.get( "rootPath" );
		this.maxSize = (Long)conf.get( "maxSize" );
		this.allowTypes = Arrays.asList( (String[])conf.get( "allowFiles" ) );
		this.filters = Arrays.asList( (String[])conf.get( "filter" ) );
		
	}
	
	public State capture (HttpServletRequest request,  String[] list ) {
		
		MultiState state = new MultiState( true );
		
		for ( String source : list ) {
			state.addState( captureRemoteData(request, source ) );
		}
		
		return state;
		
	}

	public State captureRemoteData (HttpServletRequest request,  String urlStr ) {
		
		HttpURLConnection connection = null;
		URL url = null;
		String suffix = null;
		
		try {
			url = new URL( urlStr );

			if ( !validHost( url.getHost() ) ) {
				return new BaseState( false, AppInfo.PREVENT_HOST );
			}
			
			connection = (HttpURLConnection) url.openConnection();
		
			connection.setInstanceFollowRedirects( true );
			connection.setUseCaches( true );
		
			if ( !validContentState( connection.getResponseCode() ) ) {
				return new BaseState( false, AppInfo.CONNECTION_ERROR );
			}
			
			suffix = MIMEType.getSuffix( connection.getContentType() );
			
			if ( !validFileType( suffix ) ) {
				return new BaseState( false, AppInfo.NOT_ALLOW_FILE_TYPE );
			}
			
			if ( !validFileSize( connection.getContentLength() ) ) {
				return new BaseState( false, AppInfo.MAX_SIZE );
			}
			
			String savePath = this.getPath( this.savePath, this.filename, suffix );
			String physicalPath = this.rootPath + savePath;

			State state = StorageManager.saveFileByInputStream( connection.getInputStream(), physicalPath );
			
			if ( state.isSuccess() ) {
				/**
				 * 修改源码：将上传后返回给页面显示图片的src路径改为绝对路径；
				 * 	如：	修改后：http://host:port/projectName/uploadPath/.../xxx.png，
				 * 		修改前：/uploadPath/.../xxx.png
				 */
//				state.putInfo( "url", PathFormat.format( savePath ) );
				state.putInfo( "url", PathFormat.getResourceAddress(request, PathFormat.format(savePath)) );
				state.putInfo( "source", urlStr );
			}
			
			return state;
			
		} catch ( Exception e ) {
			return new BaseState( false, AppInfo.REMOTE_FAIL );
		}
		
	}
	
	private String getPath ( String savePath, String filename, String suffix  ) {
		
		return PathFormat.parse( savePath + suffix, filename );
		
	}
	
	private boolean validHost ( String hostname ) {
		
		return !filters.contains( hostname );
		
	}
	
	private boolean validContentState ( int code ) {
		
		return HttpURLConnection.HTTP_OK == code;
		
	}
	
	private boolean validFileType ( String type ) {
		
		return this.allowTypes.contains( type );
		
	}
	
	private boolean validFileSize ( int size ) {
		return size < this.maxSize;
	}
	
}
