package ai.turbochain.ipex.wallet.util;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.context.annotation.Configuration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;

@Configuration
public class HttpRequest {


    /**
     * 向指定 URL 发送POST方法的请求
     *
     * @param url
     *            发送请求的 URL
     * @param param
     *            请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
     * @return 所代表远程资源的响应结果
     */
    public static String sendPost(String url, Object param) {
        PrintWriter out = null;
        BufferedReader in = null;
        String result = "";
        try {
            URL realUrl = new URL(url);
            // 打开和URL之间的连接
            URLConnection conn = realUrl.openConnection();
            // 设置通用的请求属性
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
//            conn.setRequestProperty("Cookie",
//                "XXL_JOB_LOGIN_IDENTITY=6333303830376536353837616465323835626137616465396638383162336437");
//            conn.setRequestProperty("Cookie",
//                    "LOGIN_IDENTITY=61646d696e5f313233343536");
            conn.setConnectTimeout(60000);
            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
            // 获取URLConnection对象对应的输出流
            out = new PrintWriter(conn.getOutputStream());
            // 发送请求参数
            out.print(param);
            // flush输出流的缓冲
            out.flush();
            // 定义BufferedReader输入流来读取URL的响应
            in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //使用finally块来关闭输出流、输入流
        finally{
            try{
                if(out!=null){
                    out.close();
                }
                if(in!=null){
                    in.close();
                }
            }
            catch(IOException ex){
                ex.printStackTrace();
            }
        }
        return result;
    }

//    public static void main(String[] args) {
//        String str = sendGetData("https://blockchain.info/block-height/593323?format=json","");
//        System.out.println("str: " + str);
//    }

    /**
     * get请求传输数据
     *
     * @param url
     * @param encoding
     * @return
     * @throws ClientProtocolException
     * @throws IOException
     */
    public static String sendGetData(String url, String encoding) {
        String result = "";
        CloseableHttpResponse response = null;
        try {
            // 创建httpclient对象
            CloseableHttpClient httpClient = HttpClients.createDefault();
            // 创建get方式请求对象
            HttpGet httpGet = new HttpGet(url);
            httpGet.addHeader("Content-type", "application/json");
            // 通过请求对象获取响应对象
            response = httpClient.execute(httpGet);
            // 获取结果实体
            // 判断网络连接状态码是否正常(0--200都数正常)
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                encoding = StringUtils.isBlank(encoding) ? "utf-8" : encoding;
                result = EntityUtils.toString(response.getEntity(), encoding);
            }
            // 释放链接
            response.close();
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != response) {
                    response.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }
}
