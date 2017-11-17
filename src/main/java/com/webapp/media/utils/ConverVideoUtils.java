package com.webapp.media.utils;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.webapp.media.contant.Contants;

public class ConverVideoUtils {
	
	private Date date;
	private long beginTime;
	private String sourceVideoPath; //源视频路径
	private String fileRealName; // 文件名 不包含扩展名
	private String fileName; // 包含扩展名
	private String videoFolder = Contants.videoFolder; // 视频目录
	private String targetFolder = Contants.targetFolder; // flv视频目录
	private String ffmpegPath = Contants.ffmpegPath; // 
	private String mencoderPath = Contants.mencoderPath;
	private String imgRealPath = Contants.imageRealPath;
	
	
	public ConverVideoUtils() {
		super();
		// TODO Auto-generated constructor stub
	}

	
	public ConverVideoUtils(String path) {
		sourceVideoPath = path;
	}
	
	public String getPath() {
		return sourceVideoPath;
	}
	
	
	public void setPath(String path) {
		sourceVideoPath = path;
	}
	
	/**                                
	 * 
	 * @param targetExtension  目标视频扩展名
	 * @param isDelSourceFile 转换完成后是否删除源文件
	 * @return
	 */
	public boolean beginConver(String targetExtension, boolean isDelSourceFile) {
		
		File file = new File(sourceVideoPath);
		fileName = file.getName(); // 获取文件名
		fileRealName = fileName.substring(0, fileName.lastIndexOf(".")).toLowerCase();
		
		System.out.println("---接收到的文件： " + sourceVideoPath + " 需要转换-----------");
		
		if(!checkFile(sourceVideoPath)) {
			System.out.println(sourceVideoPath + "文件不存在");
			return false;
		}
		
		date = new Date();
		beginTime = date.getTime();
		
		System.out.println("开始转文件: " + sourceVideoPath + "=============" );
		
		if(process(targetExtension, isDelSourceFile)) {
			Date date2 = new Date();
			System.out.println("转换成功===========");
			long endTime = date2.getTime();
			long timeDis = (endTime - beginTime);
			
			String totalTime = String.valueOf(timeDis);
			
			System.out.println("===========转换格式共用了" + totalTime);
			
			// 截取图片
			if(processImg(sourceVideoPath)) {
				System.out.println("截取图片成功=========");
			} else {
				System.out.println("截取图片失败");
			}
			
			if(isDelSourceFile) {
				deleteFile(sourceVideoPath);
			}
			sourceVideoPath = null;
			return true;
		}
		return false;
	}


	private void deleteFile(String sourceVideoPath2) {
		// TODO Auto-generated method stub
		
	}


	private boolean processImg(String sourceVideoPath) {
		// TODO Auto-generated method stub
		if(!checkFile(sourceVideoPath)) {
			System.out.println("图片转换失败========");
			return false;
		}
		
		File file = new File(sourceVideoPath);
		fileName = file.getName();
		fileRealName = fileName.substring(0, fileName.lastIndexOf(".")).toLowerCase();
		
		List<String> commend = new ArrayList<String>();
		//第一帧： 00:00:01  
        //time ffmpeg -ss 00:00:01 -i test1.flv -f image2 -y test1.jpg  
		commend.add(ffmpegPath);
		commend.add("-ss");
		commend.add("00:00:01");
		commend.add("-i");
		commend.add(sourceVideoPath);
		commend.add("-f");
		commend.add("image2");
		commend.add("-y");
		commend.add(imgRealPath + fileRealName + ".jpg");
		
		try {
			ProcessBuilder builder = new ProcessBuilder();
			builder.command(commend);
			builder.start();
			return true;
		} catch (Exception e) {
			// TODO: handle exception
		}
		return false;
	}


	private boolean checkFile(String path) {
		// TODO Auto-generated method stub
		File file = new File(path);		
		return file.isFile() ? true : false;
	}
	
	/***
	 * 转换视频方法 
	 * @param targetExtension 目标视频扩展名
	 * @param isDelSourceFile 转换完成后是否删除源文件 
	 * @return
	 */
	private boolean process(String targetExtension, boolean isDelSourceFile) {
		
		int type = checkContentType();
		
		boolean flag = false;
		
		if(type == 0) {
			flag = processVideoFormat(sourceVideoPath, targetExtension, isDelSourceFile);
		} else if(type == 1) {
			// 其它文件转换成 avi , 然后再用 ffmpeg 转换成指定格式
			String aviFilePath = processAvi(type);
			
			if(null == aviFilePath) {
				System.out.println("========avi转换失败");
				return false;
			} else {
				System.out.println("开始转换成avi======");
				flag = processVideoFormat(aviFilePath, targetExtension, isDelSourceFile);
			}
		}
		return flag;
	}


