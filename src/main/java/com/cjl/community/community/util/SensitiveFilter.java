package com.cjl.community.community.util;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * @author cjl
 * @date 2020/4/13 11:13
 * 敏感词过滤
 */
@Component
@Slf4j
public class SensitiveFilter {


    //替换敏感词的符号
    private static final String REPLACEMENT="***";

    //根节点
    private TrieNode rootNode=new TrieNode();


    //判断是否为符号
    private boolean isSymbol(Character c){
        return CharUtils.isAsciiAlphanumeric(c)&&(c<0x2E80||c>0x9FFF);
    }
    //在构造方法之后自动调用
    @PostConstruct
    public void init(){
        try {
            InputStream is=this.getClass().getClassLoader().getResourceAsStream("sensitiveWords.txt");
            BufferedReader reader=new BufferedReader(new InputStreamReader(is));
            String keyword;
            while ((keyword=reader.readLine())!=null){
                //添加到前缀树
                this.addKeyword(keyword);
            }
        } catch (Exception e) {
            log.error("加载敏感词文件失败："+e.getMessage());
        }



    }

    //将一个敏感词添加到前缀树中
    private void addKeyword(String keyword){
        TrieNode tempNode=rootNode;
        for (int i = 0; i < keyword.length(); i++) {
            char c=keyword.charAt(i);
            TrieNode subNode = tempNode.getSubNode(c);
            if(subNode==null){
                //初始化子节点
                subNode=new TrieNode();
                tempNode.addSubNode(c,subNode);
            }
            //指向子节点，进入下一轮循环
            tempNode=subNode;

            //设置结束标识
            if(i==keyword.length()-1){
                tempNode.setKeywordEnd(true);
            }
        }
    }


    /**
     * 过滤敏感词
     * @param text 带敏感词的文本
     * @return 返回过滤后的文本
     */
    public String filter(String text){
        if(StringUtils.isBlank(text)){
            return null;
        }
        //指针1，指向树
        TrieNode tempNode=rootNode;
        //指针2，指向字符串的首位
        int begin=0;
        //指针2，指向字符串的末位
        int position=0;
        //保存结果
        StringBuilder sb=new StringBuilder();

        while (position<text.length()){
            char c=text.charAt(position);
            //是符号
            if(isSymbol(c)){
                //若指针1处于根节点，将此符号计入结果，让指针2向下一步
                if(tempNode==rootNode){
                    sb.append(c);
                    begin++;
                }
                //无论符号在开头或中间，指针三都向下一步
                position++;
                continue;
            }
            //不是符号
            //检查下级节点
            tempNode = tempNode.getSubNode(c);
            if(tempNode==null){
                //以begin为开头的字符串不是敏感词
                sb.append(text.charAt(begin));
                //进入下一个位置
                position=++begin;
                //重新指向根节点
                tempNode=rootNode;
            }else if(tempNode.isKeywordEnd){
                //发现敏感词，即begin和position之间的字符为敏感词
                sb.append(REPLACEMENT);
                //进入下一个位置
                begin=++position;
                //重新指向根节点
                tempNode=rootNode;
            }else {
                //检查下一个字符
                position++;
            }
        }

        //将最后一批字符计入结果
        sb.append(text.substring(begin));
        return sb.toString();
    }




    /**
     * 前缀树结构
     */
    @Data
    private class TrieNode{
        //敏感词词结束的标识
        private boolean isKeywordEnd=false;

        //子节点(key是下级字符，value是下级节点)
        private Map<Character,TrieNode> subNode=new HashMap<>();

        //添加子节点
        public void addSubNode(Character c,TrieNode node){
            subNode.put(c,node);
        }
        //获取子节点
        public TrieNode getSubNode(Character c){
            return subNode.get(c);
        }

    }
}