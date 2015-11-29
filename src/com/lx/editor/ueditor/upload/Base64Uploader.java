package com.lx.editor.ueditor.upload;

import com.lx.editor.ueditor.PathFormat;
import com.lx.editor.ueditor.define.AppInfo;
import com.lx.editor.ueditor.define.BaseState;
import com.lx.editor.ueditor.define.FileType;
import com.lx.editor.ueditor.define.State;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Base64;

public final class Base64Uploader {

	public static State save(HttpServletRequest request, String content, Map<String, Object> conf) {
		
		byte[] data = decode(content);

		long maxSize = ((Long) conf.get("maxSize")).longValue();

		if (!validSize(data, maxSize)) {
			return new BaseState(false, AppInfo.MAX_SIZE);
		}

		String suffix = FileType.getSuffix("JPG");

		String savePath = PathFormat.parse((String) conf.get("savePath"),
				(String) conf.get("filename"));
		
		savePath = savePath + suffix;
		String physicalPath = (String) conf.get("rootPath") + savePath;

		State storageState = StorageManager.saveBinaryFile(data, physicalPath);

		if (storageState.isSuccess()) {
			/**
			 * 修改源码：将上传后返回给页面显示图片的src路径改为绝对路径；
			 * 	如：	修改后：http://host:port/projectName/uploadPath/.../xxx.png，
			 * 		修改前：/uploadPath/.../xxx.png
			 */
//			storageState.putInfo("url", PathFormat.format(savePath));
			storageState.putInfo("url", PathFormat.getResourceAddress(request, PathFormat.format(savePath)));
			storageState.putInfo("type", suffix);
			storageState.putInfo("original", "");
		}

		return storageState;
	}

	private static byte[] decode(String content) {
		return Base64.decodeBase64(content);
	}

	private static boolean validSize(byte[] data, long length) {
		return data.length <= length;
	}
	
}