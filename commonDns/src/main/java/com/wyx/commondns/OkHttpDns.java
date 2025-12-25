package com.wyx.commondns;

import android.util.Log;

import androidx.annotation.NonNull;

import com.alibaba.pdns.DNSResolver;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Dns;

public class OkHttpDns implements Dns {

    private static OkHttpDns instance;
    private static Object lock = new Object();
    private DNSResolver mDNSResolver = DNSResolver.getInstance();

    private OkHttpDns() {
    }

    public static OkHttpDns getInstance() {
        if (null == instance) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new OkHttpDns();
                }
            }
        }
        return instance;
    }

    @Override
    public List<InetAddress> lookup(@NonNull String hostname) throws UnknownHostException {
        //调用移动解析HTTPDNS Android SDK提供API进行域名解析
        String[] IPArray = mDNSResolver.getIpv4ByHostFromCache(hostname,true);
        if (IPArray == null || IPArray.length == 0){
            IPArray = mDNSResolver.getIPsV4ByHost(hostname);
        }
        if (IPArray != null && IPArray.length > 0) {
            List<InetAddress> inetAddresses = new ArrayList<>();
            InetAddress address;
            for (String ip : IPArray) {
                address = InetAddress.getByName(ip);
                inetAddresses.add(address);
            }
            if (!inetAddresses.isEmpty()) {
                return inetAddresses;
            }
        }
        //如果返回null，走系统DNS服务解析域名
        return okhttp3.Dns.SYSTEM.lookup(hostname);
    }
}