	/**
	 * 对ffmpeg无法解析的文件格式(wmv9，rm，rmvb等), 可以先用别的工具（mencoder）转换为avi(ffmpeg能解析的)格式.
	 * @param type
	 * @return
	 */
	private String processAvi(int type) {
		// TODO Auto-generated method stub
		
		// 命令类型：mencoder 1.rmvb -oac mp3lame -lameopts preset=64 -ovc xvid  
        // -xvidencopts bitrate=600 -of avi -o rmvb.avi  
		List<String> commend = new ArrayList<String>();
		commend.add(mencoderPath);
		commend.add(sourceVideoPath);
		commend.add("-oac"); // 设置音频编码器
		commend.add("mp3lame"); // 设置音频编码器为 mp3lame 即 mp3
		commend.add("-lameopts"); // 设置 mp3lame 的相关参数
		// commend.add("cbr:br=32");  // 设置音频的码率为32
		commend.add("preset=64");
		commend.add("-ovc"); // 设置视频编码器
		/****
		 *  视频编码器名称
		 *  lavc  使用 libavcoder 中的一个视频编码器
		 *  xvid  xvid, MPEG-4 高级简单格式 (ASP) 编码器
		 *  x264  x264, MPEG-4 高级视频编码 (AVC), AKA H.264编码器
		 *  nuv   nuppel视频  为一些实时 程序所用
		 *  raw   未压缩的视频帧
		 *  copy  不要重新编码  只是复制已压缩的各帧
		 *  frameno  用于三通道编码(不推荐)
		 */
		commend.add("xvid");
		// -xvidencopts bitrate=600 视频编码率为 600kps
		commend.add("-xvidencopts"); 
		commend.add("bitrate=600");
		commend.add("-of");
		commend.add("avi");
		commend.add("-o");
		commend.add(videoFolder + fileRealName + ".avi");
		
		try {
			ProcessBuilder builder = new ProcessBuilder();
			builder.command(commend);
			Process p = builder.start();
			doWaitFor(p);
			p.destroy();
			return videoFolder + fileRealName + ".avi";
		} catch (Exception e) {
			// TODO: handle exception
		}
		return null;
	}


	/**
	 * 检查文件类型
	 * @return
	 */
	private int checkContentType () {
		String type = sourceVideoPath.substring(sourceVideoPath.lastIndexOf(".") + 1, 
				sourceVideoPath.length()).toLowerCase();
		
		if (type.equals("avi")) {
			return 0;
		} else if(type.equals("mpg")) {
			return 0;
		} else if(type.equals("wmv")) {
			return 0;
		} else if(type.equals("3gp")) {
			return 0;
		} else if(type.equals("mov")) {
			return 0;
		} else if(type.equals("mp4")) {
			return 0;
		} else if(type.equals("asf")) {
			return 0;
		} else if(type.equals("asx")) {
			return 0;
		} else if(type.equals("flv")) {
			return 0;
		} 
		// 对ffmpeg 无法解析的文件格式 (wmv9, rm, rmvb) 使用 mencoder 转换成 avi 后再使用 ffmpeg
		else if(type.equals("wmv9")) {
			return 1;
		}else if(type.equals("rm")) {
			return 1;
		}else if(type.equals("rmvb")) {
			return 1;
		} 
		return 9;
	}

	/**
	 *  转换指定格式
	 *  ffmpeg 能解析的格式：(asx, asf, mpg, wmv, 3gp, mp4, mov, avi, flv等)
	 * @param sourceVideoPath2
	 * @param targetExtension
	 * @param isDelSourceFile
	 * @return
	 */
	private boolean processVideoFormat(String oldFilePath, String targetExtension, boolean isDelSourceFile) {
		
		if(!checkFile(oldFilePath)) {
			System.out.println("===========" + oldFilePath + " 不是文件");
			return false;
		}
		
		// ffmpeg -i FILE_NAME.flv -ar 22050 NEW_FILE_NAME.mp4
		List<String> commend = new ArrayList<String>();
		commend.add(ffmpegPath);
		commend.add("-i");
		commend.add(oldFilePath);
		commend.add("-ar");
		commend.add("22050");
		commend.add(targetFolder + fileRealName + targetExtension);
		
		try {
			ProcessBuilder builder = new ProcessBuilder();
			String cmd = commend.toString();
			builder.command(commend);
			Process p = builder.start();
			doWaitFor(p);
			p.destroy();
			return true;
		} catch (Exception e) {
			// TODO: handle exception
		}
		return false;
	}


	private int doWaitFor(Process p) {
		// TODO Auto-generated method stub
		InputStream is = null;
		InputStream err = null;
		
		int exitValue = 1;
		
		try {
			System.out.println("开始=============");
			is = p.getInputStream();
			err = p.getErrorStream();
			
			boolean finished = false;
			
			while (!finished) {
				try {
					while(is.available() > 0) {
						Character c = new Character((char) is.read());
						System.out.println("============读取的字符： " + c);
					}
					
					while(err.available() > 0) {
						Character c = new Character((char) err.read());
						System.out.println("============读取的err字符： " + c);
					}
					
					exitValue = p.exitValue();
					finished = true;
				} catch (IllegalThreadStateException e) {
					// TODO: handle exception
					Thread.currentThread().sleep(500);
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			System.err.println("doWaitFor(): 出现的异步========" + e.getMessage());
		} finally {
			try {
				if (null != is) {
					is.close();
				}
				
				if (null != err) {
					err.close();
				}
			} catch (Exception e2) {
				// TODO: handle exception
			}
		}
		return exitValue;
	}
	
	
	
}
