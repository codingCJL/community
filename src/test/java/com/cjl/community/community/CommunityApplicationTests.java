package com.cjl.community.community;

import com.cjl.community.community.dao.DiscussPostMapper;
import com.cjl.community.community.dao.LoginTicketMapper;
import com.cjl.community.community.dao.MessageMapper;
import com.cjl.community.community.dao.UserMapper;
import com.cjl.community.community.dao.elasticsearch.DiscussPostRepository;
import com.cjl.community.community.entity.DiscussPost;
import com.cjl.community.community.entity.LoginTicket;
import com.cjl.community.community.entity.User;
import com.cjl.community.community.util.MailClient;
import com.cjl.community.community.util.SensitiveFilter;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
class CommunityApplicationTests implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private DiscussPostMapper discussPostMapper;
    @Autowired
    private LoginTicketMapper loginTicketMapper;
    @Autowired
    private MailClient mailClient;
    @Autowired
    private TemplateEngine templateEngine;
    @Autowired
    private SensitiveFilter sensitiveFilter;
    @Autowired
    private MessageMapper messageMapper;
    @Autowired
    private RedisTemplate redisTemplate;
    @Test
    void contextLoads() {
        //userMapper.updateHeader(1,"https");
        //System.out.println(userMapper.selectById(1));
        /*for (int i = 1; i < 10; i++) {
            User user=new User();
            user.setUsername("user"+ i);
            user.setPassword("asd");
            user.setSalt("salt");
            user.setEmail("asd@aa.com");
            user.setHeaderUrl("https://unsplash.it/500/500?image="+i);
            user.setActivationCode("aaa");
            user.setCreateTime(new Date());
            userMapper.insertUser(user);
        }*/
        /*List<DiscussPost> discussPosts = discussPostMapper.selectDiscussPosts(0, 0, 10);
        for(DiscussPost d:discussPosts){
            System.out.println(d);
        }*/
        //测试邮件
        //mailClient.sendMail("983196361@qq.com","html测试邮件","<a href='www.baidu.com'>百度</a>");

        //System.out.println(mailClient.from);
        /*Context context=new Context();
        context.setVariable("username","cjl");
        String content=templateEngine.process("/mail/activation",context);
        System.out.println(content);
        mailClient.sendMail("983196361@qq.com","html测试邮件",content);*/
        /*LoginTicket loginTicket=new LoginTicket();
        loginTicket.setTicket("asd");
        loginTicket.setUserId(14);
        loginTicket.setExpired(new Date());
        loginTicket.setStatus(0);
        loginTicketMapper.insertLoginTicket(loginTicket);*/
        //String ticket="asd";
        //System.out.println(loginTicketMapper.selectByTicket("asd"));
        //loginTicketMapper.updateStatusByTicket(ticket,1);
        //System.out.println(sensitiveFilter.filter("傻 逼哈赌 博哈aa"));
        System.out.println(messageMapper.selectConversations(111, 0, 20));
        System.out.println(messageMapper.selectConversationCount(111));
        System.out.println(messageMapper.selectLetters("111_112", 0, 10));
        System.out.println(messageMapper.selectLetterCount("111_112"));
        System.out.println(messageMapper.selectLetterUnread(111, "111_112"));
    }

    @Test
    void redisString() {
        String key="age";
        redisTemplate.opsForValue().set(key,22);
        System.out.println(redisTemplate.opsForValue().increment(key));
    }
    @Test
    void redisHash() {
        String key="test:user";
        redisTemplate.opsForHash().put(key,"id",1);
        redisTemplate.opsForHash().put(key,"username","cjl");
        System.out.println(redisTemplate.opsForHash().get(key,"id"));
        System.out.println(redisTemplate.opsForHash().get(key,"username"));
    }
    @Test
    void redisList() {
        String key="test:ids";
        redisTemplate.opsForList().leftPush(key,101);
        redisTemplate.opsForList().leftPush(key,102);
        redisTemplate.opsForList().leftPush(key,103);
        System.out.println(redisTemplate.opsForList().size(key));
        System.out.println(redisTemplate.opsForList().index(key,0));
        System.out.println(redisTemplate.opsForList().range(key,0,2));
        System.out.println(redisTemplate.opsForList().leftPop(key));
        System.out.println(redisTemplate.opsForList().leftPop(key));
        System.out.println(redisTemplate.opsForList().leftPop(key));
    }
    @Test
    void redisSet() {
        String key="test:teachers";
        redisTemplate.opsForSet().add(key,"刘备","关羽","张飞","cjl");
        System.out.println(redisTemplate.opsForSet().size(key));
        System.out.println(redisTemplate.opsForSet().pop(key));
        System.out.println(redisTemplate.opsForSet().members(key));

    }
    @Test
    void redisSortSet() {
        String key="test:students";
        redisTemplate.opsForZSet().add(key,"唐僧",80);
        redisTemplate.opsForZSet().add(key,"悟空",70);
        redisTemplate.opsForZSet().add(key,"八戒",50);
        redisTemplate.opsForZSet().add(key,"白龙马",30);
        System.out.println(redisTemplate.opsForZSet().zCard(key));
        System.out.println(redisTemplate.opsForZSet().score(key,"悟空"));
        System.out.println(redisTemplate.opsForZSet().rank(key,"悟空"));
        System.out.println(redisTemplate.opsForZSet().reverseRank(key,"悟空"));
        System.out.println(redisTemplate.opsForZSet().range(key,0,2));

    }
    @Test
    void testKeys() {
        redisTemplate.delete("test:user");
        redisTemplate.hasKey("test:user");
        redisTemplate.expire("test:students",5, TimeUnit.SECONDS);
    }
    //多次访问同一个key
    @Test
    void testKey() {
        String key="age";
        BoundValueOperations operations=redisTemplate.boundValueOps(key);
        operations.increment();
        operations.increment();
        operations.increment();
        System.out.println(operations.get());
    }
    //redis高级数据类型，HyperLogLog
    //统计20万个重复数据的独立总数
    @Test
    void testHyperLogLog() {
        String key="test:HLL01";
        for (int i = 1; i <=100000; i++) {
            redisTemplate.opsForHyperLogLog().add(key,i);
        }
        for (int i = 1; i <= 100000; i++) {
            int r=(int)(Math.random()*100000+1);
            redisTemplate.opsForHyperLogLog().add(key,r);
        }
        Long size = redisTemplate.opsForHyperLogLog().size(key);
        System.out.println(size);


    }
    //将三组数据合并，再统计合并后的重复数据的独立总数
    @Test
    void testHyperLogLogUnion() {
        String key1="test:HLL02";
        for (int i = 1; i <=10000; i++) {
            redisTemplate.opsForHyperLogLog().add(key1,i);
        }
        String key2="test:HLL03";
        for (int i = 5001; i <=15000; i++) {
            redisTemplate.opsForHyperLogLog().add(key2,i);
        }
        String key3="test:HLL04";
        for (int i = 10001; i <=20000; i++) {
            redisTemplate.opsForHyperLogLog().add(key3,i);
        }
        String unionKey="test:HLL:union";
        redisTemplate.opsForHyperLogLog().union(unionKey,key1,key2,key3);
        Long size = redisTemplate.opsForHyperLogLog().size(unionKey);
        System.out.println(size);
    }

    //redis高级数据类型，bitmap
    //统计一组数据的布尔值
    @Test
    void testBitmap() {
        String key="test:bm01";
        //记录
        redisTemplate.opsForValue().setBit(key,1,true);
        redisTemplate.opsForValue().setBit(key,4,true);
        redisTemplate.opsForValue().setBit(key,7,true);
        //查询
        System.out.println(redisTemplate.opsForValue().getBit(key, 0));
        System.out.println(redisTemplate.opsForValue().getBit(key, 2));
        System.out.println(redisTemplate.opsForValue().getBit(key, 7));
        //统计
        Object obj=redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection redisConnection) throws DataAccessException {
                return redisConnection.bitCount(key.getBytes());
            }
        });
        System.out.println(obj);
    }

    @Autowired
    private KafkaProducer kafkaProducer;

    @Autowired
    private KafkaConsumer kafkaConsumer;
    @Test
    void testKafka() {
        kafkaProducer.sendMessage("test","wsw");
        kafkaProducer.sendMessage("test","哈哈");

    }
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext=applicationContext;
    }



    @Autowired
    private DiscussPostRepository discussPostRepository;

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;
    @Test
    void testES() {
        SearchQuery searchQuery=new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery("java","titile","content"))
                .withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                .withPageable(PageRequest.of(0,10))
                .withHighlightFields(
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
                ).build();
        Page<DiscussPost> postPage = discussPostRepository.search(searchQuery);
        System.out.println(postPage.getTotalElements());
        System.out.println(postPage.getTotalPages());
        System.out.println(postPage.getNumber());
        System.out.println(postPage.getSize());
        for(DiscussPost post:postPage){
            System.out.println(post);
        }
        //System.out.println(discussPostRepository.findById(231));
        /*discussPostRepository.deleteAll();
        discussPostRepository.saveAll(discussPostMapper.selectDiscussAllPosts());*/
    }
    @Test
    void ElasticsearchTemplate(){
        SearchQuery searchQuery=new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery("阿里","titile","content"))
                .withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                .withPageable(PageRequest.of(0,10))
                .withHighlightFields(
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
                ).build();
        Page<DiscussPost> postPage=elasticsearchTemplate.queryForPage(searchQuery, DiscussPost.class, new SearchResultMapper() {
            @Override
            public <T> AggregatedPage<T> mapResults(SearchResponse searchResponse, Class<T> aClass, Pageable pageable) {
                SearchHits hits = searchResponse.getHits();
                if(hits.totalHits<=0){
                    return null;
                }
                List<DiscussPost> list=new ArrayList<>();
                for(SearchHit hit:hits){
                    DiscussPost post=new DiscussPost();
                    String id = hit.getSourceAsMap().get("id").toString();
                    post.setId(Integer.valueOf(id));

                    String userId = hit.getSourceAsMap().get("userId").toString();
                    post.setUserId(Integer.valueOf(userId));

                    String title = hit.getSourceAsMap().get("title").toString();
                    post.setTitle(title);

                    String content = hit.getSourceAsMap().get("content").toString();
                    post.setContent(content);

                    String status = hit.getSourceAsMap().get("status").toString();
                    post.setStatus(Integer.valueOf(status));

                    String createTime = hit.getSourceAsMap().get("createTime").toString();
                    post.setCreateTime(new Date(Long.valueOf(createTime)));

                    String commentCount = hit.getSourceAsMap().get("commentCount").toString();
                    post.setCommentCount(Integer.valueOf(commentCount));

                    // 处理高亮显示的结果
                    HighlightField titleField = hit.getHighlightFields().get("title");
                    if (titleField != null) {
                        post.setTitle(titleField.getFragments()[0].toString());
                    }

                    HighlightField contentField = hit.getHighlightFields().get("content");
                    if (contentField != null) {
                        post.setContent(contentField.getFragments()[0].toString());
                    }

                    list.add(post);

                }
                return new AggregatedPageImpl(list, pageable,
                        hits.getTotalHits(), searchResponse.getAggregations(), searchResponse.getScrollId(), hits.getMaxScore());
            }

            @Override
            public <T> T mapSearchHit(SearchHit searchHit, Class<T> aClass) {
                return null;
            }

        });
        System.out.println(postPage.getTotalElements());
        System.out.println(postPage.getTotalPages());
        System.out.println(postPage.getNumber());
        System.out.println(postPage.getSize());
        for(DiscussPost post:postPage){
            System.out.println(post);
        }
    }
}
@Component
class KafkaProducer{

    @Autowired
    private KafkaTemplate kafkaTemplate;

    public void sendMessage(String topic,String content){
        kafkaTemplate.send(topic,content);
        System.out.println("发送了消息："+content);
    }
}
@Component
class KafkaConsumer{

    @KafkaListener(topics = {"test"})
    public void handleMessage(ConsumerRecord record){
        System.out.println("收到消息："+record.value());
    }

}

