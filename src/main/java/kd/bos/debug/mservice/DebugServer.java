package kd.bos.debug.mservice;

import kd.bos.config.client.util.ConfigUtils;
import kd.bos.service.webserver.JettyServer;


public class DebugServer {


    // **********请根据文档维护以下信息********* //
    private static final String CLUSTER_NAME = "digi";
    private static final String TENANT_CODE = "digi";
    //debug启动之后，根据cloudIDE生成的地址，填入到该处后，再重启
    private static final String DOMAIN_IERP = "http://localhost:8080/ierp";
    // **********维护信息结束*************** //

    // 苍穹开发环境所在机器的端口,如端口(8080)冲突请修改
    private static final String HOST_COSMIC_PORT = "8080";


    public static void main(String[] args) throws Exception {
        // 输出sql日志
        System.setProperty("db.sql.out", "true");
        System.setProperty(ConfigUtils.APP_NAME_KEY, "mservice-biz2.0-cosmic");

        setProperties();
        JettyServer.main(null);
    }

    /**
     * 配置公共苍穹启动信息
     */
    private static void setProperties() {

        // 配置信息：参加追光者大赛的伙伴不需要做修改
        final String DOMAIN_URL = "http://digi.tpddns.cn:18090/";
        final String REDIS = "digi.tpddns.cn:6379/digi@20201030";
        final String RABBITMQ = "digi.tpddns.cn:5672";
        final String ZOOKEEPER = "digi.tpddns.cn:2181";
        final String RABBITMQ_USER = "admin";
        final String RABBITMQ_PWD = "digi@20201030";

        // 配置信息：参加追光者大赛的伙伴不需要做修改
        /*final String DOMAIN_URL = "http://121.36.224.94:8090";
        final String REDIS = "121.36.224.94:6379/digi@20201022";
        final String RABBITMQ = "121.36.224.94:5672";
        final String ZOOKEEPER = "121.36.224.94:2181";
        final String RABBITMQ_USER = "admin";
        final String RABBITMQ_PWD = "digi@20201022";*/

        System.setProperty("JETTY_WEBAPP_PATH", "../devops/mservice-cosmic/webapp");
        System.setProperty("JETTY_WEBRES_PATH", "../devops/static-file-service");

        // 设置集群环境名称和配置服务器地址
        System.setProperty(ConfigUtils.CLUSTER_NAME_KEY, CLUSTER_NAME);
        System.setProperty("domain.tenantCode", TENANT_CODE);
        System.setProperty(ConfigUtils.CONFIG_URL_KEY, ZOOKEEPER);
        System.setProperty("Schedule.zk.server", ZOOKEEPER);
        System.setProperty("monitor.redis", REDIS); // redis配置
        System.setProperty("redis.serversForCache", REDIS);
        System.setProperty("redis.serversForSession", REDIS);
        System.setProperty("algo.storage.redis.url", REDIS);


        String[] rabbitInfo = RABBITMQ.split(":");
        StringBuffer buffer = new StringBuffer();
        buffer.append("host=").append(rabbitInfo[0]).append(System.getProperty("line.separator")).append("port=").append(rabbitInfo[1])
                .append(System.getProperty("line.separator")).append("user=").append(RABBITMQ_USER)
                .append(System.getProperty("line.separator")).append("password=").append(RABBITMQ_PWD)
                .append(System.getProperty("line.separator")).append("vhost=mc")
                .append(System.getProperty("line.separator")).append("type=rabbitmq");
        System.setProperty("mq.server", buffer.toString()); // rabbitmq配置

        System.setProperty("trace.reporter.type", "");
        System.setProperty("configAppName", "mservice,web");
        System.setProperty("webmserviceinone", "true");

        System.setProperty("file.encoding", "utf-8");
        System.setProperty("xdb.enable", "false");

        System.setProperty("mq.consumer.register", "false");
        // 区别与其他MQ消费者，调度任务
//        System.setProperty("mq.debug.queue.tag", "15892232964");
        System.setProperty("MONITOR_HTTP_PORT", "9998");
        System.setProperty("JMX_HTTP_PORT", "9091");
        System.setProperty("dubbo.protocol.port", "28888");
        System.setProperty("dubbo.consumer.url", "dubbo://localhost:28888");
        System.setProperty("dubbo.consumer.url.qing", "dubbo://localhost:30880");
        System.setProperty("dubbo.registry.register", "false");

        System.setProperty("dubbo.service.lookup.local", "false");
        System.setProperty("appSplit", "false");

        System.setProperty("JETTY_WEB_PORT", HOST_COSMIC_PORT);

        System.setProperty("tenant.code.type", "config");
        System.setProperty("bos.app.special.deployalone.ids", "");

        System.setProperty("fileserver", DOMAIN_URL + "/fileserver");
        System.setProperty("imageServer.url", DOMAIN_URL + "/fileserver");

        System.setProperty("mc.server.url", DOMAIN_URL + "/mc");
        System.setProperty("domain.contextUrl", DOMAIN_IERP);
    }

}
