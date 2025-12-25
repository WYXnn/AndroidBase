package com.wyx.commondns

import android.content.Context
import com.alibaba.pdns.DNSResolver

object DNSManager {

    fun init(context: Context, accountId : String, accessId : String, accessSecret : String) {
        DNSResolver.Init(context,accountId, accessId, accessSecret)
    }

    fun setKeepAliveDomains(domains : Array<String>) {
        DNSResolver.setKeepAliveDomains(domains)
    }

    fun preLoadDomains(domains : Array<String>) {
        DNSResolver.getInstance().preLoadDomains(DNSResolver.QTYPE_IPV4,domains)
    }

    fun setEnableIPv6(enable : Boolean) {
        DNSResolver.setEnableIPv6(enable)
    }

    fun setEnableLogger(enable: Boolean) {
        DNSResolver.setEnableLogger(enable)
    }

    fun setEnableLocalDns(enable: Boolean) {
        DNSResolver.setEnableLocalDns(enable)
    }



}