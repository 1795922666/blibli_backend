package com.zspt.blibli.common.utils;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SystemConstants {
    public static final String IMAGE_UPLOAD_DIR = "D:\\lesson\\nginx-1.18.0\\html\\hmdp\\imgs\\";
//    默认用户名前缀
    public static final String USER_NICK_NAME_PREFIX = "user_";
//    上传chunk默认名称
    public  static  final String CHUNK="chunk_";

    public static final int DEFAULT_PAGE_SIZE = 5;

    public static final int MAX_PAGE_SIZE = 10;
//    头像图片大小限制
    public static long  MAX_AVATAR_SIZE=5 * 1024 * 1024;


    public static final ExecutorService CACHE_REBuILD_EXECUTOR = Executors.newFixedThreadPool(10);
//    redis的文件上传key
public static final String REDIS_FILE_UPLOAD = "bli:file:upload:";
//    redis的视频key
    public static final String REDIS_VIDEO_RECOMMEND = "bli:video:recommend";
    public static final String REDIS_VIDEO_INFO = "bli:video:info:";
    public  static final  String REDIS_VIDEO_LIKE = "bli:video:like:";
    public static  final  String REDIS_VIDEO_COIN = "bli:video:coin:";
//    redis的用户key
    public static final String REDIS_USER_FOLLOW = "bli:user:%s:follow";
    public static final String REDIS_USER_COLLECT = "bli:user:%s:collect";
    public static final String REDIS_USER_INFO = "bli:user:%s:info";
}
