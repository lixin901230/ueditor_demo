package com.lx.editor.ueditor.hunter;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FileUtils;

import com.lx.editor.ueditor.PathFormat;
import com.lx.editor.ueditor.define.AppInfo;
import com.lx.editor.ueditor.define.BaseState;
import com.lx.editor.ueditor.define.MultiState;
import com.lx.editor.ueditor.define.State;

public class FileManager {

	private String dir = null;
	private String rootPath = null;
	private String[] allowFiles = null;
	private int count = 0;
	
	public FileManager ( Map<String, Object> conf ) {

		this.rootPath = (String)conf.get( "rootPath" );
		this.dir = this.rootPath + (String)conf.get( "dir" );
		this.allowFiles = this.getAllowFiles( conf.get("allowFiles") );
		this.count = (Integer)conf.get( "count" );
		
	}
	
//	public State listFile ( int index ) {
	public State listFile (HttpServletRequest request, int index ) {
		
		File dir = new File( this.dir );
		State state = null;

		if ( !dir.exists() ) {
			return new BaseState( false, AppInfo.NOT_EXIST );
		}
		
		if ( !dir.isDirectory() ) {
			return new BaseState( false, AppInfo.NOT_DIRECTORY );
		}
		
		Collection<File> list = FileUtils.listFiles( dir, this.allowFiles, true );
		
		if ( index < 0 || index > list.size() ) {
			state = new MultiState( true );
		} else {
			Object[] fileList = Arrays.copyOfRange( list.toArray(), index, index + this.count );
//			state = this.getState( fileList );
			state = this.getState(request, fileList );
		}
		
		state.putInfo( "start", index );
		state.putInfo( "total", list.size() );
		
		return state;
		
	}
	
//	private State getState ( Object[] files ) {
	private State getState (HttpServletRequest request, Object[] files ) {
		
		MultiState state = new MultiState( true );
		BaseState fileState = null;
		
		File file = null;
		
		for ( Object obj : files ) {
			if ( obj == null ) {
				break;
			}
			file = (File)obj;
			fileState = new BaseState( true );
			/**
			 * 修改源码：将文件路径拼成如下形式
			 * 	如：	修改后：http://host:port/projectName/uploadPath/.../fileName.xxx，
			 * 		修改前：/uploadPath/.../fileName.xxx
			 */
//			fileState.putInfo( "url", PathFormat.format( this.getPath( file ) ) );
			fileState.putInfo( "url", PathFormat.getResourceAddress(request, PathFormat.format(this.getPath( file ))) );
			state.addState( fileState );
		}
		
		return state;
		
	}
	
	private String getPath ( File file ) {
		
		//修复官方jar包bug
//		String path = file.getAbsolutePath();
		String path = PathFormat.format(file.getAbsolutePath());
		return path.replace( this.rootPath, "/" );
		
	}
	
	private String[] getAllowFiles ( Object fileExt ) {
		
		String[] exts = null;
		String ext = null;
		
		if ( fileExt == null ) {
			return new String[ 0 ];
		}
		
		exts = (String[])fileExt;
		
		for ( int i = 0, len = exts.length; i < len; i++ ) {
			
			ext = exts[ i ];
			exts[ i ] = ext.replace( ".", "" );
			
		}
		
		return exts;
		
	}
	
}
