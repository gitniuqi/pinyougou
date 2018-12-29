package test;

import org.csource.common.MyException;
import org.csource.fastdfs.*;
import org.junit.Test;

import java.io.IOException;

/**
 * 这个是fdfs_Client测试类
 */
public class Myfdfs_client {
    @Test
    public void fun1() throws IOException, MyException {
        //1加载配置文件
        ClientGlobal.init("E:\\pinyougoCode\\pinyougou-parent\\pinyougou-shop-web\\src\\main\\resources\\fdfs_client.config");
        //2创建trackerClient对象
        TrackerClient trackerClient = new TrackerClient();
        // 3、使用TrackerClient对象创建连接，获得一个TrackerServer对象。
        TrackerServer trackerServer = trackerClient.getConnection();
        // 4、创建一个StorageServer的引用，值为 null
        StorageServer storageServer = null;
        // 5、创建一个StorageClient对象，需要两个参数TrackerServer对象、StorageServer的引用
        StorageClient storageClient = new StorageClient(trackerServer, storageServer);
        // 6、使用StorageClient对象上传图片。
        //扩展名不带“.”
        String[] strings = storageClient.upload_file("C:\\Users\\唐聪\\Pictures\\Paris (@yiyannyang) · Instagram 照片和视频_files\\28436309_226544314574564_869859649149468672_n.jpg", "jpg",
                null);
        // 7、返回数组。包含组名和图片的路径。
        for (String string : strings) {
            System.out.println(string);
        }

    }
}
