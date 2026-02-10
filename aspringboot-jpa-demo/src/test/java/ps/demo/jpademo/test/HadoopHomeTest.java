package ps.demo.jpademo.test;

public class HadoopHomeTest {
    public static void main(String[] args) {
        System.setProperty("hadoop.home.dir", "D:\\hadoop"); // 可选：临时设置
        try {
            org.apache.hadoop.fs.FileSystem.get(new java.net.URI("file:///"), new org.apache.hadoop.conf.Configuration());
            System.out.println("Hadoop FileSystem loaded successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}